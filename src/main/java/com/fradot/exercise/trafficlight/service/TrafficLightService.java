package com.fradot.exercise.trafficlight.service;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.repository.TrafficLightConfigurationRepository;
import com.fradot.exercise.trafficlight.scheduler.TrafficLightRunnableTask;
import com.fradot.exercise.trafficlight.scheduler.TrafficLightScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

/** This class maintains the logic to handle the {@link TrafficLightConfiguration} life cycle. */
@Service
@Transactional
public class TrafficLightService {

  private static final Logger log = LoggerFactory.getLogger(TrafficLightService.class);

  private TrafficLightScheduler trafficLightScheduler;
  private PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue;
  private TrafficLightConfigurationRepository trafficLightConfigurationRepository;

  @Autowired
  public TrafficLightService(
      TrafficLightScheduler trafficLightScheduler,
      PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue,
      TrafficLightConfigurationRepository trafficLightConfigurationRepository) {
    this.trafficLightScheduler = trafficLightScheduler;
    this.trafficLightConfigurationQueue = trafficLightConfigurationQueue;
    this.trafficLightConfigurationRepository = trafficLightConfigurationRepository;
  }

  public void synchWithDatabase() {
    this.enableConfigurations(
        this.trafficLightConfigurationRepository.findAllConfigurationsToBeEnabled());

    // TODO: handle disabling configurations
    List<TrafficLightConfiguration> toBeDisabledList =
        this.trafficLightConfigurationRepository.findAllConfigurationsToBeDisabled();
  }

  @Transactional
  private void enableConfigurations(List<TrafficLightConfiguration> trafficLightConfigurationList) {

    Optional<TrafficLightConfiguration> defaultConfiguration =
        trafficLightConfigurationList.stream()
            .filter(TrafficLightConfiguration::isDefaultConfiguration)
            .findFirst();

    List<TrafficLightConfiguration> trafficLightConfigurationListToBeScheduled =
        trafficLightConfigurationList.stream()
            .filter(configuration -> !configuration.isDefaultConfiguration())
            .collect(Collectors.toList());

    if (defaultConfiguration.isPresent()) {
      trafficLightConfigurationQueue.add(defaultConfiguration.get());

      // Schedule enabling and disabling tasks
      for (TrafficLightConfiguration configuration : trafficLightConfigurationListToBeScheduled) {

        // enabling task
        if (configuration.getStartCronExpression() == null || (configuration.getStartCronExpression() != null &&
                configuration.getStartCronExpression().isEmpty())) {
          log.warn("Start cron expression is not defined, configuration will never be activated!");
        } else {
          String enablingTaskId = configuration.getId().toString() + "enabling";
          TrafficLightRunnableTask enabling =
              new TrafficLightRunnableTask(
                  this.trafficLightConfigurationQueue, configuration, true, false);
          trafficLightScheduler.addCronTask(
              enablingTaskId, enabling, configuration.getStartCronExpression());
        }

        // disabling task
        if (configuration.getEndCronExpression() == null || (configuration.getEndCronExpression() != null &&
                configuration.getEndCronExpression().isEmpty())) {
            log.warn("Start cron expression is not defined, configuration will never be activated!");
        } else {
          TrafficLightRunnableTask disabling =
              new TrafficLightRunnableTask(
                  this.trafficLightConfigurationQueue, configuration, false, true);
          String disablingTaskId = configuration.getId().toString() + "disabling";
          trafficLightScheduler.addCronTask(
              disablingTaskId, disabling, configuration.getEndCronExpression());
        }
      }

      trafficLightConfigurationList.forEach(
          configuration -> {
            configuration.setActive(true);
            configuration.setToBeDisabled(false);
            configuration.setToBeEnabled(false);
          });

      this.trafficLightConfigurationRepository.saveAll(trafficLightConfigurationList);
    } else {
      throw new IllegalStateException("Default configuration not present in the database");
    }
  }

  private void enableAllActiveConfigurationsAtStartup() {
    log.info("Loading all configurations from the database");
    this.enableConfigurations(
        this.trafficLightConfigurationRepository.findAllActiveConfigurations());
  }

  @PostConstruct
  public void init() {
    trafficLightScheduler.addStartupTask(this::enableAllActiveConfigurationsAtStartup);
  }
}
