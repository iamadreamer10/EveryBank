package com.backend.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepositProductOptionDto {
    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd;

    @JsonProperty("intr_rate_type")
    private String intrRateType;

    @JsonProperty("intr_rate_type_nm")
    private String intrRateTypeNm;

    @JsonProperty("save_trm")
    private String saveTrm;

    @JsonProperty("intr_rate")
    private String intrRate;

    @JsonProperty("intr_rate2")
    private String intrRate2;

}
