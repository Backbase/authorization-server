package com.backbase.authserver.authentication;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class AiConsentAuthenticationConfigurer<H extends HttpSecurityBuilder<H>> extends
    AbstractAuthenticationFilterConfigurer<H, AiConsentAuthenticationConfigurer<H>, AiConsentAuthenticationFilter> {

    public static final String CALLBACK_URL = "/callback";

    public AiConsentAuthenticationConfigurer() {
        super(new AiConsentAuthenticationFilter(), CALLBACK_URL);
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(CALLBACK_URL);
    }
}
