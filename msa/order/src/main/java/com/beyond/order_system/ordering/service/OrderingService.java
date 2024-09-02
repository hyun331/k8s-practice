package com.beyond.order_system.ordering.service;

import com.beyond.order_system.common.config.RestTemplateConfig;
import com.beyond.order_system.common.dto.CommonResDto;
import com.beyond.order_system.common.service.StockInventoryService;

import com.beyond.order_system.ordering.controller.SseController;
import com.beyond.order_system.ordering.domain.OrderDetail;
import com.beyond.order_system.ordering.domain.OrderStatus;
import com.beyond.order_system.ordering.domain.Ordering;
import com.beyond.order_system.ordering.dto.*;
import com.beyond.order_system.ordering.repository.OrderDetailRepository;
import com.beyond.order_system.ordering.repository.OrderingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
//import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StockInventoryService stockInventoryService;
    private final SseController sseController;
    private final RestTemplate restTemplate;
    private final ProductFeign productFeign;

    @Autowired
    public OrderingService(OrderingRepository orderingRepository, OrderDetailRepository orderDetailRepository, StockInventoryService stockInventoryService, SseController sseController, RestTemplate restTemplate1, ProductFeign productFeign) {
        this.orderingRepository = orderingRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.stockInventoryService = stockInventoryService;
        this.sseController = sseController;
        this.restTemplate = restTemplate1;
        this.productFeign = productFeign;
    }

    public Ordering orderRestTemplateCreate(List<OrderingReqDto> dtos) {
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Ordering ordering = Ordering.builder()
                .memberEmail(memberEmail)
                .build();

        //msa이기 때문에 product를 요청해서 가져와야함
        for(OrderingReqDto dto : dtos){
//            eureka에 product-service 물어봐서 product와 연동
            String productGetUrl = "http://product-service/product/"+dto.getProductId();
            //http header 조립 - 토큰 세팅을 위해
            HttpHeaders httpHeaders = new HttpHeaders();
            //토큰은 header안에 존재
            String token = (String)SecurityContextHolder.getContext().getAuthentication().getCredentials();
            httpHeaders.set("Authorization",token);
            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

            //product에서 commonResDto 형태로 주기 때문에 이 형식으로 받아야함
            ResponseEntity<CommonResDto> productEntity = restTemplate.exchange(productGetUrl, HttpMethod.GET, entity, CommonResDto.class);
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDto productDto = objectMapper.convertValue(productEntity.getBody().getResult(), ProductDto.class);
            //product api의 요청을 통해 product 객체를 조회해야함
            if(productDto.getName().contains("sale")){
                int newQuantity = stockInventoryService.decreaseStock(productDto.getId(), dto.getProductCount()).intValue();
                if(newQuantity<0){
                    throw new IllegalArgumentException("재고 부족");
                }
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(productDto.getId(), dto.getProductCount()));
            }
            else{
                //재고 수 확인
                if(dto.getProductCount()>productDto.getStockQuantity()){
                    throw new IllegalArgumentException(productDto.getName()+" 재고 부족");
                }
                //restTemplage을 통해 update 요청을 보내야함
                String updateUrl = "http://product-service/product/update/stock";
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ProductUpdateStockDto> updateEntity = new HttpEntity<>(new ProductUpdateStockDto(dto.getProductId(), dto.getProductCount()), httpHeaders);

                restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, Void.class);
            }

            int quantity = dto.getProductCount();
            OrderDetail orderDetail =  OrderDetail.builder()
                    .productId(productDto.getId())
                    .quantity(quantity)
                    .ordering(ordering)
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }

        Ordering savedOrdering = orderingRepository.save(ordering);
        sseController.publishMessage(savedOrdering.fromEntity(), "admin@naver.com");
        return savedOrdering;
    }

    //feign 사용
    public Ordering orderFeignClientCreate(List<OrderingReqDto> dtos) {
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Ordering ordering = Ordering.builder()
                .memberEmail(memberEmail)
                .build();

        for(OrderingReqDto dto : dtos){
            //responseEntity가 기본 응답 값이므로 바로 CommonResDto로 매핑
            CommonResDto commonResDto = productFeign.getProductById(dto.getProductId());
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDto productDto = objectMapper.convertValue(commonResDto.getResult(), ProductDto.class);


            if(productDto.getName().contains("sale")){
                int newQuantity = stockInventoryService.decreaseStock(productDto.getId(), dto.getProductCount()).intValue();
                if(newQuantity<0){
                    throw new IllegalArgumentException("재고 부족");
                }
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(productDto.getId(), dto.getProductCount()));
            }
            else{
                if(dto.getProductCount()>productDto.getStockQuantity()){
                    throw new IllegalArgumentException(productDto.getName()+" 재고 부족");
                }

                productFeign.updateProductStock(new ProductUpdateStockDto(dto.getProductId(), dto.getProductCount()));

            }

            int quantity = dto.getProductCount();
            OrderDetail orderDetail =  OrderDetail.builder()
                    .productId(productDto.getId())
                    .quantity(quantity)
                    .ordering(ordering)
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }

        Ordering savedOrdering = orderingRepository.save(ordering);
        sseController.publishMessage(savedOrdering.fromEntity(), "admin@naver.com");
        return savedOrdering;
    }

    //조회는 feign, 변경은 kafka
