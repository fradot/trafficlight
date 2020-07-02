package com.fradot.exercise.trafficlight.scheduler;

import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.time.temporal.ChronoUnit.SECONDS;
import static com.fradot.exercise.trafficlight.statemachine.TrafficLightState.RED;
import static com.fradot.exercise.trafficlight.statemachine.TrafficLightState.ORANGE;
import static com.fradot.exercise.trafficlight.statemachine.TrafficLightState.GREEN;

@Component
public class TrafficLightCurrentConfiguration {

    private ConcurrentMap<TrafficLightState, Duration> stateDurationMap = new ConcurrentHashMap<>(3);

    public Map<TrafficLightState, Duration> getStateDurationMap() {
        return stateDurationMap;
    }

    /**
     * Init bean with default configuration.
     */
    @PostConstruct
    public void init() {
        this.getStateDurationMap().put(ORANGE, Duration.of(ORANGE.getDefaultInterval(), SECONDS));
        this.getStateDurationMap().put(GREEN, Duration.of(GREEN.getDefaultInterval(), SECONDS));
        this.getStateDurationMap().put(RED, Duration.of(RED.getDefaultInterval(), SECONDS));
    }
}
