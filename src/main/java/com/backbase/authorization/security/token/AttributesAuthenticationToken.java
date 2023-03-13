package com.backbase.authorization.security.token;


import java.util.Map;
import org.springframework.security.core.Authentication;

public interface AttributesAuthenticationToken extends Authentication {

    Map<String, Object> getAttributes();

}
