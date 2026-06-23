package com.janginharou.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fastapi")
public class FastApiProperties {

    private String baseUrl;
    private String internalApiKey;
    private int connectTimeoutMs = 1000;
    private int readTimeoutMs = 15000;
    private int retryDelayMs = 500;
}
