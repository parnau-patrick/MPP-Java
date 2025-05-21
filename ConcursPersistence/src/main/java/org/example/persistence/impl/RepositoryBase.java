package org.example.persistence.impl;

import org.example.observer.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Base repository class with observer pattern implementation
 */
public abstract class RepositoryBase {
    private final List<Observer> observers = new ArrayList<>();

    /**
     * Add an observer to be notified of changes
     * @param observer Observer to add
     */
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    /**
     * Remove an observer
     * @param observer Observer to remove
     */
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Notify all observers of changes
     */
    protected void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}