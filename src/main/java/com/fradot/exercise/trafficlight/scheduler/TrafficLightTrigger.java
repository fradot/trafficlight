package com.fradot.exercise.trafficlight.scheduler;

import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * This class is registered in the {@link TrafficLightScheduler} and provide a method to control the scheduled interval execution
 * by overriding the <code>nextExecutionTime</code> method.
 */
@Component
public class TrafficLightTrigger implements Trigger {

    private static final Logger log = LoggerFactory.getLogger(TrafficLightTrigger.class);

    private TrafficLightCurrentConfiguration trafficLightCurrentConfiguration;
    private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;

    @Autowired
    public TrafficLightTrigger(TrafficLightCurrentConfiguration trafficLightCurrentConfiguration,
                               StateMachine<TrafficLightState, TrafficLightTransition> stateMachine) {
        this.trafficLightCurrentConfiguration = trafficLightCurrentConfiguration;
        this.stateMachine = stateMachine;
    }

    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Duration nextInterval = getNextExecutionInterval(stateMachine.getState());
        LocalDateTime lastExecutionTime = convertToLocalDateTime(triggerContext.lastActualExecutionTime());
        return Date.from(lastExecutionTime.atZone(ZoneId.systemDefault()).plusSeconds(nextInterval.getSeconds()).toInstant());

    }

    private LocalDateTime convertToLocalDateTime(@Nullable Date date) {
        return date != null ? Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                : Instant.ofEpochMilli(new Date().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private Duration getNextExecutionInterval(State<TrafficLightState, TrafficLightTransition> currentState) {

        switch (currentState.getId()) {
            case ORANGE:
                return trafficLightCurrentConfiguration.getStateDurationMap().get(TrafficLightState.ORANGE);
            case RED:
                return trafficLightCurrentConfiguration.getStateDurationMap().get(TrafficLightState.RED);
            case GREEN:
                return trafficLightCurrentConfiguration.getStateDurationMap().get(TrafficLightState.GREEN);
        }

        return null;
    }


}
