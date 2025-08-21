package com.backend.domain.product.controller;

import com.backend.domain.product.domain.DepositProduct;
import com.backend.domain.product.domain.SavingProduct;
import com.backend.domain.product.dto.DepositProductDetailDto;
import com.backend.domain.product.dto.SavingProductDetailDto;
import com.backend.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/deposit")
    public ResponseEntity<List<DepositProduct>> getDepositList(){
        List<DepositProduct> allDepositProduct = productService.getDepositProductList();
        return ResponseEntity.ok(allDepositProduct);
    }

    @GetMapping("/deposit/{productCode}")
    public ResponseEntity<DepositProductDetailDto> getDepositProduct(@PathVariable String productCode){
        DepositProductDetailDto depositProductDetail = productService.getDepositProductDetail(productCode);
        return ResponseEntity.ok(depositProductDetail);
    }

    @GetMapping("/savings")
    public ResponseEntity<List<SavingProduct>> getSavingList(){
        List<SavingProduct> allSavingProduct = productService.getSavingProductList();
        return ResponseEntity.ok(allSavingProduct);
    }

    @GetMapping("/savings/{productCode}")
    public ResponseEntity<SavingProductDetailDto> getSavingProduct(@PathVariable String productCode){
        SavingProductDetailDto savingProductDetail = productService.getSavingProductDetail(productCode);
        return ResponseEntity.ok(savingProductDetail);
    }
}
