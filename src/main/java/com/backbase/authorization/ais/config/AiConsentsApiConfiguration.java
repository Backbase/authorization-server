package com.backbase.authorization.ais.config;

import com.backbase.authorization.ais.config.AiConsentsProperties.Proxy;
import com.mastercard.mcob.ais.ApiClient;
import com.mastercard.mcob.ais.api.AiConsentsApi;
import com.mastercard.mcob.ais.api.AiConsentsAuthorizationsApi;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
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
@EnableConfigurationProperties(AiConsentsProperties.class)
public class AiConsentsApiConfiguration {

    @Bean
    public ApiClient apiClient(AiConsentsProperties properties) {
        ApiClient apiClient = new ApiClient();
        properties.getApi().getBaseUri()
            .ifPresent(uri -> {
                log.debug("Configuring api with Base Uri: {}", uri);
                apiClient.updateBaseUri(uri);
            });
        return configureProxy(apiClient, properties.getApi().getProxy());
    }

    @Bean
    public AiConsentsApi aiConsentsApi(ApiClient apiClient) {
        return new AiConsentsApi(apiClient);
    }

    @Bean
    public AiConsentsAuthorizationsApi aiConsentsAuthorizationsApi(ApiClient apiClient) {
        return new AiConsentsAuthorizationsApi(apiClient);
    }

    private ApiClient configureProxy(ApiClient apiClient, Proxy proxy) {
        if (proxy.getEnabled()) {
            log.debug("Enabling proxy configuration: {}", proxy);
            HttpClient.Builder builder = HttpClient.newBuilder();
            builder.proxy(ProxySelector.of(new InetSocketAddress(proxy.getHost(), proxy.getPort())));
            return apiClient.setHttpClientBuilder(builder);
        }
        return apiClient;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "management.trace.http.in-memory")
    @ConditionalOnExpression("#{${management.endpoints.enabled-by-default:false} or ${management.trace.http.enabled:false}}")
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository();
    }

}