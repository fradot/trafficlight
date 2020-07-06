package com.fradot.exercise.trafficlight.scheduler;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * This class represents a traffic light task and is meant to enable or disable a {@link
 * TrafficLightConfiguration}.
 */
public class TrafficLightRunnableTask implements Runnable {

    private TrafficLightConfiguration trafficLightConfiguration;
    private PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue;
    private Boolean enabling;
    private Boolean disabling;

    public TrafficLightRunnableTask(
            PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue,
            TrafficLightConfiguration trafficLightConfiguration,
            Boolean enabling,
            Boolean disabling) {
        this.trafficLightConfiguration = trafficLightConfiguration;
        this.trafficLightConfigurationQueue = trafficLightConfigurationQueue;
        this.disabling = disabling;
        this.enabling = enabling;

        if (enabling && disabling) {
            throw new IllegalArgumentException(
                    "A Traffic Light task can't be an enabling and disabling task at the same time");
        }
    }

    @Override
    public void run() {
        // Configuration is already scheduled so it needs to be removed from the queue to be disabled.
        // if it's the default configuration it won't be removed
        if (trafficLightConfigurationQueue.contains(this.trafficLightConfiguration)
                && !this.trafficLightConfiguration.isDefaultConfiguration()
                && this.disabling) {
            trafficLightConfigurationQueue.remove(this.trafficLightConfiguration);

            // Configuration is not scheduled and it needs to be added to the queue to be activated.
        } else if (!trafficLightConfigurationQueue.contains(this.trafficLightConfiguration) && this.enabling) {
            trafficLightConfigurationQueue.add(this.trafficLightConfiguration);
        }
    }
}
