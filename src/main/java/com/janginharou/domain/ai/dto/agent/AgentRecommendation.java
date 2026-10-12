package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.*;
import java.util.List;

public record AgentRecommendation(
        @NotNull @Positive @JsonDeserialize(using = WireValues.LongValue.class) Long experienceId,
        @NotBlank @JsonDeserialize(using = WireValues.StringValue.class) String reason,
        @NotNull @Size(min = 1) @JsonDeserialize(contentUsing = WireValues.StringValue.class)
        List<@NotBlank String> evidenceKeys
) implements StrictContract {}
