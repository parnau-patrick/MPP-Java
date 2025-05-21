package org.example.persistence.interfaces;

import org.example.domain.Participant;
import org.example.persistence.Repository;

/**
 * Repository interface for Participant entities
 */
public interface IParticipantRepository extends Repository<Integer, Participant> {
    // Add participant-specific repository methods here if needed
}