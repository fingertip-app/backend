package com.janginharou.global.client;

import com.janginharou.global.client.dto.FastApiExplainRequest;
import com.janginharou.global.client.dto.FastApiExplainResponse;
import com.janginharou.global.client.dto.FastApiRecommendationRequest;
import com.janginharou.global.client.dto.FastApiRecommendationResponse;
import com.janginharou.global.client.dto.FastApiSummarizeRequest;
import com.janginharou.global.client.dto.FastApiSummarizeResponse;
import com.janginharou.global.config.FastApiProperties;
import com.janginharou.global.exception.ExternalServiceException;
import com.janginharou.global.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;

@Component
@RequiredArgsConstructor
public class FastApiClient {

    private static final int MAX_PROVIDER_RETRY_ATTEMPTS = 1;

    private final RestClient fastApiRestClient;
    private final FastApiProperties properties;
    private final Sleeper sleeper;

    @FunctionalInterface
    public interface Sleeper {
        void sleep(long ms) throws InterruptedException;
    }

    public FastApiExplainResponse explainCulture(String query, String locale) {
        int attempt = 0;
        while (true) {
            try {
                return requestExplainCulture(query, locale);
            } catch (RetryableProviderException e) {
                if (attempt >= MAX_PROVIDER_RETRY_ATTEMPTS) {
                    throw new ExternalServiceException("AI service is unavailable", e, "AI_UNAVAILABLE");
                }
                attempt++;
                sleepBeforeRetry();
            }
        }
    }

    public FastApiSummarizeResponse summarizeReview(String content, String locale) {
        int attempt = 0;
        while (true) {
            try {
                return requestSummarizeReview(content, locale);
            } catch (RetryableProviderException e) {
                if (attempt >= MAX_PROVIDER_RETRY_ATTEMPTS) {
                    throw new ExternalServiceException("AI service is unavailable", e, "AI_UNAVAILABLE");
                }
                attempt++;
                sleepBeforeRetry();
            }
        }
    }

    public FastApiRecommendationResponse getRecommendations(FastApiRecommendationRequest request) {
        int attempt = 0;
        while (true) {
            try {
                return requestRecommendations(request);
            } catch (RetryableProviderException e) {
                if (attempt >= MAX_PROVIDER_RETRY_ATTEMPTS) {
                    throw new ExternalServiceException("AI service is unavailable", e, "AI_UNAVAILABLE");
                }
                attempt++;
                sleepBeforeRetry();
            }
        }
    }

    private FastApiExplainResponse requestExplainCulture(String query, String locale) {
        try {
            return fastApiRestClient.post()
                    .uri("/api/v1/ai/explain")
                    .body(new FastApiExplainRequest(query, locale))
                    .retrieve()
                    .onStatus(status -> status.value() == 422, (request, response) -> {
                        throw new InvalidRequestException("AI query validation failed");
                    })
                    .onStatus(status -> status.value() == 400, (request, response) -> {
                        throw new InvalidRequestException("AI query validation failed");
                    })
                    .onStatus(this::isConfigurationFailure, (request, response) -> {
                        throw new ExternalServiceException(
                                "AI service authentication is not configured correctly",
                                "AI_CONFIG_ERROR"
                        );
                    })
                    .onStatus(this::isRetryableProviderFailure, (request, response) -> {
                        throw new RetryableProviderException();
                    })
                    .body(FastApiExplainResponse.class);
        } catch (ResourceAccessException e) {
            if (hasTimeoutCause(e)) {
                throw new ExternalServiceException("AI response timed out", e, "AI_TIMEOUT");
            }
            if (isConnectionResetCause(e)) {
                throw new RetryableProviderException();
            }
            throw new ExternalServiceException("AI service is unavailable", e, "AI_UNAVAILABLE");
        } catch (RestClientException e) {
            throw new ExternalServiceException("AI service is unavailable", e, "AI_UNAVAILABLE");
        }
    }

    private FastApiSummarizeResponse requestSummarizeReview(String content, String locale) {
        try {
            return fastApiRestClient.post()
                    .uri("/api/v1/ai/review-summary")
                    .body(new FastApiSummarizeRequest(content, locale))
                    .retrieve()
                    .onStatus(status -> status.value() == 422, (request, response) -> {
                        throw new InvalidRequestException("AI query validation failed");
                    })
                    .onStatus(status -> status.value() == 400, (request, response) -> {
                        throw new InvalidRequestException("AI query validation failed");
                    })
                    .onStatus(this::isConfigurationFailure, (request, response) -> {
                        throw new ExternalServiceException(
                                "AI service authentication is not configured correctly",
                                "AI_CONFIG_ERROR"
                        );
                    })
                    .onStatus(this::isRetryableProviderFailure, (request, response) -> {
                        throw new RetryableProviderException();
                    })
                    .body(FastApiSummarizeResponse.class);
        } catch (ResourceAccessException e) {
            if (hasTimeoutCause(e)) {
                throw new ExternalServiceException("AI response timed out", e, "AI_TIMEOUT");
            }
            if (isConnectionResetCause(e)) {
                throw new RetryableProviderException();
            }
            throw new ExternalServiceException("AI service is unavailable", e, "AI_UNAVAILABLE");
        } catch (RestClientException e) {
            throw new ExternalServiceException("AI service is unavailable", e, "AI_UNAVAILABLE");
        }
    }

    private FastApiRecommendationResponse requestRecommendations(FastApiRecommendationRequest request) {
        try {
            return fastApiRestClient.post()
                    .uri("/api/v1/ai/recommendations")
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.value() == 422, (req, response) -> {
                        throw new InvalidRequestException("AI query validation failed");
                    })
                    .onStatus(status -> status.value() == 400, (req, response) -> {
                        throw new InvalidRequestException("AI query validation failed");
                    })
                    .onStatus(this::isConfigurationFailure, (req, response) -> {
                        throw new ExternalServiceException(
                                "AI service authentication is not configured correctly",
                                "AI_CONFIG_ERROR"
                        );
                    })
                    .onStatus(this::isRetryableProviderFailure, (req, response) -> {
                        throw new RetryableProviderException();
                    })
                    .body(FastApiRecommendationResponse.class);
        } catch (ResourceAccessException e) {
            if (hasTimeoutCause(e)) {
                throw new ExternalServiceException("AI response timed out", e, "AI_TIMEOUT");
            }
            if (isConnectionResetCause(e)) {
                throw new RetryableProviderException();
            }
            throw new ExternalServiceException("AI service is unavailable", e, "AI_UNAVAILABLE");
        } catch (RestClientException e) {
            throw new ExternalServiceException("AI service is unavailable", e, "AI_UNAVAILABLE");
        }
    }

    private boolean isConfigurationFailure(HttpStatusCode status) {
        return status.value() == 401 || status.value() == 403;
    }

    private boolean isRetryableProviderFailure(HttpStatusCode status) {
        return status.value() == 502 || status.value() == 503;
    }

    private boolean hasTimeoutCause(Throwable error) {
        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof SocketTimeoutException || cause instanceof HttpTimeoutException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private boolean isConnectionResetCause(Throwable error) {
        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof java.net.ConnectException) {
                return true;
            }
            if (cause instanceof java.net.SocketException &&
                "Connection reset".equalsIgnoreCase(cause.getMessage())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private void sleepBeforeRetry() {
        try {
            sleeper.sleep(properties.getRetryDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("AI provider retry interrupted", e, "AI_UNAVAILABLE");
        }
    }

    private static class RetryableProviderException extends RuntimeException {
    }
}
