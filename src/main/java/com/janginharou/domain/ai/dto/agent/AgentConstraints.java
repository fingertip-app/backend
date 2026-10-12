package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter @NoArgsConstructor @AllArgsConstructor
public class AgentConstraints implements StrictContract {
    private RegionCode regionCode;
    @DecimalMin("0") @JsonDeserialize(using = WireValues.DecimalValue.class) @JsonSerialize(using = WireValues.DecimalString.class)
    private BigDecimal maxPriceKrw;
    @NotNull @Min(1) @Max(20) @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = WireValues.IntegerValue.class)
    private Integer headCount = 1;

    @JsonIgnore @AssertTrue(message = "Price supports at most two decimal places")
    public boolean isPricePrecisionValid() {
        return maxPriceKrw == null || maxPriceKrw.stripTrailingZeros().scale() <= 2;
    }
}
