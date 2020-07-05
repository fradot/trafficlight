package com.fradot.exercise.trafficlight;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightTransition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
class TrafficTrafficLightApplicationIT {

    @Autowired
    private PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue;

    @Autowired
    private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;

    @DisplayName("It should load a default configuration from the database")
    @Test
    @Order(1)
    public void itShouldLoadADefaultConfigurationFromTheDatabase() throws InterruptedException {
        await().until(() -> !trafficLightConfigurationQueue.isEmpty());
        TrafficLightConfiguration trafficLightConfigurationActual = trafficLightConfigurationQueue.peek();
        assertTrue(trafficLightConfigurationActual.isDefaultConfiguration());
    }

    @DisplayName("Default trigger should be executed every second.")
    @Test
    @Order(2)
    public void itShouldTriggerTheDefaultTransitionEverySecond() throws Exception {
        await().until(() -> !trafficLightConfigurationQueue.isEmpty());
        await()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.RED));
        await()
                .atMost(1100, TimeUnit.MILLISECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.GREEN));
        await()
                .atMost(1100, TimeUnit.MILLISECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.ORANGE));

        assertThat(stateMachine.getState().getId()).isEqualTo(TrafficLightState.ORANGE);
    }


    @DisplayName("It should trigger the transition according to the configuration with the highest priority.")
    @Test
    @Order(2)
    public void itShouldTriggerTheTransitionAccordingToTheConfigurationWithHighestPriority() throws Exception {
        await().timeout(100, TimeUnit.SECONDS)
                .and()
                .until(() ->trafficLightConfigurationQueue.size() >= 3);

        await()
                .timeout(20, TimeUnit.SECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.GREEN));

        await()
                .between(4500, TimeUnit.MILLISECONDS, 5500, TimeUnit.MILLISECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.ORANGE));

        assertThat(stateMachine.getState().getId()).isEqualTo(TrafficLightState.ORANGE);
    }

    @DisplayName("It should use the default configuration once other configurations are disabled.")
    @Test
    @Order(3)
    public void itShouldUseTheDeffaultCOnfigurationOnceOtherConfigurationsAreDisabled() throws Exception {
        await().timeout(120, TimeUnit.SECONDS)
                .and()
                .until(() ->trafficLightConfigurationQueue.size() >= 3);

        await().timeout(120, TimeUnit.SECONDS)
                .and()
                .until(() -> trafficLightConfigurationQueue.size() == 1);

        await()
                .timeout(4, TimeUnit.SECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.GREEN));

        await()
                .between(500, TimeUnit.MILLISECONDS, 1500, TimeUnit.MILLISECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.ORANGE));

        assertThat(stateMachine.getState().getId()).isEqualTo(TrafficLightState.ORANGE);
    }



    @DisplayName("The initial State should be RED.")
    @Test
    @Order(3)
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
    @Order(4)
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
