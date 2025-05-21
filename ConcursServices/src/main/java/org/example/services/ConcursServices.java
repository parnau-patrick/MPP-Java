package org.example.services;

import org.example.domain.Event;
import org.example.domain.Inscriere;
import org.example.domain.Participant;
import org.example.domain.User;
import org.example.services.dto.EventDTO;
import org.example.services.dto.ParticipantEventDTO;

import java.util.List;
import java.util.Optional;

/**
 * Interface for all Concurs application services
 */
public interface ConcursServices {

    /**
     * Authenticate a user
     * @param username Username
     * @param password Password
     * @return Optional containing the authenticated user if credentials are valid
     * @throws ServiceException if authentication fails
     */
    Optional<User> authenticateUser(String username, String password) throws ServiceException;

    /**
     * Register a new user
     * @param username Username
     * @param password Password
     * @param officeName Office name
     * @return The created user
     * @throws ServiceException if registration fails
     */
    User registerUser(String username, String password, String officeName) throws ServiceException;

    /**
     * Get all events
     * @return List of all events
     * @throws ServiceException if retrieval fails
     */
    List<Event> getAllEvents() throws ServiceException;

    /**
     * Get event by ID
     * @param id Event ID
     * @return Optional containing the event if found
     * @throws ServiceException if retrieval fails
     */
    Optional<Event> getEventById(Integer id) throws ServiceException;

    /**
     * Create a new event
     * @param distance Event distance
     * @param style Event style
     * @return The created event
     * @throws ServiceException if creation fails
     */
    Event createEvent(String distance, String style) throws ServiceException;

    /**
     * Get all events with participant counts
     * @return List of events with participant counts
     * @throws ServiceException if retrieval fails
     */
    List<EventDTO> getAllEventsWithParticipantCounts() throws ServiceException;

    /**
     * Get all participants
     * @return List of all participants
     * @throws ServiceException if retrieval fails
     */
    List<Participant> getAllParticipants() throws ServiceException;

    /**
     * Get participant by ID
     * @param id Participant ID
     * @return Optional containing the participant if found
     * @throws ServiceException if retrieval fails
     */
    Optional<Participant> getParticipantById(Integer id) throws ServiceException;

    /**
     * Register a participant for events
     * @param name Participant name
     * @param age Participant age
     * @param eventIds List of event IDs to register for
     * @return The registration object
     * @throws ServiceException if registration fails
     */
    Inscriere registerParticipant(String name, int age, List<Integer> eventIds) throws ServiceException;

    /**
     * Get all events a participant is registered for
     * @param participantId Participant ID
     * @return List of events the participant is registered for
     * @throws ServiceException if retrieval fails
     */
    List<Event> getEventsByParticipant(Integer participantId) throws ServiceException;

    /**
     * Get all participants registered for an event
     * @param eventId Event ID
     * @return List of participants registered for the event
     * @throws ServiceException if retrieval fails
     */
    List<Participant> getParticipantsByEvent(Integer eventId) throws ServiceException;

    /**
     * Get participant-event data for UI display
     * @param eventId Event ID
     * @return List of participant DTOs with event count information
     * @throws ServiceException if retrieval fails
     */
    List<ParticipantEventDTO> getParticipantsWithEventCount(Integer eventId) throws ServiceException;
}