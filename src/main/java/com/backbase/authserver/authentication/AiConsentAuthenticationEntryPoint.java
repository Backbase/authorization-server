package com.backbase.authserver.authentication;

import com.mastercard.openbanking.accounts.ApiException;
import com.mastercard.openbanking.accounts.api.AiConsentsApi;
import com.mastercard.openbanking.accounts.models.Merchant;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsOKBody;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsParamsBody;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsParamsBody.PermissionsEnum;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsParamsBodyRequestInfo;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiConsentAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private final AiConsentsApi aiConsentsApi;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        try {
            PostAccountsConsentsParamsBody consentRequest = new PostAccountsConsentsParamsBody()
                .permissions(List.of(PermissionsEnum.ALLPSD2))
                .validUntil(LocalDate.now().plusDays(1))
                .validUntilDateTime(OffsetDateTime.now().plusDays(1))
                .requestInfo(new PostAccountsConsentsParamsBodyRequestInfo()
                    .xRequestId(UUID.randomUUID().toString())
                    .tppRedirectURI("http://host.docker.internal:8080/callback")
                    .isLivePsuRequest(true)
                    .psuIPAddress("192.168.0.1")
                    .psuAgent("PostmanRuntime/7.20.1")
                    .aspspId("b806ae68-a45b-49d6-b25a-69fdb81dede6")
                    .psuTppCustomerId("b806ae68-a45b-49d6-b25a-69fdb81dede6")
                    .merchant(new Merchant().id("MerchantId").name("MerchantName"))
                    .credentials(Map.of("iban", "DE357543513"))
                );
            PostAccountsConsentsOKBody consent = aiConsentsApi.getConsent(consentRequest);
            redirectStrategy.sendRedirect(request, response, consent.getLinks().getScaRedirect());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

}
