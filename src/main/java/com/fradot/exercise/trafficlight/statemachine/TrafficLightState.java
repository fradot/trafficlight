package com.fradot.exercise.trafficlight.statemachine;

public enum TrafficLightState {
    ORANGE("Orange", 2),
    GREEN("Green", 2),
    RED("Red", 2);

    private final String name;
    private final Integer defaultInterval;

    TrafficLightState(final String name, Integer defaultInterval){
        this.name = name;
        this.defaultInterval = defaultInterval;
    }

    public String getName() {
        return name;
    }

    public Integer getDefaultInterval() {
        return defaultInterval;
    }
}
