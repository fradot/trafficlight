package com.fradot.exercise.trafficlight.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is registered as a listener in the {@link TrafficLightStateMachineConfig} and listens for {@link TrafficLightState} changes.
 */
@Component
public class TrafficLightListener extends StateMachineListenerAdapter<TrafficLightState, TrafficLightTransition> {

    private static final AtomicInteger transitionsCounter = new AtomicInteger(0);

    private static final Logger log = LoggerFactory.getLogger(TrafficLightStateMachineConfig.class);

    @Override
    public void stateChanged(State<TrafficLightState, TrafficLightTransition> from, State<TrafficLightState, TrafficLightTransition> to) {
        log.info("Transitioning from {} to {}",
                transitionsCounter.getAndIncrement() > 0
                        ? from.getId().getName() : TrafficLightStateMachineConfig.INITIAL_STATE,
                to.getId().getName());
    }
}
