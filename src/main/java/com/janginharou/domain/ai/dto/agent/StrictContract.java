package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.annotation.JsonAnySetter;

/** Reject unknown properties even when the application's mapper ignores them. */
public interface StrictContract {
    @JsonAnySetter
    default void rejectUnknown(String name, Object value) {
        throw new IllegalArgumentException("Unknown agent contract field: " + name);
    }
}
