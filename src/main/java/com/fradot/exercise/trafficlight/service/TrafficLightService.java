package com.fradot.exercise.trafficlight.service;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.repository.TrafficLightConfigurationRepository;
import com.fradot.exercise.trafficlight.scheduler.TrafficLightScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * This class maintains the logic to handle the {@link TrafficLightConfiguration} life cycle.
 */
@Service
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

    public void refreshFromDatabase() {

        List<TrafficLightConfiguration> trafficLightActiveConfigurations =
                trafficLightConfigurationRepository.findAllActiveConfigurations();
        List<TrafficLightConfiguration> trafficLightDisablesConfigurations =
                trafficLightConfigurationRepository.findAllDisabledConfigurations();
    }

    public void loadDefaultConfiguration() {
        Optional<TrafficLightConfiguration> trafficLightConfiguration =
                trafficLightConfigurationRepository.findDefaultConfiguration();
        if(trafficLightConfiguration.isPresent()) {
            trafficLightConfigurationQueue.add(trafficLightConfiguration.get());
        } else {
            throw new IllegalStateException("Default configuration not present in the database");
        }
    }

    @PostConstruct
    public void init() {
        trafficLightScheduler.addStartupTask(this::loadDefaultConfiguration);
    }


}
