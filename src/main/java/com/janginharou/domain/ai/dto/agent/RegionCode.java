package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;

public enum RegionCode {
    SEOUL, GYEONGGI, INCHEON, BUSAN, JEJU;

    @JsonCreator
    public static RegionCode fromJson(JsonNode value) {
        if (!value.isTextual()) throw new IllegalArgumentException("Region code must be a string");
        return valueOf(value.asText());
    }
}
