package com.janginharou.domain.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendationRequest {

    @Size(max = 500)
    private String freeText;

    @NotNull
    private CompanionType companionType;

    @NotNull
    @Min(1)
    @Max(20)
    private Integer headCount;

    @NotNull
    @Size(min = 1, max = 5)
    private List<@NotBlank @Size(max = 50) String> interests;

    @Size(max = 50)
    private String region;

    private TimePreference timePreference;

    @Valid
    @Size(max = 10)
    private List<ConversationMessage> conversationHistory;

    @Size(max = 10)
    private String locale;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationMessage {
        @NotNull
        private ConversationRole role;

        @NotBlank
        @Size(max = 500)
        private String content;
    }

    public enum CompanionType {
        ALONE,
        FRIEND,
        FAMILY,
        COUPLE,
        KIDS,
        FOREIGN_GUEST,
        OTHER
    }

    public enum TimePreference {
        MORNING,
        AFTERNOON,
        EVENING,
        WEEKDAY,
        WEEKEND,
        ANYTIME
    }

    public enum ConversationRole {
        USER,
        ASSISTANT
    }
}
