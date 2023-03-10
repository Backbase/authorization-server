package com.backbase.authserver.authentication;

import static com.backbase.authserver.authentication.AiConsentAuthenticationConfigurer.CALLBACK_URL;

import com.backbase.authserver.config.AiConsentProperties;
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
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class AiConsentAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String ASPSP_ID_PARAM = "aspspId";

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final AiConsentsApi aiConsentsApi;
    private final AiConsentProperties properties;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        try {
            PostAccountsConsentsParamsBody consentRequest = new PostAccountsConsentsParamsBody()
                .permissions(List.of(PermissionsEnum.ALLPSD2))
                .validUntilDateTime(OffsetDateTime.now().plusDays(1))
                .requestInfo(new PostAccountsConsentsParamsBodyRequestInfo()
                    .xRequestId(UUID.randomUUID().toString())
                    .aspspId(properties.getDefaultAspspId())
                    .tppRedirectURI(buildRedirectURI(request, properties.getDefaultAspspId()))
                );
            PostAccountsConsentsOKBody consent = aiConsentsApi.getConsent(consentRequest);
            redirectStrategy.sendRedirect(request, response, consent.getLinks().getScaRedirect());
        } catch (ApiException e) {
            throw new IOException(e);
        }
    }

    private String buildRedirectURI(HttpServletRequest request, String aspspId) {
        return UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request))
            .replacePath(CALLBACK_URL)
            .replaceQuery("")
            .queryParam(ASPSP_ID_PARAM, aspspId)
            .build()
            .toUriString();
    }

}
