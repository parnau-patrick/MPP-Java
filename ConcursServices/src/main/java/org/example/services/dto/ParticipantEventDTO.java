package org.example.services.dto;

import java.io.Serializable;

/**
 * Data Transfer Object for participant with event count
 */
public class ParticipantEventDTO implements Serializable {
    private final Integer id;
    private final String name;
    private final int age;
    private final int eventsCount;

    public ParticipantEventDTO(Integer id, String name, int age, int eventsCount) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.eventsCount = eventsCount;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public int getEventsCount() { return eventsCount; }
}