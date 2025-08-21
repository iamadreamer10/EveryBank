package com.backend.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingProductDto {

    @JsonProperty("fin_co_no")
    private String finCoNo;

    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd;

    @JsonProperty("kor_co_nm")
    private String korCoNm;

    @JsonProperty("fin_prdt_nm")
    private String finPrdtNm;

    @JsonProperty("join_way")
    private String joinWay;

    @JsonProperty("join_member")
    private String joinMember;

    @JsonProperty("etc_note")
    private String etcNote;

    @JsonProperty("max_limit")
    private String maxLimit;
}
