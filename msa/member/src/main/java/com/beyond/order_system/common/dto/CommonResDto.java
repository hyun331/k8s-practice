package com.beyond.order_system.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

//objectmapper할 때 필요
@Data
@NoArgsConstructor
public class CommonResDto {
    private int status_code;
    private String status_message;
    //object로 하면 여러 클래스 받을 수 있음
    private Object result;

    public CommonResDto(HttpStatus httpStatus, String status_message, Object result){
        this.status_code = httpStatus.value();
        this.status_message=status_message;
        this.result = result;
    }
}
