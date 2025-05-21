package org.example.network.dto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for participant registration data transfer
 */
public class RegistrationDTO implements Serializable {
    private String participantName;
    private int participantAge;
    private List<Integer> eventIds;

    public RegistrationDTO() {}

    public RegistrationDTO(String participantName, int participantAge, List<Integer> eventIds) {
        this.participantName = participantName;
        this.participantAge = participantAge;
        this.eventIds = eventIds;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public int getParticipantAge() {
        return participantAge;
    }

    public void setParticipantAge(int participantAge) {
        this.participantAge = participantAge;
    }

    public List<Integer> getEventIds() {
        return eventIds;
    }

    public void setEventIds(List<Integer> eventIds) {
        this.eventIds = eventIds;
    }
}