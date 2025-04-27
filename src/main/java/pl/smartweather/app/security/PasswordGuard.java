package pl.smartweather.app.security;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordGuardConstraintValidator.class)
public @interface PasswordGuard {

    String message() default "Password invalid - no requirements met";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
