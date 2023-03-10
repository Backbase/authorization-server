package com.backbase.authserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mastercard.connect")
public class AiConsentProperties {

    public static final String DEFAULT_ASPSP_ID = "b806ae68-a45b-49d6-b25a-69fdb81dede6";

    String baseUri = "https://developer.mastercard.com/apigwproxy/openbanking/connect/api";

    String defaultAspspId = DEFAULT_ASPSP_ID;

}
