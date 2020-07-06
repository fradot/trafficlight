package com.fradot.exercise.trafficlight.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link TrafficLightConfiguration}
 */
@RunWith(SpringRunner.class)
public class TrafficLightConfigurationTest {

    private static final String START_CRON = "0 00 18 ? * FRI";
    private static final String END_CRON = "0 14 18 ? * FRI";

    @TestConfiguration
    static class ModelConfiguration {

        @Bean
        public TrafficLightConfiguration trafficLightConfiguration() {
            return new TrafficLightConfiguration(1L, 1L, 1L, 1L, START_CRON, END_CRON, 6, false);
        }
    }

    @Autowired
    TrafficLightConfiguration trafficLightConfiguration;

    @Test
    public void itShouldBeEqualToAnObjectWithTheSameProperties() {
        TrafficLightConfiguration testConfiguration =
                new TrafficLightConfiguration(1L, 1L, 1L, 1L, START_CRON, END_CRON, 6, false);

        assertTrue(trafficLightConfiguration.equals(testConfiguration));
    }

    @Test
    public void itShouldBeDifferentFromAnObjectWithDifferentProperties() {
        TrafficLightConfiguration testConfiguration =
                new TrafficLightConfiguration(-1L, 1L, 1L, 1L, START_CRON, END_CRON, 6, true);

        assertTrue(!trafficLightConfiguration.equals(testConfiguration));
    }

    @Test
    public void itShouldCompareGreaterThanAnObjectWithLowestPriority() {
        TrafficLightConfiguration testConfiguration =
                new TrafficLightConfiguration(-1L, 1L, 1L, 1L, START_CRON, END_CRON, 5, false);

        assertThat(trafficLightConfiguration).isGreaterThan(testConfiguration);
    }

    @Test
    public void itShouldCompareLessThanAnObjectWithLowestPriority() {
        TrafficLightConfiguration testConfiguration =
                new TrafficLightConfiguration(-1L, 1L, 1L, 1L, START_CRON, END_CRON, 7, false);

        assertThat(trafficLightConfiguration).isLessThan(testConfiguration);
    }

    @Test
    public void itShouldCompareEqualToAnObjectWithTheSamePriorityAndSameProperties() {
        TrafficLightConfiguration testConfiguration =
                new TrafficLightConfiguration(-1L, 1L, 1L, 1L, START_CRON, END_CRON, 6, false);

        assertThat(trafficLightConfiguration).isLessThanOrEqualTo(testConfiguration);
    }
}
