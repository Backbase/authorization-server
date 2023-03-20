package com.backbase.authorization.ais.authentication;

import com.backbase.authorization.ais.config.AiConsentsProperties;
import com.backbase.authorization.ais.config.AiConsentsProperties.Aspsp;
import com.mastercard.mcob.ais.ApiException;
import com.mastercard.mcob.ais.api.AiConsentsApi;
import com.mastercard.mcob.ais.model.PostAccountsConsentsOKBody;
import com.mastercard.mcob.ais.model.PostAccountsConsentsParamsBody;
import com.mastercard.mcob.ais.model.PostAccountsConsentsParamsBodyRequestInfo;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiConsentRedirectEntryPoint implements AuthenticationEntryPoint {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final AiConsentsApi aiConsentsApi;
    private final AiConsentsProperties properties;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        log.debug("Requesting AI consent initiation via redirect");
        try {
            Aspsp aspsp = getAspsp(request);
            PostAccountsConsentsParamsBody consentRequest = new PostAccountsConsentsParamsBody()
                .permissions(aspsp.getPermissions())
                .validUntilDateTime(OffsetDateTime.now().plus(aspsp.getConsentValidity()))
                .requestInfo(new PostAccountsConsentsParamsBodyRequestInfo()
                    .xRequestId(UUID.randomUUID().toString())
                    .aspspId(aspsp.getId())
                    .tppRedirectURI(buildRedirectURI(request, aspsp.getId()))
                );
            PostAccountsConsentsOKBody consent = aiConsentsApi.getConsent(consentRequest);
            redirectStrategy.sendRedirect(request, response, consent.getLinks().getScaRedirect());
        } catch (ApiException e) {
            throw new IOException(e);
        }
    }

    private Aspsp getAspsp(HttpServletRequest request) {
        String aspspId = request.getParameter(AiConsentsProperties.ASPSP_ID_KEY);
        if (ObjectUtils.isEmpty(aspspId)) {
            Aspsp aspsp = properties.getDefaultAspsp();
            log.debug("Fallback to default ASPSP configuration: {}", aspsp.getId());
            return aspsp;
        }
        log.debug("Getting configuration for ASPSP: {}", aspspId);
        return properties.getAspsps().stream().filter(a -> a.getId().equals(aspspId)).findFirst().orElseThrow();
    }

    private static String buildRedirectURI(HttpServletRequest request, String aspspId) {
        return UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request))
            .replacePath(AiConsentAuthenticationConfigurer.CALLBACK_URL)
            .replaceQuery("")
            .queryParam(AiConsentsProperties.ASPSP_ID_KEY, aspspId)
            .build()
            .toUriString();
    }

}