//    public Ordering orderFeignKafkaCreate(List<OrderingReqDto> dtos) {
//        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
//        Ordering ordering = Ordering.builder()
//                .memberEmail(memberEmail)
//                .build();
//
//        for(OrderingReqDto dto : dtos){
//            //responseEntity가 기본 응답 값이므로 바로 CommonResDto로 매핑
//            CommonResDto commonResDto = productFeign.getProductById(dto.getProductId());
//            ObjectMapper objectMapper = new ObjectMapper();
//            ProductDto productDto = objectMapper.convertValue(commonResDto.getResult(), ProductDto.class);
//
//
//            if(productDto.getName().contains("sale")){
//                int newQuantity = stockInventoryService.decreaseStock(productDto.getId(), dto.getProductCount()).intValue();
//                if(newQuantity<0){
//                    throw new IllegalArgumentException("재고 부족");
//                }
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(productDto.getId(), dto.getProductCount()));
//            }
//            else{
//                if(dto.getProductCount()>productDto.getStockQuantity()){
//                    throw new IllegalArgumentException(productDto.getName()+" 재고 부족");
//                }
//                ProductUpdateStockDto productUpdateStockDto = new ProductUpdateStockDto(dto.getProductId(), dto.getProductCount());
//                // kafka의 topic == rabbitmq의 큐
//                kafkaTemplate.send("product-update-topic", productUpdateStockDto);
//            }
//
//            int quantity = dto.getProductCount();
//            OrderDetail orderDetail =  OrderDetail.builder()
//                    .productId(productDto.getId())
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    .build();
//            ordering.getOrderDetails().add(orderDetail);
//        }
//
//        Ordering savedOrdering = orderingRepository.save(ordering);
//        sseController.publishMessage(savedOrdering.fromEntity(), "admin@naver.com");
//        return savedOrdering;
//    }
//
        public List<OrderListResDto> orderList() {
        List<Ordering> orderingList = orderingRepository.findAll(); //주문 리스트
        List<OrderListResDto> orderListResDtoList = new ArrayList<>();
        for(Ordering ordering : orderingList){  //각 주문마다
            orderListResDtoList.add(ordering.fromEntity());
        }
        return orderListResDtoList;
    }

    public List<OrderListResDto> myOrderList() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Ordering> orderingList = orderingRepository.findAllByMemberEmail(email);
        List<OrderListResDto> orderListResDtoList = new ArrayList<>();
        for(Ordering ordering : orderingList){
            orderListResDtoList.add(ordering.fromEntity());
        }
        return orderListResDtoList;
    }

    public Ordering cancelOrder(Long id) {
        Ordering ordering = orderingRepository.findById(id).orElseThrow(()->new EntityNotFoundException("order not found"));
        ordering.updateStatus(OrderStatus.CANCLED);
        return ordering;
    }
}
