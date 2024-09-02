package com.beyond.order_system.ordering.service;

import com.beyond.order_system.common.config.FeignConfig;
import com.beyond.order_system.common.dto.CommonResDto;
import com.beyond.order_system.ordering.dto.ProductUpdateStockDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


//url 설정을 추가하여 service 자원을 검색하도록 설정. url의 product-service는 쿠버네티스의 해당 서비스명
@FeignClient(name = "product-service", url="http://product-service", configuration = FeignConfig.class)
public interface ProductFeign {

    @GetMapping(value = "/product/{id}")
    CommonResDto getProductById(@PathVariable("id") Long id);

    @PutMapping(value = "/product/update/stock")
    void updateProductStock(@RequestBody ProductUpdateStockDto dto);
}
