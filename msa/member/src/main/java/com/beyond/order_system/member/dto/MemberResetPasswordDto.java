package com.beyond.order_system.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResetPasswordDto {
    private String email;
    private String asIsPassword;
    private String toBePassword;
}
