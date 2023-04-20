package com.backbase.authorization.security.authentication;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationProvider;
import org.springframework.stereotype.Component;

/**
 * Configures the Authorization Flow authentication provider to use a more permissive validator for redirect uris.
 */
@Component
@ConditionalOnProperty(prefix = "security.authorization.code-flow", name = "permissive-redirect")
public class OAuth2AuthorizationCodeRequestAuthenticationProviderPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ProviderManager providerManager) {
            providerManager.getProviders().stream()
                    .filter(provider -> provider instanceof OAuth2AuthorizationCodeRequestAuthenticationProvider)
                    .findFirst()
                    .ifPresent(provider -> {
                        var authCodeProvider = (OAuth2AuthorizationCodeRequestAuthenticationProvider) provider;
                        authCodeProvider.setAuthenticationValidator(new PermissiveOAuth2AuthorizationCodeRequestAuthenticationValidator());
                    });
        }
        return bean;
    }
}
