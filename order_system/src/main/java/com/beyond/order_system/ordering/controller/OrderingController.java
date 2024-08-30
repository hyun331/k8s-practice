package com.beyond.order_system.ordering.controller;

import com.beyond.order_system.common.dto.CommonResDto;
import com.beyond.order_system.ordering.domain.Ordering;
import com.beyond.order_system.ordering.dto.OrderingReqDto;
import com.beyond.order_system.ordering.service.OrderingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderingController {
    private final OrderingService orderingService;


    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }

//    @PostMapping("/create")
//    public ResponseEntity<?> orderingCreate(@RequestBody OrderingReqDto orderingReqDto){
//        System.out.println("creategk\n\n\n\n");
//        Ordering ordering = orderingService.orderingCreate(orderingReqDto);
//        //entity 자체를 리턴하면 순환참조에 빠질 수 있다!
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "ordering create", ordering.getId());
//        return  new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
//
//    }



    //로그인 후
    @PostMapping("/create")
    public ResponseEntity<?> orderingCreate(@RequestBody List<OrderingReqDto> orderingReqDto){
        Ordering ordering = orderingService.orderingCreate(orderingReqDto);
        //entity 자체를 리턴하면 순환참조에 빠질 수 있다!
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "ordering create", ordering.getId());
        return  new ResponseEntity<>(commonResDto, HttpStatus.CREATED);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> orderList(){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "order list", orderingService.orderList());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    @GetMapping("/myorders")
    public ResponseEntity<?> myOrders(){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "my order list", orderingService.myOrderList());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/cancel/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "cancel order", orderingService.cancelOrder(id).getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
