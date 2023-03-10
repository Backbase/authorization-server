package com.backbase.authorization.config;

import com.backbase.authorization.model.AiConsentUser;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mastercard.connect")
public class AiConsentsProperties {

    public static final String ASPSP_ID_KEY = "aspspId";
    public static final String CONSENT_ID_KEY = "consentId";

    String baseUri = "https://developer.mastercard.com/apigwproxy/openbanking/connect/api";

    private Map<String, Aspsp> aspsps = Map.of("b806ae68-a45b-49d6-b25a-69fdb81dede6", new Aspsp());

    @Data
    public static class Aspsp {

        private AiConsentUser defaultUser = new AiConsentUser("sara", List.of("USER"));

        private Map<String, Consent> consents;
    }

    @Data
    public static class Consent {

        private AiConsentUser user;
    }

    public String getDefaultAspspId() {
        return aspsps.entrySet().stream().findFirst().get().getKey();
    }

}
