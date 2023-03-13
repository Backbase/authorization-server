package com.backbase.authorization.ai.authentication;

import com.backbase.authorization.ai.config.AiConsentsApiProperties;
import com.mastercard.openbanking.ai.ApiException;
import com.mastercard.openbanking.ai.api.AiConsentsApi;
import com.mastercard.openbanking.ai.models.PostAccountsConsentsOKBody;
import com.mastercard.openbanking.ai.models.PostAccountsConsentsParamsBody;
import com.mastercard.openbanking.ai.models.PostAccountsConsentsParamsBody.PermissionsEnum;
import com.mastercard.openbanking.ai.models.PostAccountsConsentsParamsBodyRequestInfo;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
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
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiConsentRedirectEntryPoint implements AuthenticationEntryPoint {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final AiConsentsApi aiConsentsApi;
    private final AiConsentsApiProperties properties;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        log.debug("Requesting AI consent initiation via redirect");
        try {
            String defaultAspspId = properties.getDefaultAspsp().getId(); // TODO: How can I get the aspspId id from the request?
            PostAccountsConsentsParamsBody consentRequest = new PostAccountsConsentsParamsBody()
                .permissions(List.of(PermissionsEnum.ALLPSD2))
                .validUntilDateTime(OffsetDateTime.now().plusDays(1))
                .requestInfo(new PostAccountsConsentsParamsBodyRequestInfo()
                    .xRequestId(UUID.randomUUID().toString())
                    .aspspId(defaultAspspId)
                    .tppRedirectURI(buildRedirectURI(request, defaultAspspId))
                );
            PostAccountsConsentsOKBody consent = aiConsentsApi.getConsent(consentRequest);
            redirectStrategy.sendRedirect(request, response, consent.getLinks().getScaRedirect());
        } catch (ApiException e) {
            throw new IOException(e);
        }
    }

    private static String buildRedirectURI(HttpServletRequest request, String aspspId) {
        return UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request))
            .replacePath(AiConsentAuthenticationConfigurer.CALLBACK_URL)
            .replaceQuery("")
            .queryParam(AiConsentsApiProperties.ASPSP_ID_KEY, aspspId)
            .build()
            .toUriString();
    }

}
