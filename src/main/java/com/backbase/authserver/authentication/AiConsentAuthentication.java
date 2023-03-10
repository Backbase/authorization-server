package com.backbase.authserver.authentication;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class AiConsentAuthentication extends AbstractAuthenticationToken {

    private final String aspspId;
    private final String credentials;
    private final String username;

    public AiConsentAuthentication(String aspspId, String authorizationQuery) {
        super(null);
        setAuthenticated(false);
        this.aspspId = aspspId;
        this.credentials = authorizationQuery;
        this.username = null;
    }

    public AiConsentAuthentication(String aspspId, String consentId, String username,
        Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        setAuthenticated(true);
        this.aspspId = aspspId;
        this.credentials = consentId;
        this.username = username;
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
}
