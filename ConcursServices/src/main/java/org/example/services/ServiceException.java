package org.example.services;

/**
 * Exception thrown by service operations
 */
public class ServiceException extends Exception {

    /**
     * Constructs a new service exception with the specified detail message.
     * @param message the detail message
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new service exception with the specified detail message and cause.
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new service exception with the specified cause.
     * @param cause the cause of the exception
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }
}