package com.backbase.authorization.authentication;

import com.backbase.authorization.config.AiConsentsApiProperties;
import com.backbase.authorization.model.AiConsentUser;
import com.backbase.authorization.oidc.AttributesAuthenticationToken;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;

public class AiConsentAuthenticationToken extends AbstractAuthenticationToken implements AttributesAuthenticationToken {

    private final Map<String, Object> attributes = new HashMap<>();

    public AiConsentAuthenticationToken(String aspspId, String authorizationQuery) {
        super(null);
        setAuthenticated(false);
        this.attributes.put(AiConsentsApiProperties.ASPSP_ID_KEY, aspspId);
        this.attributes.put(AiConsentsApiProperties.CONSENT_ID_KEY, authorizationQuery);
    }

    public AiConsentAuthenticationToken(String aspspId, String consentId, AiConsentUser user) {
        super(user.getRoles().stream().map(SimpleGrantedAuthority::new).toList());
        setAuthenticated(true);
        this.attributes.put(AiConsentsApiProperties.ASPSP_ID_KEY, aspspId);
        this.attributes.put(AiConsentsApiProperties.CONSENT_ID_KEY, consentId);
        this.attributes.put(StandardClaimNames.PREFERRED_USERNAME, user.getUsername());
    }

    public String getAspspId() {
        return (String) this.attributes.get(AiConsentsApiProperties.ASPSP_ID_KEY);
    }

    @Override
    public String getCredentials() {
        return (String) this.attributes.get(AiConsentsApiProperties.CONSENT_ID_KEY);
    }

    @Override
    public String getPrincipal() {
        return (String) this.attributes.getOrDefault(StandardClaimNames.PREFERRED_USERNAME, null);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.copyOf(attributes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString().replaceAll(".$", ""));
        sb.append(", ");
        sb.append("AspspId=").append(getAspspId());
        sb.append("]");
        return sb.toString();
    }
}
