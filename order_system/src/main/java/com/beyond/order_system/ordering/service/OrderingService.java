package com.beyond.order_system.ordering.service;

import com.beyond.order_system.common.service.StockInventoryService;
import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.repository.MemberRepository;
import com.beyond.order_system.ordering.controller.SseController;
import com.beyond.order_system.ordering.domain.OrderDetail;
import com.beyond.order_system.ordering.domain.OrderStatus;
import com.beyond.order_system.ordering.domain.Ordering;
import com.beyond.order_system.ordering.dto.OrderingReqDto;
import com.beyond.order_system.ordering.dto.OrderListResDto;
import com.beyond.order_system.ordering.dto.StockDecreaseEvent;
import com.beyond.order_system.ordering.repository.OrderDetailRepository;
import com.beyond.order_system.ordering.repository.OrderingRepository;
import com.beyond.order_system.product.domain.Product;
import com.beyond.order_system.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    //재고 redis를 이용하여 체크
    private final StockInventoryService stockInventoryService;
    //rabbitmq 사용을 위해
//    private final StockDecreaseEventHandler stockDecreaseEventHandler;

    //알림을 위해
    private final SseController sseController;
    @Autowired
    public OrderingService(OrderingRepository orderingRepository, MemberRepository memberRepository, ProductRepository productRepository, OrderDetailRepository orderDetailRepository, StockInventoryService stockInventoryService, /*StockDecreaseEventHandler stockDecreaseEventHandler,*/ SseController sseController) {
        this.orderingRepository = orderingRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.stockInventoryService = stockInventoryService;
//        this.stockDecreaseEventHandler = stockDecreaseEventHandler;
        this.sseController = sseController;
    }

    //synchronize 키워드를 붙이면 문제없지 않나? 한번에 한스레드만 이 메서드를 쓰기 때문
    //synchronize를 설정하더라도, 재고감소가 db에 반영되는 시점은 트랜잭션이 커밋되고 종료되는 시점
    public Ordering orderingCreate(List<OrderingReqDto> dtos) {

        //방법 2. jpa에 최적화된 방식
        //재고 개수 적용
            //내가 차감하고자 하는 개수보다 재고가 작으면 예외 발생 illegal
            //정상적으로 차감 가능하면 재고 감수 update
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        for(OrderingReqDto dto : dtos){
            Product product = productRepository.findById(dto.getProductId()).orElse(null);

            //redis를 통한 재고관리 및 재고 잔량 확인
            if(product.getName().contains("sale")){
                int newQuantity = stockInventoryService.decreaseStock(product.getId(), dto.getProductCount()).intValue();
                if(newQuantity<0){
                    throw new IllegalArgumentException("재고 부족");
                }

                //남은 잔량을 왜 가져오는걸까? - rdb와 연동을 하기 위해
                //redis 한번 변경될 때마다 rdb에 바로 연동하면 데드락 또는 갱신이상 걸릴 수 있음
                //이벤트 기반 아키텍처 사용하기
                //queueing 서비스 : rabbit MQ, kafka
                //MQ : message queue를 사용하여 해결. 요청을 que에 담아뒀다가(publish) spring이 listen해서 가져가서 rdb에 적용
                //rabbitmq를 통해 비동기적으로 이벤트 처리
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(product.getId(), dto.getProductCount()));

            }
            else{
                //재고 수 확인
                if(dto.getProductCount()>product.getStockQuantity()){
                    throw new IllegalArgumentException(product.getName()+" 재고 부족");
                }
                product.updateStockQunatity(dto.getProductCount());
            }



            int quantity = dto.getProductCount();
            OrderDetail orderDetail =  OrderDetail.builder()
                    .product(product)
                    .quantity(quantity)
                    .ordering(ordering) //아직 ordering을 save하지 않았는데 어떻게 ordering인지 알지? -> jpa가 알아서 해준다. cascade.persist 했기 때문
                    .build();
            ordering.getOrderDetails().add(orderDetail);

            //재고 감소
            //더티체킹으로 인해 별도의 save 불필요
            //product.updateStockQunatity(dto.getProductCount());
        }

        Ordering savedOrdering = orderingRepository.save(ordering);

        ///////주문을 하면 알림주기
        //admin@naver.com 에게 publish한다
        sseController.publishMessage(savedOrdering.fromEntity(), "admin@naver.com");


        return savedOrdering;







//        //        방법1.쉬운방식
////        Ordering생성 : member_id, status
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()->new EntityNotFoundException("없음"));
//        Ordering ordering = orderingRepository.save(dto.toEntity(member));
//
////        OrderDetail생성 : order_id, product_id, quantity
//        for(OrderingReqDto.OrderDto orderDto : dto.getOrderDtos()){
//            Product product = productRepository.findById(orderDto.getProductId()).orElse(null);
//            int quantity = orderDto.getProductCount();
//            OrderDetail orderDetail =  OrderDetail.builder()
//                    .product(product)
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    .build();
//            orderDetailRepository.save(orderDetail);
//        }
//
//        return ordering;



    }

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
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member not found"));
        List<Ordering> orderingList = orderingRepository.findAllByMember(member);
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
