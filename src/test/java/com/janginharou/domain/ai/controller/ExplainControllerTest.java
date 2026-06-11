package com.janginharou.domain.ai.controller;

import com.janginharou.domain.ai.dto.ExplainResponse;
import com.janginharou.domain.ai.service.ExplainService;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.config.JwtTokenProvider;
import com.janginharou.global.config.SecurityConfig;
import com.janginharou.global.exception.ExternalServiceException;
import com.janginharou.global.security.SecurityErrorHandlers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExplainController.class)
@WithMockUser
@Import(SecurityConfig.class)
class ExplainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExplainService explainService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SecurityErrorHandlers securityErrorHandlers;

    @Test
    void returnsExplainResponse() throws Exception {
        when(explainService.explain(any())).thenReturn(ExplainResponse.builder()
                .answer("판소리 해설")
                .sources(List.of())
                .matchingKeywords(List.of("판소리"))
                .recommendedCategories(List.of("음악"))
                .recommendedTags(List.of("판소리", "음악"))
                .relatedExperiences(List.of())
                .fallback(false)
                .build());

        mockMvc.perform(post("/v1/ai/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "query": "판소리가 뭐예요?",
                                  "locale": "ko"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.answer").value("판소리 해설"))
                .andExpect(jsonPath("$.data.recommendedTags[0]").value("판소리"))
                .andExpect(jsonPath("$.data.fallback").value(false));
    }

    @Test
    void rejectsBlankQuery() throws Exception {
        mockMvc.perform(post("/v1/ai/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query": "   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void rejectsMissingQuery() throws Exception {
        mockMvc.perform(post("/v1/ai/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"locale": "ko"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void mapsUnavailableErrorToServiceUnavailable() throws Exception {
        when(explainService.explain(any())).thenThrow(
                new ExternalServiceException("AI service is unavailable", "AI_UNAVAILABLE")
        );

        mockMvc.perform(post("/v1/ai/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query": "판소리"}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("AI_UNAVAILABLE"));
    }

    @Test
    void mapsTimeoutErrorToGatewayTimeout() throws Exception {
        when(explainService.explain(any())).thenThrow(
                new ExternalServiceException("AI response timed out", "AI_TIMEOUT")
        );

        mockMvc.perform(post("/v1/ai/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query": "판소리"}
                                """))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.errorCode").value("AI_TIMEOUT"));
    }
}
