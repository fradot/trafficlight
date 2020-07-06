package com.fradot.exercise.trafficlight;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.repository.TrafficLightConfigurationRepository;
import com.fradot.exercise.trafficlight.scheduler.TrafficLightScheduler;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightTransition;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TrafficLightConfigurationDisablingAtRuntimeIT {

    @Autowired
    private PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue;

    @Autowired private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;

    @Autowired private TrafficLightConfigurationRepository trafficLightConfigurationRepository;

    @Autowired private TrafficLightScheduler trafficLightScheduler;

    private static final Logger log = LoggerFactory.getLogger(TrafficLightTaskEnablingAtRuntimeIT.class);


    @DisplayName("It should disable a configuration at runtime.")
    @Order(1)
    @Test
    public void itShouldDisableAConfigurationAtRuntime() throws Exception {

        log.info("wait for all the configuration to be active.");
        await()
                .timeout(200, TimeUnit.SECONDS)
                .and()
                .until(() -> trafficLightConfigurationQueue.size() >= 3);

        log.info("Update the highest in priority configuration to be disabled");
        TrafficLightConfiguration highPriorityConfiguration = trafficLightConfigurationQueue.peek();
        highPriorityConfiguration.setToBeDisabled(true);
        trafficLightConfigurationRepository.save(highPriorityConfiguration);

        log.info("wait for the configuration to be removed.");
        sleep(185000);

        log.info("wait for RED.");
        await()
                .timeout(20, TimeUnit.SECONDS)
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.RED));

        log.info("GREEN is displayed after between 1500ms and 2500ms.");
        await()
                .between(700, TimeUnit.MILLISECONDS, 2500, TimeUnit.MILLISECONDS)
                .and()
                .until(() -> stateMachine.getState().getId().equals(TrafficLightState.GREEN));

        assertThat(stateMachine.getState().getId()).isEqualTo(TrafficLightState.GREEN);
    }
}
