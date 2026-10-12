package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.HashSet;
import java.util.List;

public record AgentResponse(
        @NotNull AgentStatus status,
        @NotBlank @JsonDeserialize(using = WireValues.StringValue.class) String answer,
        @NotNull @JsonDeserialize(contentUsing = WireValues.LongValue.class) List<@NotNull @Positive Long> recommendedExperienceIds,
        @NotNull List<@Valid @NotNull AgentRecommendation> recommendations,
        @NotNull List<@Valid @NotNull CultureDocument> sources,
        @NotNull @Valid AgentConstraints appliedConstraints,
        @NotNull @JsonDeserialize(contentUsing = WireValues.StringValue.class) List<@NotBlank String> unsupportedConstraints,
        @NotNull @JsonDeserialize(contentUsing = WireValues.StringValue.class) List<@NotBlank String> limitations,
        @NotBlank @JsonDeserialize(using = WireValues.StringValue.class) String requestId
) implements StrictContract {
    @JsonIgnore @AssertTrue(message = "Recommendation IDs and status must be consistent")
    public boolean isOutcomeConsistent() {
        if (recommendedExperienceIds == null || recommendations == null || unsupportedConstraints == null) return false;
        if (recommendations.stream().anyMatch(v -> v == null)) return false;
        List<Long> ids = recommendations.stream().map(AgentRecommendation::experienceId).toList();
        if (!ids.equals(recommendedExperienceIds) || new HashSet<>(ids).size() != ids.size()) return false;
        if ((status == AgentStatus.NO_MATCH || status == AgentStatus.CLARIFICATION_REQUIRED) && !ids.isEmpty()) return false;
        return unsupportedConstraints.isEmpty() || status == AgentStatus.CLARIFICATION_REQUIRED;
    }
}
