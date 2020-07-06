package com.fradot.exercise.trafficlight.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

/**
 * This class defines the {@link org.springframework.statemachine.StateMachine} configuration which enables the state transitioning,
 * listeners and transitions registration.
 */
@Configuration
@EnableStateMachine
public class TrafficLightStateMachineConfig
        extends EnumStateMachineConfigurerAdapter<TrafficLightState, TrafficLightTransition> {

    private final TrafficLightListener trafficLightListener;

    private static final Logger log = LoggerFactory.getLogger(TrafficLightStateMachineConfig.class);
    public static final TrafficLightState INITIAL_STATE = TrafficLightState.RED;

    @Autowired
    public TrafficLightStateMachineConfig(TrafficLightListener trafficLightListener) {
        this.trafficLightListener = trafficLightListener;
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<TrafficLightState, TrafficLightTransition> config)
            throws Exception {
        log.info("Initializing state machine configuration.");
        super.configure(config);
        config.withConfiguration().autoStartup(true).listener(trafficLightListener);
    }

    @Override
    public void configure(StateMachineStateConfigurer<TrafficLightState, TrafficLightTransition> states)
            throws Exception {
        log.info("Initializing state machine states.");
        super.configure(states);
        states.withStates().initial(TrafficLightState.RED).states(EnumSet.allOf(TrafficLightState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<TrafficLightState, TrafficLightTransition> transitions)
            throws Exception {
        log.info("Initializing state machine transitions.");
        super.configure(transitions);
        transitions
                .withExternal()
                .source(TrafficLightState.RED)
                .target(TrafficLightState.GREEN)
                .event(TrafficLightTransition.TRANSITION)
                .and()
                .withExternal()
                .source(TrafficLightState.GREEN)
                .target(TrafficLightState.ORANGE)
                .event(TrafficLightTransition.TRANSITION)
                .and()
                .withExternal()
                .source(TrafficLightState.ORANGE)
                .target(TrafficLightState.RED)
                .event(TrafficLightTransition.TRANSITION);
    }
}
