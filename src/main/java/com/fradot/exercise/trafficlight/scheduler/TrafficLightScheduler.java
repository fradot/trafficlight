package com.fradot.exercise.trafficlight.scheduler;

import com.fradot.exercise.trafficlight.statemachine.TrafficLightListener;
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

@Configuration
@EnableScheduling
public class TrafficLightScheduler implements SchedulingConfigurer {

    private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;
    private TrafficLightTrigger trafficLightTrigger;

    @Autowired
    public TrafficLightScheduler(TrafficLightTrigger trafficLightTrigger, StateMachine<TrafficLightState, TrafficLightTransition> stateMachine) {
        this.stateMachine = stateMachine;
        this.trafficLightTrigger = trafficLightTrigger;
    }


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler());
        taskRegistrar.addTriggerTask(() -> stateMachine.sendEvent(TrafficLightTransition.TRANSITION), trafficLightTrigger);
    }

    @Bean(destroyMethod="shutdown")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler
                = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setThreadNamePrefix(
                "TrafficLight Task Scheduler");
        return threadPoolTaskScheduler;
    }
}
