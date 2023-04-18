package com.backbase.authorization.ais.config;

import com.mastercard.mcob.ais.ApiClient;
import com.mastercard.mcob.ais.api.AiConsentsApi;
import com.mastercard.mcob.ais.api.AiConsentsAuthorizationsApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

@Slf4j
@Configuration
@EnableConfigurationProperties(OpenBankingApiProperties.class)
public class OpenBankingApiConfiguration {

    @Bean
    public ApiClient apiClient(OpenBankingApiProperties properties) {
        ApiClient apiClient = new ApiClient();
        properties.getBaseUri()
                .ifPresent(uri -> {
                    log.debug("Configuring api with Base Uri: {}", uri);
                    apiClient.updateBaseUri(uri);
                });
        return configureProxy(apiClient, properties.getProxy());
    }

    @Bean
    public AiConsentsApi aiConsentsApi(ApiClient apiClient) {
        return new AiConsentsApi(apiClient);
    }

    @Bean
    public AiConsentsAuthorizationsApi aiConsentsAuthorizationsApi(ApiClient apiClient) {
        return new AiConsentsAuthorizationsApi(apiClient);
    }

    private ApiClient configureProxy(ApiClient apiClient, OpenBankingApiProperties.Proxy proxy) {
        if (proxy.getEnabled()) {
            log.debug("Enabling proxy configuration: {}", proxy);
            HttpClient.Builder builder = HttpClient.newBuilder();
            builder.proxy(ProxySelector.of(new InetSocketAddress(proxy.getHost(), proxy.getPort())));
            return apiClient.setHttpClientBuilder(builder);
        }
        return apiClient;
    }

}
