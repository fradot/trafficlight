package com.fradot.exercise.trafficlight.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class TrafficLightTrigger implements Trigger {

    private static final Logger log = LoggerFactory.getLogger(TrafficLightTrigger.class);

    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        // TODO: refactor
        if(triggerContext.lastActualExecutionTime() != null) {
            LocalDateTime ldt = convertToLocalDateTime(triggerContext.lastActualExecutionTime());
            return Date.from(ldt.atZone(ZoneId.systemDefault()).plusSeconds(2).toInstant());
        }

        return new Date();
    }

    public LocalDateTime convertToLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
