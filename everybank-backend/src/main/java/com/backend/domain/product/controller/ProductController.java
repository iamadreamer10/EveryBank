package com.backend.domain.product.controller;

import com.backend.domain.product.domain.DepositProduct;
import com.backend.domain.product.domain.SavingProduct;
import com.backend.domain.product.dto.response.DepositProductDetailDto;
import com.backend.domain.product.dto.response.SavingProductDetailDto;
import com.backend.domain.product.service.ProductService;
import com.backend.global.common.BaseResponse;
import com.backend.global.common.code.SuccessCode;
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
    public ResponseEntity<BaseResponse<List<DepositProduct>>> getDepositList(){
        List<DepositProduct> allDepositProduct = productService.getDepositProductList();
        return BaseResponse.success(SuccessCode.SELECT_SUCCESS,allDepositProduct);
    }

    @GetMapping("/deposit/{productCode}")
    public ResponseEntity<BaseResponse<DepositProductDetailDto>> getDepositProduct(@PathVariable String productCode){
        DepositProductDetailDto depositProductDetail = productService.getDepositProductDetail(productCode);
        return BaseResponse.success(SuccessCode.SELECT_SUCCESS, depositProductDetail);
    }

    @GetMapping("/savings")
    public ResponseEntity<BaseResponse<List<SavingProduct>>> getSavingList(){
        List<SavingProduct> allSavingProduct = productService.getSavingProductList();
        return BaseResponse.success(SuccessCode.SELECT_SUCCESS, allSavingProduct);
    }

    @GetMapping("/savings/{productCode}")
    public ResponseEntity<BaseResponse<SavingProductDetailDto>> getSavingProduct(@PathVariable String productCode){
        SavingProductDetailDto savingProductDetail = productService.getSavingProductDetail(productCode);
        return BaseResponse.success(SuccessCode.SELECT_SUCCESS, savingProductDetail);
    }
}
