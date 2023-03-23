package com.backbase.authorization.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = AllowedRedirectUriValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedRedirectUris {

    String message() default "One or more invalid redirect uris";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
