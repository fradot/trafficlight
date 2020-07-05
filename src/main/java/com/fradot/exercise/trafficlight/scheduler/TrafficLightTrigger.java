package com.fradot.exercise.trafficlight.scheduler;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.concurrent.PriorityBlockingQueue;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * This class is registered in the {@link TrafficLightScheduler} and provide a method to control the scheduled interval execution
 * by overriding the <code>nextExecutionTime</code> method.
 */
@Component
public class TrafficLightTrigger implements Trigger {

    private static final Logger log = LoggerFactory.getLogger(TrafficLightTrigger.class);

    private PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue;
    private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;

    @Autowired
    public TrafficLightTrigger(StateMachine<TrafficLightState, TrafficLightTransition> stateMachine,
                               PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue) {
        this.stateMachine = stateMachine;
        this.trafficLightConfigurationQueue = trafficLightConfigurationQueue;
    }

    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Duration nextInterval = getNextExecutionInterval(stateMachine.getState());
        LocalDateTime lastExecutionTime = convertToLocalDateTime(triggerContext.lastActualExecutionTime());
        return Date.from(lastExecutionTime.atZone(ZoneId.systemDefault()).plusSeconds(nextInterval.getSeconds()).toInstant());

    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date != null ?
                Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now(ZoneId.systemDefault());
    }

    private Duration getNextExecutionInterval(State<TrafficLightState, TrafficLightTransition> currentState) {

        if (trafficLightConfigurationQueue != null && trafficLightConfigurationQueue.size() > 0) {
            switch (currentState.getId()) {
                case ORANGE:
                    return Duration.of(trafficLightConfigurationQueue.peek().getOrangeDuration(), SECONDS);
                case RED:
                    return Duration.of(trafficLightConfigurationQueue.peek().getRedDuration(), SECONDS);
                case GREEN:
                    return Duration.of(trafficLightConfigurationQueue.peek().getGreenDuration(), SECONDS);
            }
        }

        throw new IllegalStateException("Default Traffic Light configuration not defined!");
    }


}
