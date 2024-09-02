package com.beyond.order_system.common.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;


@Configuration
public class FeignConfig {
    //모든 feign 요청에 전역적으로 token 세팅
    @Bean
    public RequestInterceptor requestInterceptor(){
        return request -> {
            String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
            request.header(HttpHeaders.AUTHORIZATION, token);
        };
    }
}
