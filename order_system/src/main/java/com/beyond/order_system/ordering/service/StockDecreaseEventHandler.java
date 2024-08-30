//package com.beyond.order_system.ordering.service;
//
//import com.beyond.order_system.common.config.RabbitMqConfig;
//import com.beyond.order_system.ordering.dto.StockDecreaseEvent;
//import com.beyond.order_system.product.domain.Product;
//import com.beyond.order_system.product.repository.ProductRepository;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitHandler;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.persistence.EntityNotFoundException;
//import javax.transaction.Transactional;
//
//@Component
//public class StockDecreaseEventHandler {
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//
//    public void publish(StockDecreaseEvent event){
//        //rabbitTemplate.convertAndSend(큐 이름 , 객체);
//        rabbitTemplate.convertAndSend(RabbitMqConfig.STOCK_DECREASE_QUEUE, event);
//    }
//
//    //Component 어노테이션 있으면 Transactional 붙일 수 있음. 에러 발생시 롤백 처리할거임
//    @Transactional  //transcation이 완료된 이후에 그 다음 메세지 수신하므로, 동시성 이슈 발생하지 않음
//    //orderingService에서 mq에 발행한 것들을 바라보고있어야함
//    @RabbitListener(queues = RabbitMqConfig.STOCK_DECREASE_QUEUE)
//    public void listen(Message message) {
//        String messageBody = new String(message.getBody());
//        System.out.println(messageBody);
//
//        StockDecreaseEvent stockDecreaseEvent;
//        //json message를 objectMapper로 직접 parsing
//        ObjectMapper objectMapper = new ObjectMapper();
//        try{
//            stockDecreaseEvent = objectMapper.readValue(messageBody, StockDecreaseEvent.class);
//            //재고 update
//            Product product = productRepository.findById(stockDecreaseEvent.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));
//            product.updateStockQunatity(stockDecreaseEvent.getProductCount());
//
//        }catch (JsonProcessingException e){
//            throw new RuntimeException(e);
//        }
//
//
//    }
//}
