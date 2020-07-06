package com.fradot.exercise.trafficlight.scheduler;

import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.statemachine.StateMachine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * This class defined a custom {@link org.springframework.scheduling.TaskScheduler} by providing a
 * custom implementation of the {@link ThreadPoolTaskScheduler} to the {@link
 * ScheduledTaskRegistrar}. By using this custom configuration it's possible to register a custom
 * {@link org.springframework.scheduling.Trigger} (see {@link TrafficLightTrigger}) and control the
 * scheduled execution interval dynamically.
 */
@Configuration
@EnableScheduling
public class TrafficLightScheduler implements SchedulingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(TrafficLightScheduler.class);

    @Value("${trafficlight.initial.delay.seconds}")
    private Long initialDelay;

    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private ConcurrentMap<String, ScheduledFuture> scheduledFutureMap;
    private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;
    private TrafficLightTrigger trafficLightTrigger;
    private List startupTasks;
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    public TrafficLightScheduler(
            TrafficLightTrigger trafficLightTrigger,
            StateMachine<TrafficLightState, TrafficLightTransition> stateMachine,
            ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.stateMachine = stateMachine;
        this.trafficLightTrigger = trafficLightTrigger;
        this.scheduledFutureMap = new ConcurrentHashMap<>(1);
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
        this.startupTasks = Collections.synchronizedList(new ArrayList<Runnable>(5));
    }

    /**
     * Configure {@link ScheduledTaskRegistrar} in order to execute my custom {@link
     * TrafficLightTrigger}.
     *
     * @param taskRegistrar the {@link ScheduledTaskRegistrar} to be customized
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
        threadPoolTaskScheduler.schedule(
                this::executeStartup,
                Date.from(LocalDateTime.now()
                        .atZone(ZoneId.systemDefault())
                        .plusSeconds(initialDelay)
                        .toInstant()));
    }

    private void executeStartup() {
        for (Object task : this.startupTasks) {
            Runnable startupTask = (Runnable) task;
            startupTask.run();
        }

        threadPoolTaskScheduler.schedule(
                () -> stateMachine.sendEvent(TrafficLightTransition.TRANSITION), trafficLightTrigger);
    }

    /**
     * Add a {@link Runnable} task to be executed at startup.
     *
     * @param task
     */
    public synchronized void addStartupTask(Runnable task) {
        this.startupTasks.add(task);
    }

    /**
     * Schedule a task to be executed based on the provided cron expression
     *
     * @param task
     * @param cronExpression
     */
    public void addCronTask(String id, Runnable task, String cronExpression) {
        ScheduledFuture<?> scheduledFuture = threadPoolTaskScheduler.schedule(task, new CronTrigger(cronExpression));
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
}
