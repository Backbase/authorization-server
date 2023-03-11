package com.backbase.authorization.authentication;

import static com.backbase.authorization.authentication.AiConsentAuthenticationConfigurer.CALLBACK_URL;

import com.backbase.authorization.config.AiConsentsProperties;
import com.mastercard.openbanking.accounts.ApiException;
import com.mastercard.openbanking.accounts.api.AiConsentsApi;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsOKBody;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsParamsBody;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsParamsBody.PermissionsEnum;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsParamsBodyRequestInfo;
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
    private final AiConsentsProperties properties;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        log.debug("Requesting AI consent initiation via redirect");
        try {
            String defaultAspspId = properties.getDefaultAspspId(); // TODO: How can I get the aspspId id from the request?
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
            .replacePath(CALLBACK_URL)
            .replaceQuery("")
            .queryParam(AiConsentsProperties.ASPSP_ID_KEY, aspspId)
            .build()
            .toUriString();
    }

}