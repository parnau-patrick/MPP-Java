package org.example.persistence.interfaces;

import org.example.domain.Event;
import org.example.domain.Inscriere;
import org.example.domain.Participant;
import org.example.observer.Observer;

import java.util.List;

/**
 * Repository interface for Inscriere (registration) entities
 */
public interface IInscriereRepository {

    /**
     * Find all events a participant is registered for
     * @param participantId Participant ID
     * @return List of events the participant is registered for
     */
    Iterable<Event> findAllEventsByParticipant(Integer participantId);

    /**
     * Find all participants registered for an event
     * @param eventId Event ID
     * @return List of participants registered for the event
     */
    Iterable<Participant> findAllParticipantsByEvent(Integer eventId);

    /**
     * Associate an event with a participant (register participant for event)
     * @param eventId Event ID
     * @param participantId Participant ID
     */
    void saveEventParticipant(Integer eventId, Integer participantId);

    /**
     * Remove an event-participant association
     * @param eventId Event ID
     * @param participantId Participant ID
     */
    void deleteEventParticipant(Integer eventId, Integer participantId);

    /**
     * Save a complete registration (participant with multiple events)
     * @param inscriere Registration to save
     */
    void save(Inscriere inscriere);

    /**
     * Find all registrations
     * @return List of all registrations
     */
    List<Inscriere> findAll();

    /**
     * Add observer for notifications when repository changes
     * @param observer Observer to add
     */
    void addObserver(Observer observer);

    /**
     * Remove observer
     * @param observer Observer to remove
     */
    void removeObserver(Observer observer);
}