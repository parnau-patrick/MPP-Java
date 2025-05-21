package org.example.persistence.interfaces;

import org.example.domain.Event;
import org.example.persistence.Repository;

/**
 * Repository interface for Event entities
 */
public interface IEventRepository extends Repository<Integer, Event> {
    // Add event-specific repository methods here if needed
}