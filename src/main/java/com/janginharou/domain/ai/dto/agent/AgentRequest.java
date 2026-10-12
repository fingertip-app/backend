package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor
public class AgentRequest implements StrictContract {
    @NotBlank @Size(max = 500) @JsonDeserialize(using = WireValues.StringValue.class)
    private String query;
    @NotNull @Pattern(regexp = "ko") @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = WireValues.RawStringValue.class)
    private String locale = "ko";
    @Valid @NotNull @JsonSetter(nulls = Nulls.FAIL)
    private AgentConstraints constraints = new AgentConstraints();
}
