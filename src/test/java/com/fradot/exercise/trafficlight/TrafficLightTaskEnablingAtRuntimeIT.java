package com.fradot.exercise.trafficlight;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.repository.TrafficLightConfigurationRepository;
import com.fradot.exercise.trafficlight.scheduler.TrafficLightScheduler;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightTransition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@RunWith(SpringRunner.class)
@SpringBootTest
class TrafficLightTaskEnablingAtRuntimeIT {

  @Autowired
  private PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue;

  @Autowired private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;

  @Autowired private TrafficLightConfigurationRepository trafficLightConfigurationRepository;

  @Autowired private TrafficLightScheduler trafficLightScheduler;

  private static final Logger log = LoggerFactory.getLogger(TrafficLightTaskEnablingAtRuntimeIT.class);


  @DisplayName("It should schedule a new configuration created at runtime.")
  @Test
  @Order(1)
  public void itShouldScheduleANewConfigurationCreatedAtRuntime() throws Exception {

    log.info("wait for 3 configurations to be scheduled.");
    await()
        .timeout(200, TimeUnit.SECONDS)
        .and()
        .until(() -> trafficLightConfigurationQueue.size() >= 3);

    log.info("create a new configuration programmatically.");
    TrafficLightConfiguration trafficLightConfiguration =
        new TrafficLightConfiguration(
            5L, 20L, 16L, 21L, "0/1 * * 1/1 * ?", null, 5, false, false, true, false);
    TrafficLightConfiguration newlyCreatedConfiguration = trafficLightConfigurationRepository.save(trafficLightConfiguration);

    log.info("wait for the new configuration to be activated.");
    await()
        .timeout(200, TimeUnit.SECONDS)
        .and()
        .until(() -> trafficLightConfigurationQueue.size() >= 4);

    log.info("wait for RED.");
    await()
        .timeout(60, TimeUnit.SECONDS)
        .until(() -> stateMachine.getState().getId().equals(TrafficLightState.RED));

    log.info("GREEN is displayed after between 15500ms and 16500ms.");
    await()
        .between(15500, TimeUnit.MILLISECONDS, 16500, TimeUnit.MILLISECONDS)
        .and()
        .until(() -> stateMachine.getState().getId().equals(TrafficLightState.GREEN));

    log.info("ORANGE is displayed after between 19500ms and 20500ms.");
    await()
        .between(19500, TimeUnit.MILLISECONDS, 20500, TimeUnit.MILLISECONDS)
        .and()
        .until(() -> stateMachine.getState().getId().equals(TrafficLightState.ORANGE));

    assertThat(stateMachine.getState().getId()).isEqualTo(TrafficLightState.ORANGE);
  }

}
