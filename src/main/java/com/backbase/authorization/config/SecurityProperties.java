package com.backbase.authorization.config;

import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Data
@ConfigurationProperties(prefix = "mastercard.authorization.oidc")
public class SecurityProperties {

    private Map<String, OidcClient> clients;

    @Data
    public static class OidcClient {

        private String clientSecret;
        private Set<ClientAuthenticationMethod> clientAuthenticationMethods;
        private Set<AuthorizationGrantType> authorizationGrantTypes;
        private Set<String> redirectUris;
        private Set<String> scopes;
    }

}
