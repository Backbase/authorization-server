package com.backbase.authorization.security.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttributeClaimMapper implements OAuth2TokenCustomizer<JwtEncodingContext>,
    Function<OidcUserInfoAuthenticationContext, OidcUserInfo> {

    public static final String SETTINGS_TOKEN_CLAIM_MAPPERS = "settings.token.claim-mappers";

    private final ObjectMapper mapper;
    private final RegisteredClientRepository registeredClientRepository;

    @Override
    public void customize(JwtEncodingContext context) {
        if (context.getPrincipal() instanceof AttributesAuthenticationToken authenticationToken) {
            Map<String, Object> attributes = authenticationToken.getAttributes();
            getAttributeClaimMappings(context.getRegisteredClient())
                .forEach(
                    mapper -> {
                        if (context.getTokenType().getValue().equals(OidcParameterNames.ID_TOKEN)
                            && mapper.getToIdToken()) {
                            log.debug("Processing '{}' claim to include in Id Token.", mapper.getAttributeName());
                            context.getClaims()
                                .claims(
                                    c -> c.put(mapper.getAttributeName(), attributes.get(mapper.getAttributeName())));
                        } else if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)
                            && mapper.getToAccessToken()) {
                            log.debug("Processing '{}' claim to include in Access Token.", mapper.getAttributeName());
                            context.getClaims()
                                .claims(
                                    c -> c.put(mapper.getAttributeName(), attributes.get(mapper.getAttributeName())));
                        }
                    }
                );
        } else {
            log.debug("Skipping token customization given unsupported authentication type.");
        }
    }

    @Override
    public OidcUserInfo apply(OidcUserInfoAuthenticationContext context) {
        OidcIdToken idToken = context.getAuthorization()
            .getToken(OidcIdToken.class)
            .getToken();
        Map<String, Object> userInfoClaims = new HashMap<>();
        userInfoClaims.put(StandardClaimNames.SUB, idToken.getSubject());

        OAuth2AuthorizationRequest request = (OAuth2AuthorizationRequest) context.getAuthorization().getAttributes()
            .get(OAuth2AuthorizationRequest.class.getName());
        if (request != null) {
            Map<String, Object> jwtClaims = ((JwtAuthenticationToken) context.getAuthentication()
                .getPrincipal())
                .getTokenAttributes();
            getAttributeClaimMappings(request.getClientId()).stream()
                .filter(AttributeClaimMapping::getToUserInfo)
                .forEach(
                    mapper -> {
                        if (idToken.getClaims().containsKey(mapper.getAttributeName())) {
                            log.debug("Processing '{}' claim from Id Token to include in User Info.",
                                mapper.getAttributeName());
                            userInfoClaims.put(mapper.getAttributeName(),
                                idToken.getClaims().get(mapper.getAttributeName()));
                        } else if (jwtClaims.containsKey(mapper.getAttributeName())) {
                            log.debug("Processing '{}' claim from Access Token to include in User Info.",
                                mapper.getAttributeName());
                            userInfoClaims.put(mapper.getAttributeName(), jwtClaims.get(mapper.getAttributeName()));
                        }
                    }
                );
        }
        return new OidcUserInfo(userInfoClaims);
    }

    private List<AttributeClaimMapping> getAttributeClaimMappings(String clientId) {
        return getAttributeClaimMappings(registeredClientRepository.findByClientId(clientId));
    }

    private List<AttributeClaimMapping> getAttributeClaimMappings(RegisteredClient registeredClient) {
        Map attributeClaims = registeredClient.getTokenSettings()
            .getSetting(SETTINGS_TOKEN_CLAIM_MAPPERS);
        return attributeClaims.values()
            .stream()
            .map(a -> mapper.convertValue(a, AttributeClaimMapping.class))
            .toList();
    }

}
