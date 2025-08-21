package com.backend.domain.financial.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingProductOptionDto {
    @JsonProperty("fin_prdt_cd")
    private String finPrdtCd;

    @JsonProperty("intr_rate_type")
    private String intrRateType;

    @JsonProperty("intr_rate_type_nm")
    private String intrRateTypeNm;

    @JsonProperty("rsrv_type")
    private String rsrvType;

    @JsonProperty("rsrv_type_nm")
    private String rsrvTypeNm;

    @JsonProperty("save_trm")
    private String saveTrm;

    @JsonProperty("intr_rate")
    private String intrRate;

    @JsonProperty("intr_rate2")
    private String intrRate2;

}
