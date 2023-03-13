package com.backbase.authorization.ais.config;

import com.backbase.authorization.ais.model.AiConsentUser;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "mastercard.mcob.ais")
public class AiConsentsProperties {

    public static final String ASPSP_ID_KEY = "aspspId";
    public static final String CONSENT_ID_KEY = "consentId";

    private Api api = new Api();

    @NotEmpty
    private Set<Aspsp> aspsps;

    public Aspsp getDefaultAspsp() {
        return aspsps.stream().findFirst().orElseThrow();
    }

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

    @Data
    public static class Api {

        Optional<String> baseUri = Optional.empty();
        Proxy proxy = new Proxy();
    }

    @Data
    public static class Proxy {

        Boolean enabled = false;
        String host;
        Integer port;
    }

}
