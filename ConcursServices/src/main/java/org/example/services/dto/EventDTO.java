package org.example.services.dto;

import java.io.Serializable;

/**
 * Data Transfer Object for event information
 */
public class EventDTO implements Serializable {
    private final Integer id;
    private final String distance;
    private final String style;
    private final int participantsCount;

    public EventDTO(Integer id, String distance, String style, int participantsCount) {
        this.id = id;
        this.distance = distance;
        this.style = style;
        this.participantsCount = participantsCount;
    }

    public Integer getId() { return id; }
    public String getDistance() { return distance; }
    public String getStyle() { return style; }
    public int getParticipantsCount() { return participantsCount; }
}