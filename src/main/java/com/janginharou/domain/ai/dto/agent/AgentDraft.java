package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor
public class AgentDraft implements StrictContract {
    @NotNull @JsonSetter(nulls = Nulls.FAIL)
    private List<@NotNull @Valid AgentRecommendation> recommendations = List.of();
    @NotNull @JsonSetter(nulls = Nulls.FAIL)
    private List<@NotNull @Valid CultureClaim> cultureClaims = List.of();
}
