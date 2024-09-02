package com.beyond.order_system.product.controller;

import com.beyond.order_system.common.dto.CommonResDto;
import com.beyond.order_system.product.domain.Product;
import com.beyond.order_system.product.dto.ProductResDto;
import com.beyond.order_system.product.dto.ProductSaveRepDto;
import com.beyond.order_system.product.dto.ProductSearchDto;
import com.beyond.order_system.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    //form-data로 받기
    //json인 경우 -> @RequestPart ProductSaveRepDto productSaveRepDto, @RequestPart MultipartFile imageFile
    public ResponseEntity<?> productCreate(ProductSaveRepDto productSaveRepDto){
//        Product product = productService.productCreate(productSaveRepDto);
        Product product = productService.productAwsCreate(productSaveRepDto);

        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "create successfully", product);

        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<?> productList(ProductSearchDto searchDto, Pageable pageable){
        Page<ProductResDto> dtos = productService.productList(searchDto, pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "lists are found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


}
