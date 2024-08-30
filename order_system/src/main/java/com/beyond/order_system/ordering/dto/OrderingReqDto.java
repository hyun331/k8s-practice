package com.beyond.order_system.ordering.dto;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.ordering.domain.OrderDetail;
import com.beyond.order_system.ordering.domain.OrderStatus;
import com.beyond.order_system.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderingReqDto {
//    private Long memberId;    //로그인 후 -> 이제 이건  필요 없음.
//    private List<OrderDto> orderDtos;

    private Long productId; //로그인 후 이렇게 꺼낸 후 @RequestBody List<OrderingReqDto>하면 됨
    private Integer productCount;//로그인 후
//로그인 후//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class OrderDto{
//        private Long productId;
//        private Integer productCount;
//    }
//
    public Ordering toEntity(Member member){
        return Ordering.builder()
                .member(member)
                .build();
    }
}