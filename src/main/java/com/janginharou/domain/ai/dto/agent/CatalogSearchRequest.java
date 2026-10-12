package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter @NoArgsConstructor
public class CatalogSearchRequest implements StrictContract {
    @NotNull @Size(min = 1, max = 5)
    @JsonDeserialize(contentUsing = WireValues.StringValue.class)
    private List<@NotBlank @Size(max = 50) String> queryTerms;
    @Valid @NotNull @JsonSetter(nulls = Nulls.FAIL)
    private AgentConstraints constraints = new AgentConstraints();
    @NotNull @Min(1) @Max(10) @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = WireValues.IntegerValue.class)
    private Integer limit = 5;

    public CatalogSearchRequest(List<String> queryTerms, AgentConstraints constraints, Integer limit) {
        this.queryTerms = queryTerms == null ? null : queryTerms.stream().map(v -> v == null ? null : v.strip()).toList();
        this.constraints = constraints == null ? new AgentConstraints() : constraints;
        this.limit = limit == null ? 5 : limit;
    }
}
