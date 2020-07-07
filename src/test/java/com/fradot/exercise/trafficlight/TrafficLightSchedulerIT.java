package com.fradot.exercise.trafficlight;

import com.fradot.exercise.trafficlight.scheduler.TrafficLightScheduler;
import com.fradot.exercise.trafficlight.statemachine.TrafficLightState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static java.lang.Thread.sleep;
import static org.mockito.Mockito.*;

/**
 * Test for {@link TrafficLightScheduler} class
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class TrafficLightSchedulerIT {

    @Autowired
    private TrafficLightScheduler trafficLightScheduler;

    @MockBean
    private Runnable myRunnable;

    @Test(expected = Exception.class)
    public void itShouldThrowAnExceptionIfTheCronExpressionIsNotValid() throws Exception {
        trafficLightScheduler.addCronTask("id", () -> {;}, "* * * 0 9 8 2");
    }


    @Test
    public void itShouldScheduleARunnableTaskToBeExecutedAtTheScheduledInterval() throws Exception {
        trafficLightScheduler.addCronTask("id", myRunnable, "0/2 * * 1/1 * ?");
        sleep(2500);
        verify(myRunnable, times(1)).run();
    }

    @Test(expected = Exception.class)
    public void itShouldThrowAnExceptionIfTryingCancellingANonExistingTask() throws Exception {
        trafficLightScheduler.cancelCronTask("id2");
    }

}
