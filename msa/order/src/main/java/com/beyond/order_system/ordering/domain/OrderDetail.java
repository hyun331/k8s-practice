package com.beyond.order_system.ordering.domain;

import com.beyond.order_system.common.domain.BaseTimeEntity;
import com.beyond.order_system.ordering.dto.OrderListResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id")
    private Ordering ordering;

    private Long productId;

    public OrderListResDto.OrderDetailDto fromEntity() {
        return OrderListResDto.OrderDetailDto.builder()
                .id(this.id)
                .count(quantity)
                .build();
    }
}
