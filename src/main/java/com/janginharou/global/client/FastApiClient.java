package com.janginharou.global.client;

import com.janginharou.global.client.dto.FastApiExplainRequest;
import com.janginharou.global.client.dto.FastApiExplainResponse;
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

    private final RestClient fastApiRestClient;

    public FastApiExplainResponse explainCulture(String query, String locale) {
        try {
            return fastApiRestClient.post()
                    .uri("/api/v1/ai/explain")
                    .body(new FastApiExplainRequest(query, locale))
                    .retrieve()
                    .onStatus(status -> status.value() == 422, (request, response) -> {
                        throw new InvalidRequestException("AI query validation failed");
                    })
                    .onStatus(this::isConfigurationFailure, (request, response) -> {
                        throw new ExternalServiceException(
                                "AI service authentication is not configured correctly",
                                "AI_CONFIG_ERROR"
                        );
                    })
                    .onStatus(this::isProviderFailure, (request, response) -> {
                        throw new ExternalServiceException("AI service is unavailable", "AI_UNAVAILABLE");
                    })
                    .body(FastApiExplainResponse.class);
        } catch (ResourceAccessException e) {
            if (hasTimeoutCause(e)) {
                throw new ExternalServiceException("AI response timed out", e, "AI_TIMEOUT");
            }
            throw new ExternalServiceException("AI service is unavailable", e, "AI_UNAVAILABLE");
        } catch (RestClientException e) {
            throw new ExternalServiceException("AI service is unavailable", e, "AI_UNAVAILABLE");
        }
    }

    private boolean isConfigurationFailure(HttpStatusCode status) {
        return status.value() == 401 || status.value() == 403;
    }

    private boolean isProviderFailure(HttpStatusCode status) {
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
}
