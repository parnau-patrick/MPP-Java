package org.example.domain.validator;

import org.example.domain.Participant;
import org.example.exception.ValidationException;

/**
 * Validator for Participant entities
 */
public class ParticipantValidator implements IValidator<Participant> {

    private final ValidatorStrategy<Participant> nameValidator = entity -> {
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            throw new ValidationException("Name cannot be empty");
        }

        if (entity.getName().length() < 2) {
            throw new ValidationException("Name must be at least 2 characters long");
        }

        if (!entity.getName().matches("[a-zA-Z\\s-]+")) {
            throw new ValidationException("Name must contain only letters, spaces and hyphens");
        }
    };

    private final ValidatorStrategy<Participant> ageValidator = entity -> {
        if (entity.getAge() == null) {
            throw new ValidationException("Age cannot be null");
        }

        if (entity.getAge() < 5) {
            throw new ValidationException("Age must be at least 5 years");
        }

        if (entity.getAge() > 100) {
            throw new ValidationException("Age must be at most 100 years");
        }
    };

    @Override
    public void validate(Participant entity) {
        if (entity == null) {
            throw new ValidationException("Participant cannot be null");
        }

        nameValidator.validate(entity);
        ageValidator.validate(entity);
    }
}