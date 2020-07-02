package com.fradot.exercise.trafficlight.scheduler;

import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightTransition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.event.annotation.AfterTestClass;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TrafficLightTriggerTest {

    @Autowired
    private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;

    @DisplayName("Default trigger should be executed every 2 seconds.")
    @Test
    public void itShouldHaveRedAsTheInitialState() throws Exception {
        assertThat(stateMachine.getState().getId()).isEqualTo(TrafficLightState.GREEN);
        sleep(2000);
        assertThat(stateMachine.getState().getId()).isEqualTo(TrafficLightState.ORANGE);
        sleep(2000);
        assertThat(stateMachine.getState().getId()).isEqualTo(TrafficLightState.RED);
    }

    @AfterTestClass
    public void shutDownStateMachine() {
        stateMachine.stop();
        stateMachine = null;
    }
}
