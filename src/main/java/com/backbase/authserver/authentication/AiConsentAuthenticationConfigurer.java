package com.backbase.authserver.authentication;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class AiConsentAuthenticationConfigurer extends
    AbstractAuthenticationFilterConfigurer<HttpSecurity, AiConsentAuthenticationConfigurer, AiConsentAuthenticationFilter> {

    public static final String CALLBACK_URL = "/callback";

    public AiConsentAuthenticationConfigurer() {
        super(new AiConsentAuthenticationFilter(CALLBACK_URL), CALLBACK_URL);
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.addFilterAt(super.getAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        super.configure(http);
    }
}
