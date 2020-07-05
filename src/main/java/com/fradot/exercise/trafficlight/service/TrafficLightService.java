package com.fradot.exercise.trafficlight.service;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.repository.TrafficLightConfigurationRepository;
import com.fradot.exercise.trafficlight.scheduler.TrafficLightRunnableTask;
import com.fradot.exercise.trafficlight.scheduler.TrafficLightScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

/**
 * This class maintains the logic to handle the {@link TrafficLightConfiguration} life cycle.
 */
@Service
@Transactional
public class TrafficLightService {

    private TrafficLightScheduler trafficLightScheduler;
    private PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue;
    private TrafficLightConfigurationRepository trafficLightConfigurationRepository;

    @Autowired
    public TrafficLightService(TrafficLightScheduler trafficLightScheduler,
                               PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue,
                               TrafficLightConfigurationRepository trafficLightConfigurationRepository) {
        this.trafficLightScheduler = trafficLightScheduler;
        this.trafficLightConfigurationQueue = trafficLightConfigurationQueue;
        this.trafficLightConfigurationRepository = trafficLightConfigurationRepository;
    }


    public void synchWithDatabase() {
        List<TrafficLightConfiguration> toBeEnabled =
                this.trafficLightConfigurationRepository.findAllConfigurationsToBeEnabled();
        List<TrafficLightConfiguration> toBeDisabled =
                this.trafficLightConfigurationRepository.findAllConfigurationsToBeDisabled();

    }


    @Transactional
    private void enableConfigurations() {

        // Get all active configurations including default
        List<TrafficLightConfiguration> trafficLightConfigurationList =
                trafficLightConfigurationRepository.findAllActiveConfigurations();

        Optional<TrafficLightConfiguration> defaultConfiguration = trafficLightConfigurationList.stream()
                .filter(TrafficLightConfiguration::isDefaultConfiguration)
                .findFirst();

        List<TrafficLightConfiguration> trafficLightConfigurationListToBeScheduled = trafficLightConfigurationList.stream()
                .filter(configuration -> !configuration.isDefaultConfiguration()).collect(Collectors.toList());

        if(defaultConfiguration.isPresent()) {
            trafficLightConfigurationQueue.add(defaultConfiguration.get());

            // Schedule enabling and disabling tasks
            for (TrafficLightConfiguration configuration : trafficLightConfigurationListToBeScheduled) {
                TrafficLightRunnableTask enabling = new TrafficLightRunnableTask(this.trafficLightConfigurationQueue,
                        configuration, true, false);
                TrafficLightRunnableTask disabling = new TrafficLightRunnableTask(this.trafficLightConfigurationQueue,
                        configuration, false, true);
                String enablingTaskId = configuration.getId().toString() + "enabling";
                String disablingTaskId = configuration.getId().toString() + "disabling";
                trafficLightScheduler.addCronTask(enablingTaskId, enabling, configuration.getStartCronExpression());
                trafficLightScheduler.addCronTask(disablingTaskId, disabling, configuration.getEndCronExpression());
            }

            trafficLightConfigurationList.forEach(configuration -> {
                configuration.setActive(true);
                configuration.setToBeDisabled(false);
                configuration.setToBeEnabled(false);
            });

            this.trafficLightConfigurationRepository.saveAll(trafficLightConfigurationList);
        } else {
            throw new IllegalStateException("Default configuration not present in the database");
        }

    }


    @PostConstruct
    public void init() {
        trafficLightScheduler.addStartupTask(this::enableConfigurations);
    }


}
