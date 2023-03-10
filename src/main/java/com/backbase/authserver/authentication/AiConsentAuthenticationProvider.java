package com.backbase.authserver.authentication;

import com.mastercard.openbanking.accounts.ApiException;
import com.mastercard.openbanking.accounts.api.AiConsentsAuthorizationsApi;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsAuthOKBody;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsAuthParamsBody;
import com.mastercard.openbanking.accounts.models.PostAccountsConsentsAuthParamsBodyRequestInfo;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
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
        try {
            AiConsentAuthentication consentAuthentication = (AiConsentAuthentication) authentication;
            PostAccountsConsentsAuthParamsBody request = new PostAccountsConsentsAuthParamsBody()
                .authorization(consentAuthentication.getCredentials())
                .requestInfo(new PostAccountsConsentsAuthParamsBodyRequestInfo()
                    .xRequestId(UUID.randomUUID().toString())
                    .aspspId(consentAuthentication.getAspspId()));
            PostAccountsConsentsAuthOKBody authorizations = aiConsentsAuthorizationsApi.getAuthorizations(request);
            return new AiConsentAuthentication(consentAuthentication.getAspspId(), authorizations.getConsentId(),
                "sara", AuthorityUtils.createAuthorityList("USER")); // TODO: Create strategy to dynamically obtain the username and authorities.
        } catch (ApiException e) {
            throw new BadCredentialsException(e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(AiConsentAuthentication.class);
    }
}
