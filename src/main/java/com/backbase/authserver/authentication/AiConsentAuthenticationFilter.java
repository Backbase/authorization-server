package com.backbase.authserver.authentication;

import static com.backbase.authserver.authentication.AiConsentAuthenticationEntryPoint.ASPSP_ID_PARAM;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class AiConsentAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    protected AiConsentAuthenticationFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        UriComponents uri = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build();
        String aspspId = uri.getQueryParams().getFirst(ASPSP_ID_PARAM);
        String authorizationQuery = uri.getQuery();
        AiConsentAuthentication consentAuthentication = new AiConsentAuthentication(aspspId, authorizationQuery);
        return this.getAuthenticationManager().authenticate(consentAuthentication);
    }
}
