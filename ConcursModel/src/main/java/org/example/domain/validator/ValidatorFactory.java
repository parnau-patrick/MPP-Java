package org.example.domain.validator;

import org.example.domain.Event;
import org.example.domain.Participant;
import org.example.domain.User;

/**
 * Factory for creating validators
 */
public class ValidatorFactory {

    /**
     * Create a validator for a specific entity type
     * @param type Type of entity to validate
     * @return Validator for the specified entity type
     * @throws IllegalArgumentException If the entity type is not supported
     */
    public static IValidator createValidator(String type) {
        switch (type) {
            case "event":
                return new EventValidator();
            case "participant":
                return new ParticipantValidator();
            case "user":
                return new UserValidator();
            default:
                throw new IllegalArgumentException("Unknown entity type: " + type);
        }
    }
}