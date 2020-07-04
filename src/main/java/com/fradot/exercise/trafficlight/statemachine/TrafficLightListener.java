package com.fradot.exercise.trafficlight.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

/**
 * This class is registered as a listener in the {@link TrafficLightStateMachineConfig} and listens for {@link TrafficLightState} changes.
 */
@Component
public class TrafficLightListener extends StateMachineListenerAdapter<TrafficLightState, TrafficLightTransition> {

    private static final Logger log = LoggerFactory.getLogger(TrafficLightStateMachineConfig.class);

    @Override
    public void stateEntered(State<TrafficLightState, TrafficLightTransition> state) {
        super.stateEntered(state);
        log.info("Transitioned to {}", state.getId().getName());
    }

    // TODO : handle state machine error

}
