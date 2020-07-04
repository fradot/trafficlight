package com.fradot.exercise.trafficlight.scheduler;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.PriorityBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link TrafficLightRunnableTask}
 */
@RunWith(SpringRunner.class)
public class TrafficLightRunnableTaskTest {

    private static final String START_CRON = "0 00 18 ? * FRI";
    private static final String END_CRON = "0 14 18 ? * FRI";

    private TrafficLightConfiguration trafficLightConfiguration;
    private TrafficLightConfiguration defaultTrafficLightConfiguration;
    private PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue;
    private TrafficLightRunnableTask trafficLightRunnableTask_Enabling_UnderTest;
    private TrafficLightRunnableTask trafficLightRunnableTask_Disabling_UnderTest;
    private TrafficLightRunnableTask trafficLightRunnableTask_Disabling_DefaultUnderTest;


    @Before
    public void init() {
        trafficLightConfiguration = new TrafficLightConfiguration(
                1L, 5L, 4L, 2L, START_CRON,
                END_CRON, 2, false);

        TrafficLightConfiguration trafficLightConfiguration2 = new TrafficLightConfiguration(
                2L, 10L, 3L, 5L, START_CRON,
                END_CRON, 1, false);

        defaultTrafficLightConfiguration = new TrafficLightConfiguration(
                -1L, 1L, 1L, 1L, START_CRON,
                END_CRON, 0, true);


        trafficLightConfigurationQueue = new PriorityBlockingQueue<>();
        trafficLightConfigurationQueue.add(defaultTrafficLightConfiguration);
        trafficLightConfigurationQueue.add(trafficLightConfiguration2);

        trafficLightRunnableTask_Enabling_UnderTest =
                new TrafficLightRunnableTask(trafficLightConfigurationQueue, trafficLightConfiguration, true, false);
        trafficLightRunnableTask_Disabling_UnderTest =
                new TrafficLightRunnableTask(trafficLightConfigurationQueue, trafficLightConfiguration, false, true);
        trafficLightRunnableTask_Disabling_DefaultUnderTest =
                new TrafficLightRunnableTask(trafficLightConfigurationQueue, defaultTrafficLightConfiguration, false, true);
    }

    @Test
    public void itShouldAddTheConfigurationToTheQueueWhenEnabling() {
        this.trafficLightRunnableTask_Enabling_UnderTest.run();
        assertThat(trafficLightConfigurationQueue).contains(trafficLightConfiguration);
    }

    @Test
    public void itShouldRemoveTheConfigurationFromTheQueueWhenDisabling() {
        this.trafficLightRunnableTask_Disabling_UnderTest.run();
        assertThat(trafficLightConfigurationQueue).doesNotContain(trafficLightConfiguration);
    }

    @Test
    public void itShouldNotRemoveTheConfigurationFromTheQueueIfIsDefaultConfiguration() {
        this.trafficLightRunnableTask_Disabling_DefaultUnderTest.run();
        assertThat(trafficLightConfigurationQueue).contains(defaultTrafficLightConfiguration);
    }


    @Test(expected = IllegalArgumentException.class)
    public void itShouldThrowAnIllegalArgumentExceptionWhenBothEnablingAndDisablingAreTrue() {
        TrafficLightRunnableTask trafficLightRunnableTask = new TrafficLightRunnableTask(trafficLightConfigurationQueue,
                defaultTrafficLightConfiguration, true, true);
    }


}
