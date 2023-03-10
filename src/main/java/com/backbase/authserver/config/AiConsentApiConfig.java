package com.backbase.authserver.config;

import com.mastercard.openbanking.accounts.ApiClient;
import com.mastercard.openbanking.accounts.api.AiConsentsApi;
import com.mastercard.openbanking.accounts.api.AiConsentsAuthorizationsApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(AiConsentProperties.class)
public class AiConsentApiConfig {

    @Bean
    public ApiClient apiClient(AiConsentProperties properties) {
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
