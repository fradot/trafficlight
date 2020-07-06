package com.fradot.exercise.trafficlight;

import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightTransition;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TrafficLightStateMachineIT {

  @Autowired private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;

  @DisplayName("The initial State should be RED.")
  @Test
  public void itShouldHaveRedAsTheInitialState() throws Exception {
    stateMachine.stop();
    StateMachineTestPlan<TrafficLightState, TrafficLightTransition> plan =
        StateMachineTestPlanBuilder.<TrafficLightState, TrafficLightTransition>builder()
            .stateMachine(stateMachine)
            .step()
            .expectStates(TrafficLightState.RED)
            .and()
            .build();
    plan.test();
  }

  @DisplayName("It should transition to GREEN then to ORANGE and back to RED.")
  @Test
  public void itShouldTransitionToGreenThenToOrangeThenBackToRed() throws Exception {
    stateMachine.stop();
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
