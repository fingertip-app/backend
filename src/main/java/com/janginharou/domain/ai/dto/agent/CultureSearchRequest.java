package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CultureSearchRequest(
        @NotBlank @Size(max = 500) @JsonDeserialize(using = WireValues.StringValue.class) String query
) implements StrictContract {}
