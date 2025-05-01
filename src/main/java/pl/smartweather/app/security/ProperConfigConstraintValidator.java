package pl.smartweather.app.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProperConfigConstraintValidator implements ConstraintValidator<ProperConfig, Map<String, String>> {

    private static final int LOWEST_PORT = 0;
    private static final int HIGHEST_PORT = 65535;
    private static final int CONFIG_SIZE = 5;

    @Override
    public boolean isValid(Map<String, String> config, ConstraintValidatorContext context) {
        if (config == null || config.isEmpty() || config.size() < CONFIG_SIZE) {
            addViolation(context, "Configuration missing");
            return false;
        }
        Set<String> requiredKeys = new HashSet<>(
                Arrays.asList("mail_host", "mail_port", "mail_name", "mail_pass", "api_key"));

        boolean isValid = true;

        for (String key : requiredKeys) {
            String value = config.get(key);
            if (value == null || value.trim().isEmpty()) {
                addViolation(context, "Configuration missing: " + key);
                isValid = false;
            }
            String portValue = config.get("mail_port");
            if (portValue != null && !portValue.trim().isEmpty()) {
                try {
                    int port = Integer.parseInt(portValue.trim());
                    if (port <= LOWEST_PORT || port > HIGHEST_PORT) {
                        addViolation(context, "Invalid mail_port value");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    addViolation(context, "mail_port must be a number");
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    @Override
    public void initialize(ProperConfig constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
