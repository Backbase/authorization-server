package com.backbase.authorization.ais.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class AiConsentAuthenticationConfigurer extends
        AbstractHttpConfigurer<AiConsentAuthenticationConfigurer, HttpSecurity> {

    public static final String DEFAULT_CALLBACK_URL = "/callback";

    private AiConsentCallbackFilter authFilter;
    private AiConsentAuthenticationProvider authenticationProvider;
    private AiConsentRedirectEntryPoint authenticationEntryPoint;
    private String callbackUrl;

    public AiConsentAuthenticationConfigurer() {
        authFilter(new AiConsentCallbackFilter(DEFAULT_CALLBACK_URL));
    }

    @Autowired
    public AiConsentAuthenticationConfigurer(AiConsentRedirectEntryPoint authenticationEntryPoint,
                                             AiConsentAuthenticationProvider authenticationProvider) {
        this();
        authenticationEntryPoint(authenticationEntryPoint);
        authenticationProvider(authenticationProvider);
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

    public AiConsentAuthenticationConfigurer authenticationProvider(
            AiConsentAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
        return this;
    }

    public AiConsentAuthenticationConfigurer callbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
        return this;
    }

    @Override
    public void init(HttpSecurity builder) throws Exception {
        Assert.notNull(this.authenticationEntryPoint, "AiConsentRedirectEntryPoint is not provided.");
        if (callbackUrl != null) {
            authFilter.setFilterProcessesUrl(callbackUrl);
            authenticationEntryPoint.setCallbackPath(callbackUrl);
        }
        ExceptionHandlingConfigurer exceptionHandling = builder.getConfigurer(ExceptionHandlingConfigurer.class);
        if (exceptionHandling == null) {
            return;
        }
        exceptionHandling.authenticationEntryPoint(postProcess(authenticationEntryPoint));
        super.init(builder);
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        Assert.notNull(this.authenticationProvider, "AiConsentAuthenticationProvider is not provided.");
        this.authFilter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager.class));
        SessionAuthenticationStrategy sessionAuthenticationStrategy = builder.getSharedObject(
                SessionAuthenticationStrategy.class);
        if (sessionAuthenticationStrategy != null) {
            this.authFilter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);
        }
        builder.authenticationProvider(postProcess(this.authenticationProvider));
        builder.addFilterAt(postProcess(this.authFilter), UsernamePasswordAuthenticationFilter.class);
        super.configure(builder);
    }
}
