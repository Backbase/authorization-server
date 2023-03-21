package com.backbase.authorization.ais.authentication;

import org.springframework.beans.factory.annotation.Autowired;
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

    private AiConsentCallbackFilter authFilter;
    private AiConsentRedirectEntryPoint authenticationEntryPoint;

    public AiConsentAuthenticationConfigurer() {
        authFilter(new AiConsentCallbackFilter(CALLBACK_URL));
    }

    @Autowired
    public AiConsentAuthenticationConfigurer(AiConsentRedirectEntryPoint authenticationEntryPoint) {
        this();
        authenticationEntryPoint(authenticationEntryPoint);
    }

    public AiConsentAuthenticationConfigurer authFilter(AiConsentCallbackFilter authFilter) {
        this.authFilter = authFilter;
        return this;
    }

    public AiConsentAuthenticationConfigurer authenticationEntryPoint(
        AiConsentRedirectEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        return this;
    }

    @Override
    public void init(HttpSecurity builder) throws Exception {
        if (this.authenticationEntryPoint != null) {
            ExceptionHandlingConfigurer exceptionHandling = builder.getConfigurer(ExceptionHandlingConfigurer.class);
            if (exceptionHandling == null) {
                return;
            }
            exceptionHandling.authenticationEntryPoint(postProcess(authenticationEntryPoint));
        }
        super.init(builder);
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        this.authFilter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager.class));
        SessionAuthenticationStrategy sessionAuthenticationStrategy = builder.getSharedObject(
            SessionAuthenticationStrategy.class);
        if (sessionAuthenticationStrategy != null) {
            this.authFilter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);
        }
        RememberMeServices rememberMeServices = builder.getSharedObject(RememberMeServices.class);
        if (rememberMeServices != null) {
            this.authFilter.setRememberMeServices(rememberMeServices);
        }
        AiConsentCallbackFilter filter = postProcess(this.authFilter);
        builder.addFilterAt(filter, UsernamePasswordAuthenticationFilter.class);
        super.configure(builder);
    }
}
