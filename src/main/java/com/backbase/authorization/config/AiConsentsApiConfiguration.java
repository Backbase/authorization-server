package com.backbase.authorization.config;

import com.mastercard.openbanking.ai.ApiClient;
import com.mastercard.openbanking.ai.api.AiConsentsApi;
import com.mastercard.openbanking.ai.api.AiConsentsAuthorizationsApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(AiConsentsProperties.class)
public class AiConsentsApiConfiguration {

    @Bean
    public ApiClient apiClient(AiConsentsProperties properties) {
        log.debug("Configuring api with Base Uri: {}", properties.getBaseUri());
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri(properties.getBaseUri());
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

}
