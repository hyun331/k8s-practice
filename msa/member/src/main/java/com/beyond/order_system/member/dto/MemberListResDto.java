package com.beyond.order_system.member.dto;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberListResDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private int orderCount;
}
