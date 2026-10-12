package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;

public enum AgentStatus {
    COMPLETED("completed"), NO_MATCH("no_match"),
    CLARIFICATION_REQUIRED("clarification_required"), DEGRADED("degraded");

    private final String value;
    AgentStatus(String value) { this.value = value; }
    @JsonValue public String value() { return value; }
    @JsonCreator public static AgentStatus fromJson(JsonNode value) {
        if (value.isTextual()) {
            for (AgentStatus status : values()) if (status.value.equals(value.asText())) return status;
        }
        throw new IllegalArgumentException("Unsupported agent status");
    }
}
