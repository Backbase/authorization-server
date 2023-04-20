package com.backbase.authorization.validator;

import java.util.Collection;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class AllowedRedirectUriValidator implements ConstraintValidator<AllowedRedirectUris, Collection<String>> {

    @Value("${security.authorization.code-flow.permissive-redirect:false}")
    private boolean permissiveRedirect;

    public boolean isValidHost(String host, RedirectTarget target) {
        switch (target) {
            case CLIENT -> {
                return !(ObjectUtils.isEmpty(host)
                    || !permissiveRedirect
                    || host.equals("localhost"));
            }
            case CALLBACK -> {
                return !(ObjectUtils.isEmpty(host)
                    || host.equals("localhost")
                    || host.startsWith("127.")
                    || host.startsWith("192."));
            }
        }
        return false;
    }

    @Override
    public boolean isValid(Collection<String> collection, ConstraintValidatorContext context) {
        if (collection == null) {
            return false;
        } else {
            return collection.stream()
                .map(uri -> UriComponentsBuilder.fromUriString(uri).build().getHost())
                .allMatch(host -> isValidHost(host, RedirectTarget.CLIENT));
        }
    }

    public enum RedirectTarget {
        /**
         * Used to validate the server callback redirect uris supported by Mastercard.
         */
        CALLBACK,
        /**
         * Used to validate the client redirect uris supported by the Spring OAuth2 Server.
         */
        CLIENT
    }

}
