package com.beyond.order_system.ordering.domain;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.ordering.dto.OrderListResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ordering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    //orderDetail class에서 ordering과 매핑됨
    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST)
    @Builder.Default    //빌더패턴에서도 초기화 되도록 하는 설정
    private List<OrderDetail> orderDetails = new ArrayList<>();


    public OrderListResDto fromEntity(){
        List<OrderListResDto.OrderDetailDto> orderDetailDtos = new ArrayList<>();
        for(OrderDetail orderDetail : orderDetails){
            orderDetailDtos.add(orderDetail.fromEntity());
        }
        return OrderListResDto.builder()
                .id(this.id)
                .memberEmail(this.member.getEmail())
                .orderStatus(this.orderStatus)
                .orderDetailDtos(orderDetailDtos)
                .build();
    }


    public void updateStatus(OrderStatus orderStatus){
        this.orderStatus = orderStatus;

    }
}
