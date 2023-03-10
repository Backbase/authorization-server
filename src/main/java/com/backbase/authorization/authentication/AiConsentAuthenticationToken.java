package com.backbase.authorization.authentication;

import com.backbase.authorization.model.AiConsentUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AiConsentAuthenticationToken extends AbstractAuthenticationToken {

    private final String aspspId;
    private final String credentials;
    private final String username;

    public AiConsentAuthenticationToken(String aspspId, String authorizationQuery) {
        super(null);
        setAuthenticated(false);
        this.aspspId = aspspId;
        this.credentials = authorizationQuery;
        this.username = null;
    }

    public AiConsentAuthenticationToken(String aspspId, String consentId, AiConsentUser user) {
        super(user.getRoles().stream().map(SimpleGrantedAuthority::new).toList());
        setAuthenticated(true);
        this.aspspId = aspspId;
        this.credentials = consentId;
        this.username = user.getUsername();
    }

    public String getAspspId() {
        return this.aspspId;
    }

    @Override
    public String getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.username;
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
