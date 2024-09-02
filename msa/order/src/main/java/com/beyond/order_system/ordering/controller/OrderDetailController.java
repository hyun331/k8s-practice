package com.beyond.order_system.ordering.controller;

import com.beyond.order_system.ordering.repository.OrderingRepository;
import com.beyond.order_system.ordering.service.OrderDetailService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderDetailController {
    private final OrderDetailService orderDetailService;

    public OrderDetailController(OrderDetailService orderDetailService) {
        this.orderDetailService = orderDetailService;
    }
}
