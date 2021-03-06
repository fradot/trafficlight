package com.fradot.exercise.trafficlight.statemachine;

/**
 * This Enum describes the three different states in which a traffic light might be.
 * This Enum is used in {@link TrafficLightStateMachineConfig} class.
 */
public enum TrafficLightState {
    ORANGE("Orange", 1L),
    GREEN("Green", 1L),
    RED("Red", 1L);

    private final String name;
    private final Long defaultInterval;

    TrafficLightState(final String name, Long defaultInterval) {
        this.name = name;
        this.defaultInterval = defaultInterval;
    }

    public String getName() {
        return name;
    }

    public Long getDefaultInterval() {
        return defaultInterval;
    }
}
