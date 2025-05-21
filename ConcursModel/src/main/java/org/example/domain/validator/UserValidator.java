package org.example.domain.validator;

import org.example.domain.User;
import org.example.exception.ValidationException;

/**
 * Validator for User entities
 */
public class UserValidator implements IValidator<User> {

    private final ValidatorStrategy<User> usernameValidator = entity -> {
        if (entity.getUsername() == null || entity.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }

        if (entity.getUsername().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long");
        }
    };

    private final ValidatorStrategy<User> passwordValidator = entity -> {
        if (entity.getPassword() == null || entity.getPassword().trim().isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }

        if (entity.getPassword().length() < 4) {
            throw new ValidationException("Password must be at least 4 characters long");
        }
    };

    private final ValidatorStrategy<User> officeNameValidator = entity -> {
        if (entity.getOfficeName() == null || entity.getOfficeName().trim().isEmpty()) {
            throw new ValidationException("Office name cannot be empty");
        }
    };

    @Override
    public void validate(User entity) {
        if (entity == null) {
            throw new ValidationException("User cannot be null");
        }

        usernameValidator.validate(entity);
        passwordValidator.validate(entity);
        officeNameValidator.validate(entity);
    }
}