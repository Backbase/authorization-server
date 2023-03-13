package com.backbase.authorization.security.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "security.authorization")
public class SecurityProperties {

    private String[] publicPaths = new String[0];

    @NotEmpty
    private Map<String, OidcClient> clientRegistration;

    @Data
    public static class OidcClient {

        private String secret;
        private Set<ClientAuthenticationMethod> clientAuthenticationMethods;
        private Set<AuthorizationGrantType> authorizationGrantTypes;
        private Set<String> redirectUris;
        private Set<String> scopes;
        private Set<ClaimMapper> claimMappers = new HashSet<>();

    }

    @Data
    public static class ClaimMapper {

        private String attributeName;
        private Boolean toUserInfo = true;
        private Boolean toAccessToken = false;
        private Boolean toIdToken = false;

    }

}
