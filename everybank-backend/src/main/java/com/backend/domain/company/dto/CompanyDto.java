package com.backend.domain.company.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyDto {
    @JsonProperty("fin_co_no")
    private String finCoNo;

    @JsonProperty("kor_co_nm")
    private String korCoNm;

}