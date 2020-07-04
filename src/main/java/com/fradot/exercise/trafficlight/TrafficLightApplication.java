package com.fradot.exercise.trafficlight;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.service.TrafficLightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * SpringBootApplication to simulate a traffic light.
 */
@SpringBootApplication
public class TrafficLightApplication implements CommandLineRunner {

    @Autowired
    private TrafficLightService trafficLightService;

    public static void main(String[] args) {
        SpringApplication.run(TrafficLightApplication.class, args);
    }

    /**
     * The <code>trafficLightConfigurationQueue</code> contains all the active configurations and these are
     * available to the whole application through the TLQueueProxy which controls the operations on the actual queue.
     * (TODO: create proxy)
     *
     * @return {@link PriorityBlockingQueue<TrafficLightConfiguration>}
     */
    @Bean
    @Autowired
    public PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue(
            @Value("trafficlight.max.number.configurations") Integer maxConfigurations) {

        final PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue =
                new PriorityBlockingQueue<>(maxConfigurations, Collections.reverseOrder());

        // TODO: load default configuration from the database
        final TrafficLightConfiguration defaultConfiguration = new TrafficLightConfiguration(
                0L, 1L, 1L, 1L, null,
                null, 0, true);
        trafficLightConfigurationQueue.add(defaultConfiguration);

        return trafficLightConfigurationQueue;
    }

    @Override
    public void run(String... args) throws Exception {
    }
}
