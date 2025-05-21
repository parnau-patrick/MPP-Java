package org.example.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a registration of a participant for events
 */
public class Inscriere extends Entity<Integer> {
    private Participant participant;
    private List<Event> events = new ArrayList<>();

    public Inscriere() {
        // Required for JavaFX property binding
    }

    public Inscriere(Participant participant) {
        this.participant = participant;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "Inscriere{" +
                "participant=" + participant +
                ", events=" + events.size() +
                '}';
    }
}