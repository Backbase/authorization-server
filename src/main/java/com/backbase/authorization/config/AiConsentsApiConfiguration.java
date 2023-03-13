package com.backbase.authorization.config;

import com.mastercard.openbanking.ai.ApiClient;
import com.mastercard.openbanking.ai.api.AiConsentsApi;
import com.mastercard.openbanking.ai.api.AiConsentsAuthorizationsApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(AiConsentsApiProperties.class)
public class AiConsentsApiConfiguration {

    @Bean
    public ApiClient apiClient(AiConsentsApiProperties properties) {
        ApiClient apiClient = new ApiClient();
        properties.getApiBaseUri()
            .ifPresent(uri -> {
                log.debug("Configuring api with Base Uri: {}", uri);
                apiClient.updateBaseUri(uri);
            });
        return apiClient;
    }

    @Bean
    public AiConsentsApi aiConsentsApi(ApiClient apiClient) {
        return new AiConsentsApi(apiClient);
    }

    @Bean
    public AiConsentsAuthorizationsApi aiConsentsAuthorizationsApi(ApiClient apiClient) {
        return new AiConsentsAuthorizationsApi(apiClient);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "management.trace.http.in-memory")
    @ConditionalOnExpression("#{${management.endpoints.enabled-by-default:false} or ${management.trace.http.enabled:false}}")
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository();
    }

}
