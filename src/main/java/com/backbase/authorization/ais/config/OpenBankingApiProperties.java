package com.backbase.authorization.ais.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Optional;

@Data
@ConfigurationProperties(prefix = "mastercard.mcob.api")
public class OpenBankingApiProperties {

    Optional<String> baseUri = Optional.empty();
    Proxy proxy = new Proxy();

    @Data
    public static class Proxy {

        Boolean enabled = false;
        String host;
        Integer port;
    }

}
