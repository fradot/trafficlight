package com.fradot.exercise.trafficlight;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.repository.TrafficLightConfigurationRepository;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightTransition;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TrafficLightDatabaseSchedulingIT {

    @Autowired
    private PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue;

    @Autowired
    private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;

    @Autowired
    private TrafficLightConfigurationRepository trafficLightConfigurationRepository;

    private static final Logger log = LoggerFactory.getLogger(TrafficLightDatabaseSchedulingIT.class);

    @DisplayName("It should load a default configuration from the database")
    @Order(1)
    @Test
    public void itShouldLoadADefaultConfigurationFromTheDatabase() {
        log.info("wait until the queue is not empty.");
        await().until(() -> !trafficLightConfigurationQueue.isEmpty());
        TrafficLightConfiguration trafficLightConfigurationActual = trafficLightConfigurationQueue.peek();
        assertTrue(trafficLightConfigurationActual.isDefaultConfiguration());
    }

    @DisplayName("Default trigger should be executed every second.")
    @Order(2)
    @Test
    public void itShouldTriggerTheDefaultTransitionEverySecond() {
        log.info("wait until the queue is not empty and state machine is on RED.");
        await().until(() -> !trafficLightConfigurationQueue.isEmpty());
        await().until(() -> stateMachine.getState().getId().equals(TrafficLightState.RED));
        log.info("wait at most 1100ms for GREEN");
        await().atMost(1100, TimeUnit.MILLISECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.GREEN));
        log.info("wait at most 1100ms for ORANGE");
        await().atMost(1100, TimeUnit.MILLISECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.ORANGE));

        assertThat(stateMachine.getState().getId()).isEqualTo(TrafficLightState.ORANGE);
    }

    @DisplayName("It should trigger the transition according to the configuration with the highest priority.")
    @Order(3)
    @Test
    public void itShouldTriggerTheTransitionAccordingToTheConfigurationWithHighestPriority() {
        log.info("wait for 3 configurations to be scheduled");
        await().timeout(100, TimeUnit.SECONDS).and().until(() -> trafficLightConfigurationQueue.size() >= 3);

        log.info("wait for RED.");
        await().until(() -> stateMachine.getState().getId().equals(TrafficLightState.RED));

        log.info("wait for GREEN.");
        await().timeout(20, TimeUnit.SECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.GREEN));

        log.info("ORANGE is displayed between 4500ms and 5500ms.");
        await().between(4500, TimeUnit.MILLISECONDS, 5500, TimeUnit.MILLISECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.ORANGE));

        assertThat(stateMachine.getState().getId()).isEqualTo(TrafficLightState.ORANGE);
    }

    @DisplayName("It should use the default configuration once other configurations are disabled.")
    @Order(4)
    @Test
    public void itShouldUseTheDefaultConfigurationOnceOtherConfigurationsAreDisabled() {
        log.info("wait for 3 configurations to be scheduled.");
        await().timeout(120, TimeUnit.SECONDS).and().until(() -> trafficLightConfigurationQueue.size() >= 3);

        log.info("wait for 2 configurations to be disabled.");
        await().timeout(120, TimeUnit.SECONDS).and().until(() -> trafficLightConfigurationQueue.size() == 1);

        log.info("wait for GREEN.");
        await().timeout(4, TimeUnit.SECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.GREEN));

        log.info("ORANGE is displayed after between 500ms and 1500ms.");
        await().between(500, TimeUnit.MILLISECONDS, 1500, TimeUnit.MILLISECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.ORANGE));

        assertThat(stateMachine.getState().getId()).isEqualTo(TrafficLightState.ORANGE);
    }
}
