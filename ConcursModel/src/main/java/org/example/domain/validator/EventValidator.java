package org.example.domain.validator;

import org.example.domain.Event;
import org.example.exception.ValidationException;

/**
 * Validator for Event entities
 */
public class EventValidator implements IValidator<Event> {

    private final ValidatorStrategy<Event> distanceValidator = entity -> {
        if (entity.getDistance() == null || entity.getDistance().isEmpty()) {
            throw new ValidationException("Distance cannot be empty");
        }

        try {
            int distance = Integer.parseInt(entity.getDistance());
            if (distance <= 0) {
                throw new ValidationException("Distance must be a positive number");
            }

            // Check for valid distances: 50m, 200m, 800m, 1500m
            if (distance != 50 && distance != 200 && distance != 800 && distance != 1500) {
                throw new ValidationException("Distance must be 50m, 200m, 800m, or 1500m");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Distance must be a valid number");
        }
    };

    private final ValidatorStrategy<Event> styleValidator = entity -> {
        if (entity.getStyle() == null || entity.getStyle().isEmpty()) {
            throw new ValidationException("Style cannot be empty");
        }

        String style = entity.getStyle().toLowerCase();
        if (!style.equals("liber") && !style.equals("spate") &&
                !style.equals("fluture") && !style.equals("mixt")) {
            throw new ValidationException("Style must be one of: liber, spate, fluture, mixt");
        }
    };

    @Override
    public void validate(Event entity) {
        if (entity == null) {
            throw new ValidationException("Event cannot be null");
        }

        distanceValidator.validate(entity);
        styleValidator.validate(entity);
    }
}