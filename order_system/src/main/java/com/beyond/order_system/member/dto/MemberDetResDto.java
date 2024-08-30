package com.beyond.order_system.member.dto;

import com.beyond.order_system.common.domain.Address;
import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDetResDto {
    private Long id;
    private String name;
    private String email;
//    private String city;
//    private String street;
//    private String zipcode;
    private Address address;
    private Role role;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;



}
