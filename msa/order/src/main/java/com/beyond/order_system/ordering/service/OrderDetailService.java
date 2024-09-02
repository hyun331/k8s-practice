package com.beyond.order_system.ordering.service;

import com.beyond.order_system.ordering.repository.OrderDetailRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailService {
    private final OrderDetailRepository orderDetailRepository;

    public OrderDetailService(OrderDetailRepository orderDetailRepository) {
        this.orderDetailRepository = orderDetailRepository;
    }
}
