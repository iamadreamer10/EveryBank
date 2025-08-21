package com.backend.domain.financial.api.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinancialApiResponse<T, O> {

    @JsonProperty("result")
    private ApiResult<T, O> result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiResult<T, O> {
        @JsonProperty("baseList")
        private List<T> baseList;

        @JsonProperty("optionList")
        private List<O> optionList;
    }
}
