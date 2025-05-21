package org.example.observer;

/**
 * Observer interface for repository update notifications
 * This allows UI components to be updated when data changes
 */
public interface Observer {

    /**
     * Called when observed data has changed
     */
    void update();
}