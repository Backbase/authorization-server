package com.backbase.authorization.ai.config;

import com.backbase.authorization.ai.model.AiConsentUser;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "open-banking.account-information")
public class AiConsentsApiProperties {

    public static final String ASPSP_ID_KEY = "aspspId";
    public static final String CONSENT_ID_KEY = "consentId";

    private Optional<String> apiBaseUri = Optional.empty();

    @NotEmpty
    private Set<Aspsp> aspsps;

    @Data
    public static class Aspsp {

        @NotNull
        private String id;

        @NotEmpty
        private Set<Consent> consents;
    }

    @Data
    public static class Consent {

        private String id;

        @NotNull
        private AiConsentUser user;
    }

    public Aspsp getDefaultAspsp() {
        return aspsps.stream().findFirst().orElseThrow();
    }

}
