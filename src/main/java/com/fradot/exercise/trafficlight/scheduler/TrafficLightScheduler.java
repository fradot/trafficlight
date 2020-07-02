package com.fradot.exercise.trafficlight.scheduler;

import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightTransition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.statemachine.StateMachine;

/**
 * This class defined a custom {@link org.springframework.scheduling.TaskScheduler} by providing a custom implementation
 * of the {@link ThreadPoolTaskScheduler} to the {@link ScheduledTaskRegistrar}.
 * By using this custom configuration it's possible to register a custom {@link org.springframework.scheduling.Trigger} (see {@link TrafficLightTrigger})
 * and control the scheduled execution interval dynamically.
 */
@Configuration
@EnableScheduling
public class TrafficLightScheduler implements SchedulingConfigurer {

    private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;
    private TrafficLightTrigger trafficLightTrigger;

    @Autowired
    public TrafficLightScheduler(TrafficLightTrigger trafficLightTrigger,
                                 StateMachine<TrafficLightState, TrafficLightTransition> stateMachine) {
        this.stateMachine = stateMachine;
        this.trafficLightTrigger = trafficLightTrigger;
    }


    /**
     * Configure {@link ScheduledTaskRegistrar} in order to execute my custom {@link TrafficLightTrigger}.
     *
     * @param taskRegistrar the {@link ScheduledTaskRegistrar} to be customized
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler());
        taskRegistrar.addTriggerTask(() -> stateMachine.sendEvent(TrafficLightTransition.TRANSITION),
                trafficLightTrigger);
    }

    /**
     * Provides a custom implementation of the {@link ThreadPoolTaskScheduler}.
     * Defining the destroyMethod to shutdown in order to terminate the scheduler.
     *
     * @return a custom implementation of the {@link ThreadPoolTaskScheduler}
     */
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler
                = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setThreadNamePrefix(
                "TrafficLight Task Scheduler");
        return threadPoolTaskScheduler;
    }
}
