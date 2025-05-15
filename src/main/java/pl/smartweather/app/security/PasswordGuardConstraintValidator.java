package pl.smartweather.app.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordGuardConstraintValidator implements ConstraintValidator<PasswordGuard, String> {

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d).+$";
    private static final int PASSWORD_MINIMUM_LENGTH = 6;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext.buildConstraintViolationWithTemplate(
                "Password not valid - Check Requirements").addConstraintViolation();
        return password != null
                && password.length() >= PASSWORD_MINIMUM_LENGTH
                && Pattern.matches(PASSWORD_PATTERN, password);
    }

    @Override
    public void initialize(PasswordGuard constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
