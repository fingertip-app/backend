package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record CatalogExperienceResponse(
        @NotNull @Positive @JsonDeserialize(using = WireValues.LongValue.class) Long id,
        @NotBlank @JsonDeserialize(using = WireValues.StringValue.class) String title,
        @NotNull @Size(max = 1200) @JsonDeserialize(using = WireValues.StringValue.class) String descriptionExcerpt,
        @NotNull @JsonDeserialize(contentUsing = WireValues.StringValue.class) List<@NotBlank String> tags,
        @NotNull @DecimalMin("0") @JsonDeserialize(using = WireValues.DecimalValue.class) @JsonSerialize(using = WireValues.DecimalString.class) BigDecimal priceKrw,
        @NotNull @Min(1) @JsonDeserialize(using = WireValues.IntegerValue.class) Integer maxParticipants,
        @JsonProperty(value = "regionCode", required = true) RegionCode regionCode,
        @NotNull @JsonDeserialize(using = WireValues.BooleanValue.class) Boolean isActive
) implements StrictContract {
    @JsonIgnore @AssertTrue(message = "Price supports at most two decimal places")
    public boolean isPricePrecisionValid() {
        return priceKrw == null || priceKrw.stripTrailingZeros().scale() <= 2;
    }
}
