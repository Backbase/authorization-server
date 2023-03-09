package com.backbase.authserver.authentication;

import com.mastercard.openbanking.accounts.ApiException;
import com.mastercard.openbanking.accounts.api.AiConsentsAuthorizationsApi;
import com.mastercard.openbanking.accounts.models.Merchant;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsAuthOKBody;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsAuthParamsBody;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsAuthParamsBodyRequestInfo;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiConsentAuthenticationProvider implements AuthenticationProvider {

    private final AiConsentsAuthorizationsApi aiConsentsAuthorizationsApi;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AiConsentAuthentication consentAuthentication = (AiConsentAuthentication) authentication;
        String authorization = consentAuthentication.getCredentials();
        try {
            PostAccountsConsentsAuthParamsBody request = new PostAccountsConsentsAuthParamsBody()
                .authorization(authorization)
                .requestInfo(new PostAccountsConsentsAuthParamsBodyRequestInfo()
                    .xRequestId(UUID.randomUUID().toString())
                    .isLivePsuRequest(true)
                    .psuIPAddress("192.168.0.1")
                    .psuAgent("PostmanRuntime/7.20.1")
                    .aspspId("b806ae68-a45b-49d6-b25a-69fdb81dede6")
                    .merchant(new Merchant().id("MerchantId").name("MerchantName")));
            PostAccountsConsentsAuthOKBody authorizations = aiConsentsAuthorizationsApi.getAuthorizations(request);
            return new AiConsentAuthentication("sara", authorizations.getConsentId(),
                AuthorityUtils.createAuthorityList("USER"));
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(AiConsentAuthentication.class);
    }
}
