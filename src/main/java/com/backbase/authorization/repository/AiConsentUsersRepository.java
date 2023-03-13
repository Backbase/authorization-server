package com.backbase.authorization.repository;

import com.backbase.authorization.config.AiConsentsApiProperties;
import com.backbase.authorization.config.AiConsentsApiProperties.Aspsp;
import com.backbase.authorization.model.AiConsentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiConsentUsersRepository {

    private final AiConsentsApiProperties properties;

    public AiConsentUser findAspspUserByConsentId(String aspspId, String consentId) {
        Aspsp aspsp = properties.getAspsps().stream().filter(a -> aspspId.equals(a.getId())).findFirst()
            .orElse(properties.getDefaultAspsp());

        return aspsp.getConsents().stream().filter(c -> consentId.equals(c.getId())).findFirst()
            .orElse(aspsp.getConsents().stream().findFirst().orElseThrow()).getUser();
    }

}
