package com.beyond.order_system.member.domain;

import com.beyond.order_system.common.domain.Address;
import com.beyond.order_system.common.domain.BaseTimeEntity;
import com.beyond.order_system.member.dto.MemberDetResDto;
import com.beyond.order_system.member.dto.MemberListResDto;
import com.beyond.order_system.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.w3c.dom.stylesheets.LinkStyle;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Ordering> orderList;

    public MemberDetResDto detFromEntity() {
        return MemberDetResDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .role(this.role)
                .address(this.address)
                .createdTime(this.getCreatedTime())
                .updatedTime(this.getUpdatedTime())
                .build();
    }

    public MemberListResDto listFromEntity() {
        return MemberListResDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .role(this.role)
                .orderCount(this.orderList.size())
                .build();
    }

    public void resetPassword(String encodePassword) {
        this.password = encodePassword;
    }
}
