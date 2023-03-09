package com.backbase.authserver.config;

import com.mastercard.openbanking.accounts.ApiClient;
import com.mastercard.openbanking.accounts.api.AiConsentsApi;
import com.mastercard.openbanking.accounts.api.AiConsentsAuthorizationsApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiConsentsApiConfig {

    @Bean
    public ApiClient apiClient(@Value("${mastercard.accounts.api.base-uri}") String baseUri) {
        log.debug("Configuring api with Base Uri: {}", baseUri);
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri(baseUri);
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
