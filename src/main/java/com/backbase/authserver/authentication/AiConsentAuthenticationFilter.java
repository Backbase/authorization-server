package com.backbase.authserver.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class AiConsentAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        String authorization = request.getQueryString();
        AiConsentAuthentication consentAuthentication = new AiConsentAuthentication(authorization);
        return this.getAuthenticationManager().authenticate(consentAuthentication);
    }
}
