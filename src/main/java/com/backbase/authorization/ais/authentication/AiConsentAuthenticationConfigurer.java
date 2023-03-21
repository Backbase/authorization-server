package com.backbase.authorization.ais.authentication;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Component;

@Component
public class AiConsentAuthenticationConfigurer extends
    AbstractHttpConfigurer<AiConsentAuthenticationConfigurer, HttpSecurity> {

    public static final String CALLBACK_URL = "/callback";

    private final AiConsentCallbackFilter authFilter;
    private final AiConsentRedirectEntryPoint authenticationEntryPoint;

    public AiConsentAuthenticationConfigurer(AiConsentRedirectEntryPoint authenticationEntryPoint) {
        this.authFilter = new AiConsentCallbackFilter(CALLBACK_URL);
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    public void init(HttpSecurity builder) throws Exception {
        ExceptionHandlingConfigurer exceptionHandling = builder.getConfigurer(ExceptionHandlingConfigurer.class);
        if (exceptionHandling == null) {
            return;
        }
        exceptionHandling.authenticationEntryPoint(postProcess(authenticationEntryPoint));
        super.init(builder);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        this.authFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        SessionAuthenticationStrategy sessionAuthenticationStrategy = http.getSharedObject(
            SessionAuthenticationStrategy.class);
        if (sessionAuthenticationStrategy != null) {
            this.authFilter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);
        }
        RememberMeServices rememberMeServices = http.getSharedObject(RememberMeServices.class);
        if (rememberMeServices != null) {
            this.authFilter.setRememberMeServices(rememberMeServices);
        }
        AiConsentCallbackFilter filter = postProcess(this.authFilter);
        http.addFilterAt(filter, UsernamePasswordAuthenticationFilter.class);
        super.configure(http);
    }
}
