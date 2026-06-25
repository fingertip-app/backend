package com.janginharou.global.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FastApiRecommendationRequest {
    private String freeText;
    private String companionType;
    private int headCount;
    private List<String> interests;
    private String region;
    private String timePreference;
    private List<ConversationMessage> conversationHistory;
    private String locale;

    @Getter
    @AllArgsConstructor
    public static class ConversationMessage {
        private String role;
        private String content;
    }
}
