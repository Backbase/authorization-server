package com.backbase.authorization.authentication;

import com.backbase.authorization.config.AiConsentsProperties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class AiConsentCallbackFilter extends AbstractAuthenticationProcessingFilter {

    protected AiConsentCallbackFilter(String callbackUrl) {
        super(callbackUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        log.debug("Processing AI consent callback request.");
        UriComponents uri = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build();
        String aspspId = uri.getQueryParams().getFirst(AiConsentsProperties.ASPSP_ID_KEY);
        String authorizationQuery = uri.getQuery();
        AiConsentAuthenticationToken consentAuthentication = new AiConsentAuthenticationToken(aspspId,
            authorizationQuery);
        consentAuthentication.setDetails(this.authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(consentAuthentication);
    }
}
