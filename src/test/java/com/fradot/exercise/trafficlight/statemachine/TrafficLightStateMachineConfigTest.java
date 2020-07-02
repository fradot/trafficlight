package com.fradot.exercise.trafficlight.statemachine;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.event.annotation.AfterTestClass;

/**
 * Test class for the state machine configuration {@link TrafficLightStateMachineConfig} class.
 */
@SpringBootTest
public class TrafficLightStateMachineConfigTest {

    @Autowired
    private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;

    @BeforeEach
    public void init() {
        stateMachine.stop();
    }

    @DisplayName("The initial State should be RED.")
    @Test
    public void itShouldHaveRedAsTheInitialState() throws Exception {
        StateMachineTestPlan<TrafficLightState, TrafficLightTransition> plan =
                StateMachineTestPlanBuilder.<TrafficLightState, TrafficLightTransition>builder()
                    .stateMachine(stateMachine)
                    .step()
                        .expectStates(TrafficLightState.RED)
                    .and()
                    .build();
        plan.test();
    }

    @DisplayName("It should transition from RED to GREEN and then to ORANGE and back to RED.")
    @Test
    public void itShouldTransitionToGreenThenToOrange() throws Exception {
        StateMachineTestPlan<TrafficLightState, TrafficLightTransition> plan =
                StateMachineTestPlanBuilder.<TrafficLightState, TrafficLightTransition>builder()
                    .stateMachine(stateMachine)
                    .step()
                        .sendEvent(TrafficLightTransition.TRANSITION)
                        .expectStates(TrafficLightState.GREEN)
                    .and()
                    .step()
                        .sendEvent(TrafficLightTransition.TRANSITION)
                        .expectState(TrafficLightState.ORANGE)
                    .and()
                    .step()
                        .sendEvent(TrafficLightTransition.TRANSITION)
                        .expectState(TrafficLightState.RED)
                    .and()
                    .build();

        plan.test();
    }

    @AfterTestClass
    public void shutDownStateMachine() {
        stateMachine.stop();
        stateMachine = null;
    }
}
