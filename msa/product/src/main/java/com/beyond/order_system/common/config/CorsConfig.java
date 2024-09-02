package com.beyond.order_system.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//cors : 같은 도메인 아니면 막겠다.
// 풀어주려면 corsConfig에서 풀어줘야함
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins("http://localhost:8081")    //허용 url 명시 //카카오 redirect시 여기에
                .allowedMethods("*")    //get, post, ...
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
