package com.fradot.exercise.trafficlight.statemachine;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.scheduler.TrafficLightScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class is registered as a listener in the {@link TrafficLightStateMachineConfig} and listens for {@link TrafficLightState} changes.
 */
@Component
public class TrafficLightListener extends StateMachineListenerAdapter<TrafficLightState, TrafficLightTransition> {

    private static final Logger log = LoggerFactory.getLogger(TrafficLightListener.class);

    @Override
    public void stateEntered(State<TrafficLightState, TrafficLightTransition> state) {
        super.stateEntered(state);
        log.info("Transitioned to {}", state.getId().getName());
    }

    @Override
    public void stateMachineError(
            StateMachine<TrafficLightState, TrafficLightTransition> stateMachine, Exception exception) {
        super.stateMachineError(stateMachine, exception);
        log.error("Cancelling all scheduled tasks. Next database synchronization will try to resume execution.");
        stateMachine.stop();
        throw new IllegalStateException("Traffic Light State Machine error", exception);
    }
}
