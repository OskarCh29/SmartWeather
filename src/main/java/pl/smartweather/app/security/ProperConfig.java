package pl.smartweather.app.security;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProperConfigConstraintValidator.class)
public @interface ProperConfig {

    String message() default "Configuration invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
