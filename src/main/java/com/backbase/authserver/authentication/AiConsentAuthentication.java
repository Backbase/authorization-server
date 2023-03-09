package com.backbase.authserver.authentication;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class AiConsentAuthentication extends AbstractAuthenticationToken {

    private final String username;
    private final String credentials;

    public AiConsentAuthentication(String authorization) {
        super(null);
        setAuthenticated(false);
        this.credentials = authorization;
        this.username = null;
    }

    public AiConsentAuthentication(String username, String consentId,
        Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        setAuthenticated(true);
        this.credentials = consentId;
        this.username = username;
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
