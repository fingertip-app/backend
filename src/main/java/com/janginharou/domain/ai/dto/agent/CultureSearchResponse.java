package com.janginharou.domain.ai.dto.agent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CultureSearchResponse(
        @NotNull @Size(max = 5) List<@NotNull @Valid CultureDocument> documents
) implements StrictContract {}
