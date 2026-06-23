package com.janginharou.global.client;

import com.janginharou.global.client.dto.FastApiExplainResponse;
import com.janginharou.global.config.FastApiProperties;
import com.janginharou.global.exception.ExternalServiceException;
import com.janginharou.global.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FastApiClientTest {

    private MockRestServiceServer server;
    private FastApiClient client;
    private FastApiProperties properties;
    private RestClient.Builder serverBuilder;

    @BeforeEach
    void setUp() {
        properties = new FastApiProperties();
        properties.setBaseUrl("http://fastapi.test");
        properties.setInternalApiKey("test-internal-key");

        serverBuilder = RestClient.builder()
                .baseUrl("http://fastapi.test")
                .defaultHeader("X-Internal-Api-Key", "test-internal-key");
        server = MockRestServiceServer.bindTo(serverBuilder).build();

        FastApiClient.Sleeper noOpSleeper = ms -> {};
        client = new FastApiClient(serverBuilder.build(), properties, noOpSleeper);
    }

    @Test
    void sendsInternalKeyAndParsesExplainResponse() {
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Internal-Api-Key", "test-internal-key"))
                .andExpect(content().json("""
                        {"query":"매듭이 뭐예요?","locale":"ko"}
                        """))
                .andRespond(withSuccess("""
                        {
                          "answer": "전통 매듭 해설",
                          "sources": [{"id": 7, "name": "매듭장", "source": "국가유산포털", "category": "공예"}],
                          "matchingKeywords": ["매듭장"],
                          "recommendedCategories": ["공예"]
                        }
                        """, MediaType.APPLICATION_JSON));

        FastApiExplainResponse response = client.explainCulture("매듭이 뭐예요?", "ko");

        assertThat(response.getAnswer()).isEqualTo("전통 매듭 해설");
        assertThat(response.getSources()).hasSize(1);
        assertThat(response.getMatchingKeywords()).containsExactly("매듭장");
        assertThat(response.getRecommendedCategories()).containsExactly("공예");
        server.verify();
    }

    @Test
    void mapsValidationFailureToInvalidRequest() {
        server.expect(requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> client.explainCulture("", "ko"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void mapsAuthenticationFailureToConfigurationError() {
        server.expect(requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.explainCulture("질문", "ko"))
                .isInstanceOfSatisfying(ExternalServiceException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo("AI_CONFIG_ERROR"));
    }

    @Test
    void retriesServiceUnavailableOnceAndReturnsSuccessfulResponse() {
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withSuccess("""
                        {
                          "answer": "503 재시도 후 성공",
                          "sources": [],
                          "matchingKeywords": [],
                          "recommendedCategories": []
                        }
                        """, MediaType.APPLICATION_JSON));

        FastApiExplainResponse response = client.explainCulture("질문", "ko");

        assertThat(response.getAnswer()).isEqualTo("503 재시도 후 성공");
        assertThat(response.getSources()).isEmpty();
        server.verify();
    }

    @Test
    void retriesServiceUnavailableOnceAndMapsFailureToUnavailableError() {
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.explainCulture("질문", "ko"))
                .isInstanceOfSatisfying(ExternalServiceException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo("AI_UNAVAILABLE"));
        server.verify();
    }

    @Test
    void retriesBadGatewayOnceAndReturnsSuccessfulResponse() {
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_GATEWAY));
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withSuccess("""
                        {
                          "answer": "재시도 후 성공",
                          "sources": [],
                          "matchingKeywords": [],
                          "recommendedCategories": []
                        }
                        """, MediaType.APPLICATION_JSON));

        FastApiExplainResponse response = client.explainCulture("질문", "ko");

        assertThat(response.getAnswer()).isEqualTo("재시도 후 성공");
        assertThat(response.getSources()).isEmpty();
        server.verify();
    }

    @Test
    void retriesBadGatewayOnceAndMapsFailureToUnavailableError() {
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_GATEWAY));
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> client.explainCulture("질문", "ko"))
                .isInstanceOfSatisfying(ExternalServiceException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo("AI_UNAVAILABLE"));
        server.verify();
    }

    @Test
    void mapsSocketTimeoutToTimeoutError() {
        server.expect(requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(request -> {
                    throw new ResourceAccessException("timed out", new SocketTimeoutException("timeout"));
                });

        assertThatThrownBy(() -> client.explainCulture("질문", "ko"))
                .isInstanceOfSatisfying(ExternalServiceException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo("AI_TIMEOUT"));
    }

    @Test
    void mapsConnectionResetToRetryableException() {
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(request -> {
                    throw new ResourceAccessException("Connection reset", new SocketException("Connection reset"));
                });
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withSuccess("""
                        {
                          "answer": "재시도 후 성공",
                          "sources": [],
                          "matchingKeywords": [],
                          "recommendedCategories": []
                        }
                        """, MediaType.APPLICATION_JSON));

        FastApiExplainResponse response = client.explainCulture("질문", "ko");

        assertThat(response.getAnswer()).isEqualTo("재시도 후 성공");
        server.verify();
    }

    @Test
    void mapsBadRequestToInvalidRequestException() {
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> client.explainCulture("질문", "ko"))
                .isInstanceOf(InvalidRequestException.class);
        server.verify();
    }

    @Test
    void mapsUnauthorizedToConfigurationError() {
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.explainCulture("질문", "ko"))
                .isInstanceOfSatisfying(ExternalServiceException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo("AI_CONFIG_ERROR"));
        server.verify();
    }

    @Test
    void retriesConnectionResetOnceAndMapsFailureToUnavailableError() {
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(request -> {
                    throw new ResourceAccessException("Connection reset", new ConnectException("Connection reset"));
                });
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(request -> {
                    throw new ResourceAccessException("Connection reset", new ConnectException("Connection reset"));
                });

        assertThatThrownBy(() -> client.explainCulture("질문", "ko"))
                .isInstanceOfSatisfying(ExternalServiceException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo("AI_UNAVAILABLE"));
        server.verify();
    }

    @Test
    void verifiesSleeperIsCalledOnRetry() {
        AtomicInteger sleepCallCount = new AtomicInteger(0);
        FastApiClient.Sleeper mockSleeper = ms -> sleepCallCount.incrementAndGet();

        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_GATEWAY));
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withSuccess("""
                        {"answer": "재시도 후 성공", "sources": [], "matchingKeywords": [], "recommendedCategories": []}
                        """, MediaType.APPLICATION_JSON));

        FastApiClient clientWithMockSleeper = new FastApiClient(serverBuilder.build(), properties, mockSleeper);
        clientWithMockSleeper.explainCulture("질문", "ko");

        assertThat(sleepCallCount.get()).isEqualTo(1);
        server.verify();
    }

    @Test
    void verifiesRetryDelayIsUsedFromProperties() {
        AtomicLong delayUsed = new AtomicLong(0);
        FastApiClient.Sleeper delaySpy = ms -> delayUsed.set(ms);

        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(withSuccess("""
                        {"answer": "성공", "sources": [], "matchingKeywords": [], "recommendedCategories": []}
                        """, MediaType.APPLICATION_JSON));

        FastApiProperties propsWithDelay = new FastApiProperties();
        propsWithDelay.setBaseUrl("http://fastapi.test");
        propsWithDelay.setInternalApiKey("test-internal-key");
        propsWithDelay.setRetryDelayMs(1000);
        FastApiClient clientWithDelaySpy = new FastApiClient(serverBuilder.build(), propsWithDelay, delaySpy);
        clientWithDelaySpy.explainCulture("질문", "ko");

        assertThat(delayUsed.get()).isEqualTo(1000);
        server.verify();
    }

    @Test
    void doesNotRetryOnArbitrarySocketException() {
        server.expect(once(), requestTo("http://fastapi.test/api/v1/ai/explain"))
                .andRespond(request -> {
                    throw new ResourceAccessException("Network error", new java.net.SocketException("Network interrupted"));
                });

        assertThatThrownBy(() -> client.explainCulture("질문", "ko"))
                .isInstanceOfSatisfying(ExternalServiceException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo("AI_UNAVAILABLE"));
        server.verify();
    }
}
