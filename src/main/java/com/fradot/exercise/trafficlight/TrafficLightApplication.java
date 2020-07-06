package com.fradot.exercise.trafficlight;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Collections;
import java.util.concurrent.PriorityBlockingQueue;

/** SpringBootApplication to simulate a traffic light. */
@SpringBootApplication
public class TrafficLightApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TrafficLightApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TrafficLightApplication.class, args);
    }

    /**
     * The <code>trafficLightConfigurationQueue</code> contains all the active configurations and
     * these are available to the whole application through the TLQueueProxy which controls the
     * operations on the actual queue. (TODO: create proxy)
     *
     * @return {@link PriorityBlockingQueue< TrafficLightConfiguration >}
     */
    @Bean
    @Autowired
    public PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue() {
        return new PriorityBlockingQueue<>(10, Collections.reverseOrder());
    }

    /**
     * Provides a custom implementation of the {@link ThreadPoolTaskScheduler}. Defining the
     * destroyMethod to shutdown in order to terminate the scheduler on application shutdown.
     *
     * @return a custom implementation of the {@link ThreadPoolTaskScheduler}
     */
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setRemoveOnCancelPolicy(true);
        threadPoolTaskScheduler.setThreadNamePrefix("TrafficLight Task Scheduler");
        threadPoolTaskScheduler.setErrorHandler(throwable -> {
            log.error("Traffic Light task scheduler error {}", throwable.getMessage());
        });
        return threadPoolTaskScheduler;
    }

    @Override
    public void run(String... args) throws Exception {}
}
