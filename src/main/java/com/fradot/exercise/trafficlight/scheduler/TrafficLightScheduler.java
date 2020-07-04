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
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.statemachine.StateMachine;

import java.util.concurrent.*;

/**
 * This class defined a custom {@link org.springframework.scheduling.TaskScheduler} by providing a custom implementation
 * of the {@link ThreadPoolTaskScheduler} to the {@link ScheduledTaskRegistrar}.
 * By using this custom configuration it's possible to register a custom {@link org.springframework.scheduling.Trigger} (see {@link TrafficLightTrigger})
 * and control the scheduled execution interval dynamically.
 */
@Configuration
@EnableScheduling
public class TrafficLightScheduler implements SchedulingConfigurer {

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private ConcurrentMap<String, ScheduledFuture> scheduledFutureMap;
    private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;
    private TrafficLightTrigger trafficLightTrigger;


    @Autowired
    public TrafficLightScheduler(TrafficLightTrigger trafficLightTrigger, StateMachine<TrafficLightState,
            TrafficLightTransition> stateMachine) {
        this.stateMachine = stateMachine;
        this.trafficLightTrigger = trafficLightTrigger;
        this.scheduledFutureMap = new ConcurrentHashMap<>(1);
    }


    /**
     * Configure {@link ScheduledTaskRegistrar} in order to execute my custom {@link TrafficLightTrigger}.
     *
     * @param taskRegistrar the {@link ScheduledTaskRegistrar} to be customized
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
        taskRegistrar.addTriggerTask(
                () -> stateMachine.sendEvent(TrafficLightTransition.TRANSITION),
                trafficLightTrigger);
    }

    /**
     * Schedule a task to be executed based on the provided cron expression
     *
     * @param task
     * @param cronExpression
     */
    public void addCronTask(String id, Runnable task, String cronExpression) {
        ScheduledFuture<?> scheduledFuture = threadPoolTaskScheduler
                .schedule(task, new CronTrigger(cronExpression));
        scheduledFutureMap.put(id, scheduledFuture);
    }

    /**
     * Delete the task identified by id.
     *
     * @param id
     */
    public void deleteCronTask(String id) {
        scheduledFutureMap.get(id).cancel(true);
    }

    /**
     * Provides a custom implementation of the {@link ThreadPoolTaskScheduler}.
     * Defining the destroyMethod to shutdown in order to terminate the scheduler on application shutdown.
     *
     * @return a custom implementation of the {@link ThreadPoolTaskScheduler}
     */
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        //TODO: set error handler
        ThreadPoolTaskScheduler threadPoolTaskScheduler
                = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setRemoveOnCancelPolicy(true);
        threadPoolTaskScheduler.setThreadNamePrefix(
                "TrafficLight Task Scheduler");
        return threadPoolTaskScheduler;
    }
}
