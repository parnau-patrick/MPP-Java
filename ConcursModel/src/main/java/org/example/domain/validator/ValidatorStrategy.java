package org.example.domain.validator;

/**
 * Strategy interface for domain entity validation
 * @param <T> Entity type to validate
 */
public interface ValidatorStrategy<T> {

    /**
     * Validate an entity
     * @param entity Entity to validate
     * @throws org.example.exception.ValidationException If validation fails
     */
    void validate(T entity);
}