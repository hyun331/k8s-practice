package com.beyond.order_system.member.dto;

import com.beyond.order_system.common.domain.Address;
import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSaveReqDto {

    //간단한 validation 검사
    @NotEmpty(message = "name is essential")
    private String name;

    @NotEmpty(message = "email is essential")
    private String email;

    @NotEmpty(message = "password is essential")
    @Size(min = 8, message="password minimum length is 8")
    private String password;

//    private String city;
//    private String street;
//    private String zipcode;

    private Address address;
    @Builder.Default
    private Role role = Role.USER;


    public Member toEntity(String encodedPassword){
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .role(this.role)
                .password(encodedPassword)
                .address(this.address)
//                .address(Address.builder().city(this.city).street(this.street).zipcode(this.zipcode).build())
                .build();
    }
}
