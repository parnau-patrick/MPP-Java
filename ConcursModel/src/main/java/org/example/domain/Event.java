package org.example.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a swimming event with distance and style
 */
public class Event extends Entity<Integer> {
    private String distance;
    private String style;
    private List<Participant> participants = new ArrayList<>();

    public Event() {
        // Required for JavaFX property binding
    }

    public Event(String distance, String style) {
        this.distance = distance;
        this.style = style;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    @Override
    public String toString() {
        return "Event{distance='" + distance + "', style='" + style + "', participants=" + participants.size() + "}";
    }
}