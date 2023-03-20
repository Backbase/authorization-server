package com.backbase.authorization.security.config;


import com.backbase.authorization.ais.authentication.AiConsentAuthenticationConfigurer;
import com.backbase.authorization.ais.authentication.AiConsentAuthenticationProvider;
import com.backbase.authorization.ais.authentication.AiConsentRedirectEntryPoint;
import com.backbase.authorization.security.token.AttributesClaimsMapper;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Slf4j
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfiguration {

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
        AiConsentRedirectEntryPoint entryPoint, AttributesClaimsMapper attributesClaimsMapper)
        throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            // Enable OpenID Connect 1.0
            .oidc(oidc -> oidc.userInfoEndpoint(configurer -> configurer.userInfoMapper(attributesClaimsMapper)))
            .and()
            // Enable CORS headers
            .cors(cors -> cors.configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues()))
            // Redirect to the login page when not authenticated from the authorization endpoint
            .exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint(entryPoint))
            // Accept access tokens for AiConsentUser Info and/or Client Registration
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, SecurityProperties properties,
        AiConsentAuthenticationConfigurer configurer, AiConsentAuthenticationProvider provider)
        throws Exception {
        http
            .authorizeHttpRequests((authorize) -> authorize
                .antMatchers(properties.getPublicPaths()).permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(provider)
            // Configuring the callback endpoint that handles the authenticated redirect from the consent authorization.
            .apply(configurer);

        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(SecurityProperties properties) {
        List<RegisteredClient> registeredClients = properties.getClientRegistration().entrySet().stream().map(e ->
            RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(e.getKey())
                .clientSecret(e.getValue().getSecret())
                .clientAuthenticationMethods(m -> m.addAll(e.getValue().getClientAuthenticationMethods()))
                .authorizationGrantTypes(t -> t.addAll(e.getValue().getAuthorizationGrantTypes()))
                .redirectUris(r -> r.addAll(e.getValue().getRedirectUris()))
                .scopes(s -> s.addAll(e.getValue().getScopes()))
                .clientSettings(ClientSettings.builder().settings(consumer(e.getValue().getClientConfiguration())).build())
                .tokenSettings(TokenSettings.builder().settings(consumer(e.getValue().getTokenConfiguration())).build())
                .build()
        ).toList();
        registeredClients.forEach(client -> log.debug("Registering OIDC client: {}", client));
        return new InMemoryRegisteredClientRepository(registeredClients);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .keyUse(KeyUse.SIGNATURE)
            .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(SecurityProperties properties) {
        return AuthorizationServerSettings.builder()
            .settings(consumer(properties.getServerConfiguration()))
            .build();
    }

    private static Consumer<Map<String, Object>> consumer(Map<String, Object> settings) {
        return stringObjectMap -> {
            if (settings != null) {
                stringObjectMap.putAll(settings);
            }
        };
    }

}
