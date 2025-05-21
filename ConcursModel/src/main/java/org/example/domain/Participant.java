package org.example.domain;

/**
 * Represents a competition participant
 */
public class Participant extends Entity<Integer> {
    private String name;
    private Integer age;

    public Participant() {
        // Required for JavaFX property binding
    }

    public Participant(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Participant{name='" + name + "', age=" + age + "}";
    }
}