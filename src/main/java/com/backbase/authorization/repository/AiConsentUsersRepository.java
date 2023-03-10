package com.backbase.authorization.repository;

import com.backbase.authorization.config.AiConsentsProperties;
import com.backbase.authorization.config.AiConsentsProperties.Aspsp;
import com.backbase.authorization.model.AiConsentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiConsentUsersRepository {

    private final AiConsentsProperties properties;

    public AiConsentUser findAspspUserByConsentId(String aspspId, String consentId) {
        Aspsp aspsp = properties.getAspsps().get(aspspId);
        if (aspsp.getConsents() != null && aspsp.getConsents().containsKey(consentId)) {
            return aspsp.getConsents().get(consentId).getUser();
        }
        return aspsp.getDefaultUser();
    }

}
