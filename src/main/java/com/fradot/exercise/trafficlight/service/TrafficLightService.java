package com.fradot.exercise.trafficlight.service;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.scheduler.TrafficLightScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * This class maintains the logic to handle the {@link TrafficLightConfiguration} life cycle.
 */
@Service
public class TrafficLightService {

    private TrafficLightScheduler trafficLightScheduler;
    private PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue;

    @Autowired
    public TrafficLightService( TrafficLightScheduler trafficLightScheduler,
                               PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue) {
        this.trafficLightScheduler = trafficLightScheduler;
        this.trafficLightConfigurationQueue = trafficLightConfigurationQueue;
    }

}
