package com.backbase.authorization.ais.authentication;

import com.backbase.authorization.ais.config.AiConsentsProperties;
import com.backbase.authorization.ais.model.AiConsentUser;
import com.backbase.authorization.security.token.AttributesAuthenticationToken;
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
        this.attributes.put(AiConsentsProperties.ASPSP_ID_KEY, aspspId);
        this.attributes.put(AiConsentsProperties.CONSENT_ID_KEY, authorizationQuery);
    }

    public AiConsentAuthenticationToken(String aspspId, String consentId, AiConsentUser user) {
        super(user.getRoles().stream().map(SimpleGrantedAuthority::new).toList());
        setAuthenticated(true);
        this.attributes.put(AiConsentsProperties.ASPSP_ID_KEY, aspspId);
        this.attributes.put(AiConsentsProperties.CONSENT_ID_KEY, consentId);
        this.attributes.put(StandardClaimNames.PREFERRED_USERNAME, user.getUsername());
    }

    public String getAspspId() {
        return (String) this.attributes.get(AiConsentsProperties.ASPSP_ID_KEY);
    }

    @Override
    public String getCredentials() {
        return (String) this.attributes.get(AiConsentsProperties.CONSENT_ID_KEY);
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
