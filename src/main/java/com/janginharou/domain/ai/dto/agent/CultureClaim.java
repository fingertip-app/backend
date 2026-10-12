package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.*;
import java.util.List;

public record CultureClaim(
        @NotBlank @JsonDeserialize(using = WireValues.StringValue.class) String text,
        @NotNull @Size(min = 1) @JsonDeserialize(contentUsing = WireValues.StringValue.class) List<@NotBlank String> documentIds
) implements StrictContract {}
