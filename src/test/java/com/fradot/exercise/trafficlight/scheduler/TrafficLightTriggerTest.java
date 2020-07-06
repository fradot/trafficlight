package com.fradot.exercise.trafficlight.scheduler;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightTransition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.TriggerContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link TrafficLightTrigger}
 */
@RunWith(SpringRunner.class)
public class TrafficLightTriggerTest {

    @MockBean
    private StateMachine<TrafficLightState, TrafficLightTransition> stateMachine;

    @MockBean
    private State<TrafficLightState, TrafficLightTransition> orangeState;

    private PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue;

    @MockBean
    private TriggerContext triggerContext;

    private TrafficLightTrigger trafficLightTriggerUnderTest;

    @Test(expected = IllegalStateException.class)
    public void itShouldRaiseAnIllegalStateExceptionIfTheConfigurationQueueIsNotInitialized() {
        this.trafficLightTriggerUnderTest = new TrafficLightTrigger(stateMachine, trafficLightConfigurationQueue);

        when(orangeState.getId()).thenReturn(TrafficLightState.ORANGE);
        when(stateMachine.getState()).thenReturn(orangeState);
        when(triggerContext.lastActualExecutionTime()).thenReturn(new Date());

        trafficLightTriggerUnderTest.nextExecutionTime(triggerContext);
        verify(triggerContext, times(1)).lastActualExecutionTime();
    }

    @Test(expected = IllegalStateException.class)
    public void itShouldRaiseAnIllegalStateExceptionIfTheConfigurationQueueIsEmpty() {
        this.trafficLightConfigurationQueue = new PriorityBlockingQueue<>(1);
        this.trafficLightTriggerUnderTest = new TrafficLightTrigger(stateMachine, trafficLightConfigurationQueue);

        when(orangeState.getId()).thenReturn(TrafficLightState.ORANGE);
        when(stateMachine.getState()).thenReturn(orangeState);
        when(triggerContext.lastActualExecutionTime()).thenReturn(new Date());

        trafficLightTriggerUnderTest.nextExecutionTime(triggerContext);
        verify(triggerContext, times(1)).lastActualExecutionTime();
    }

    @Test
    public void itShouldCalculateTheNextExecutionTimeBasedOnCurrentDateTimePlusDelayForTheFirstExecution() {
        this.trafficLightConfigurationQueue = new PriorityBlockingQueue<>(1);
        this.trafficLightConfigurationQueue.add(
                new TrafficLightConfiguration(1L, 10L, 10L, 10L, "0 0/2 * 1/1 * ?", "0 0/2 * 1/1 * ?", 5, false));
        this.trafficLightTriggerUnderTest = new TrafficLightTrigger(stateMachine, trafficLightConfigurationQueue);

        when(triggerContext.lastActualExecutionTime()).thenReturn(null);
        when(orangeState.getId()).thenReturn(TrafficLightState.ORANGE);
        when(stateMachine.getState()).thenReturn(orangeState);

        LocalDateTime currentTime = LocalDateTime.now();
        Date expectedNextExecutionTime = Date.from(
                currentTime.atZone(ZoneId.systemDefault()).plusSeconds(10L).toInstant());

        assertThat(trafficLightTriggerUnderTest.nextExecutionTime(triggerContext))
                .isCloseTo(expectedNextExecutionTime, 300);
        verify(triggerContext, times(1)).lastActualExecutionTime();
    }

    @Test
    public void itShouldCalculateTheNextExecutionTimeBasedOnLastActualExecutionTimeIfPresent() {
        this.trafficLightConfigurationQueue = new PriorityBlockingQueue<>(1);
        this.trafficLightConfigurationQueue.add(
                new TrafficLightConfiguration(1L, 10L, 10L, 10L, "0 0/2 * 1/1 * ?", "0 0/2 * 1/1 * ?", 5, false));
        this.trafficLightTriggerUnderTest = new TrafficLightTrigger(stateMachine, trafficLightConfigurationQueue);

        LocalDateTime currentTime = LocalDateTime.now();
        when(triggerContext.lastActualExecutionTime())
                .thenReturn(Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant()));
        when(orangeState.getId()).thenReturn(TrafficLightState.ORANGE);
        when(stateMachine.getState()).thenReturn(orangeState);

        Date expectedNextExecutionTime = Date.from(
                currentTime.atZone(ZoneId.systemDefault()).plusSeconds(10L).toInstant());

        assertThat(trafficLightTriggerUnderTest.nextExecutionTime(triggerContext))
                .isCloseTo(expectedNextExecutionTime, 300);
        verify(triggerContext, times(1)).lastActualExecutionTime();
    }
}
