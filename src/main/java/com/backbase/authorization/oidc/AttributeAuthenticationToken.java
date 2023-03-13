package com.backbase.authorization.oidc;


import java.util.Map;
import org.springframework.security.core.Authentication;

public interface AttributeAuthenticationToken extends Authentication {

    Map<String, Object> getAttributes();

}
