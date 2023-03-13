package com.backbase.authorization.ais.authentication;

import com.backbase.authorization.ais.model.AiConsentUser;
import com.backbase.authorization.ais.repository.AiConsentUsersRepository;
import com.mastercard.mcob.ais.ApiException;
import com.mastercard.mcob.ais.api.AiConsentsAuthorizationsApi;
import com.mastercard.mcob.ais.model.PostAccountsConsentsAuthOKBody;
import com.mastercard.mcob.ais.model.PostAccountsConsentsAuthParamsBody;
import com.mastercard.mcob.ais.model.PostAccountsConsentsAuthParamsBodyRequestInfo;
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
