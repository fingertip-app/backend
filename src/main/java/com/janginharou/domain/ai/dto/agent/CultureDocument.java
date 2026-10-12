package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record CultureDocument(
        @NotBlank @JsonDeserialize(using = WireValues.StringValue.class) String documentId,
        @NotBlank @JsonDeserialize(using = WireValues.StringValue.class) String heritageId,
        @NotBlank @JsonDeserialize(using = WireValues.StringValue.class) String title,
        @NotBlank @JsonDeserialize(using = WireValues.StringValue.class) String source,
        @JsonProperty(value = "category", required = true) @JsonDeserialize(using = WireValues.StringValue.class) String category,
        @NotNull @Size(max = 1200) @JsonDeserialize(using = WireValues.StringValue.class) String contentExcerpt
) implements StrictContract {}
