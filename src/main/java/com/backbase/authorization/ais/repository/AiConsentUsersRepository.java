package com.backbase.authorization.ais.repository;

import com.backbase.authorization.ais.config.AiConsentsProperties;
import com.backbase.authorization.ais.config.AiConsentsProperties.Aspsp;
import com.backbase.authorization.ais.model.AiConsentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiConsentUsersRepository {

    private final AiConsentsProperties properties;

    public AiConsentUser findAspspUserByConsentId(String aspspId, String consentId) {
        Aspsp aspsp = properties.getAspsps().stream().filter(a -> aspspId.equals(a.getId())).findFirst()
            .orElse(properties.getDefaultAspsp());

        return aspsp.getAvailableConsents().stream().filter(c -> consentId.equals(c.getId())).findFirst()
            .orElse(aspsp.getAvailableConsents().stream().findFirst().orElseThrow()).getUser();
    }

}
