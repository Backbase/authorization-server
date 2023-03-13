package com.backbase.authorization.authentication;

import com.backbase.authorization.model.AiConsentUser;
import com.backbase.authorization.repository.AiConsentUsersRepository;
import com.mastercard.openbanking.ai.ApiException;
import com.mastercard.openbanking.ai.api.AiConsentsAuthorizationsApi;
import com.mastercard.openbanking.ai.models.PostAccountsConsentsAuthOKBody;
import com.mastercard.openbanking.ai.models.PostAccountsConsentsAuthParamsBody;
import com.mastercard.openbanking.ai.models.PostAccountsConsentsAuthParamsBodyRequestInfo;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiConsentAuthenticationProvider implements AuthenticationProvider {

    private final AiConsentsAuthorizationsApi aiConsentsAuthorizationsApi;
    private final AiConsentUsersRepository usersRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.debug("Exchanging the authorization for access consent");
        try {
            AiConsentAuthenticationToken consentToken = (AiConsentAuthenticationToken) authentication;
            PostAccountsConsentsAuthParamsBody request = new PostAccountsConsentsAuthParamsBody()
                .authorization(consentToken.getCredentials())
                .requestInfo(new PostAccountsConsentsAuthParamsBodyRequestInfo()
                    .xRequestId(UUID.randomUUID().toString())
                    .aspspId(consentToken.getAspspId()));
            PostAccountsConsentsAuthOKBody authorization = aiConsentsAuthorizationsApi.getAuthorizations(request);
            AiConsentUser user = usersRepository.findAspspUserByConsentId(
                consentToken.getAspspId(), authorization.getConsentId());
            return new AiConsentAuthenticationToken(consentToken.getAspspId(), authorization.getConsentId(), user);
        } catch (ApiException e) {
            throw new BadCredentialsException(e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(AiConsentAuthenticationToken.class);
    }
}
