package com.fradot.exercise.trafficlight.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class represents a Traffic Light configuration. It defines how long each state should be displayed and
 * which priority this configuration has over other overlapping configurations.
 *
 * <code>startCronExpression</code>     is used to define the periodic start time of this configuration
 * <code>endCronExpression</code>       defines the periodic end time of this configuration
 * <code>priority</code>                the configuration priority defines the behaviour of the application when two or
 *                      more configurations are active at the same time. In this case,
 *                      the configuration with highest priority will be used.
 * <code>defaultConfiguration</code>    A Default configuration is always active and has the lowest priority.
 */
@Entity
@Table(name = TrafficLightConfiguration.TABLE)
public class TrafficLightConfiguration implements Serializable, Comparable<TrafficLightConfiguration> {

    public static final String TABLE = "tl_configuration";
    private static final AtomicLong seq = new AtomicLong(0);

    @Transient
    private Long thisSeq;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @Column(name="green_duration_in_seconds", nullable = false)
    private Long greenDuration;

    @Column(name="red_duration_in_seconds", nullable = false)
    private Long redDuration;

    @Column(name="orange_duration_in_seconds", nullable = false)
    private Long orangeDuration;

    @Column(name="start_cron_expression")
    private String startCronExpression;

    @Column(name="end_cron_expression")
    private String endCronExpression;

    @Column(name="priority", nullable = false)
    private Integer priority;

    @Column(name="default_configuration", nullable = false)
    private Boolean defaultConfiguration;

    @Column(name="active", nullable = false)
    private Boolean active;

    @Column(name="to_be_enabled", nullable = false)
    private Boolean toBeEnabled;

    @Column(name="to_be_disabled", nullable = false)
    private Boolean toBeDisabled;

    public TrafficLightConfiguration() {
        this.thisSeq = seq.incrementAndGet();
    }

    public TrafficLightConfiguration(Long id, Long greenDuration, Long redDuration, Long orangeDuration,
                                     String startCronExpression, String endCronExpression, Integer priority,
                                     Boolean defaultConfiguration) {
        this.id = id;
        this.greenDuration = greenDuration;
        this.redDuration = redDuration;
        this.orangeDuration = orangeDuration;
        this.startCronExpression = startCronExpression;
        this.endCronExpression = endCronExpression;
        this.priority = priority;
        this.defaultConfiguration = defaultConfiguration;
        this.thisSeq = seq.incrementAndGet();

        //TODO: raise an IllegalStateException if seq is greater than the max number of configurations allowed.

    }

    public TrafficLightConfiguration(Long id, Long greenDuration, Long redDuration, Long orangeDuration,
                                     String startCronExpression, String endCronExpression, Integer priority,
                                     Boolean defaultConfiguration, Boolean active, Boolean toBeEnabled, Boolean toBeDisabled) {
        this.id = id;
        this.greenDuration = greenDuration;
        this.redDuration = redDuration;
        this.orangeDuration = orangeDuration;
        this.startCronExpression = startCronExpression;
        this.endCronExpression = endCronExpression;
        this.priority = priority;
        this.defaultConfiguration = defaultConfiguration;
        this.toBeEnabled = toBeEnabled;
        this.toBeDisabled = toBeDisabled;
        this.active = active;
        this.thisSeq = seq.incrementAndGet();

        //TODO: raise an IllegalStateException if seq is greater than the max number of configurations allowed.
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGreenDuration() {
        return greenDuration;
    }

    public void setGreenDuration(Long greenDuration) {
        this.greenDuration = greenDuration;
    }

    public Long getRedDuration() {
        return redDuration;
    }

    public void setRedDuration(Long redDuration) {
        this.redDuration = redDuration;
    }

    public Long getOrangeDuration() {
        return orangeDuration;
    }

    public void setOrangeDuration(Long orangeDuration) {
        this.orangeDuration = orangeDuration;
    }

    public String getStartCronExpression() {
        return startCronExpression;
    }

    public void setStartCronExpression(String startCronExpression) {
        this.startCronExpression = startCronExpression;
    }

    public String getEndCronExpression() {
        return endCronExpression;
    }

    public void setEndCronExpression(String endCronExpression) {
        this.endCronExpression = endCronExpression;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean isDefaultConfiguration() {
        return this.defaultConfiguration;
    }

    public void setDefaultConfiguration() {
        this.defaultConfiguration = defaultConfiguration;
    }

    public Long getSeq() {
        return thisSeq;
    }

    public Boolean getToBeEnabled() {
        return toBeEnabled;
    }

    public void setToBeEnabled(Boolean toBeEnabled) {
        this.toBeEnabled = toBeEnabled;
    }

    public Boolean getToBeDisabled() {
        return toBeDisabled;
    }

    public void setToBeDisabled(Boolean toBeDisabled) {
        this.toBeDisabled = toBeDisabled;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        TrafficLightConfiguration that = (TrafficLightConfiguration) other;
        return Objects.equals(id, that.id) &&
                Objects.equals(greenDuration, that.greenDuration) &&
                Objects.equals(redDuration, that.redDuration) &&
                Objects.equals(orangeDuration, that.orangeDuration) &&
                Objects.equals(startCronExpression, that.startCronExpression) &&
                Objects.equals(endCronExpression, that.endCronExpression) &&
                Objects.equals(priority, that.priority) &&
                Objects.equals(defaultConfiguration, that.defaultConfiguration);

    }

    @Override
    public int hashCode() {
        return Objects.hash(id, greenDuration, redDuration, orangeDuration, startCronExpression,
                endCronExpression, priority, defaultConfiguration);
    }

    @Override
    public String toString() {
        return "TrafficLightConfiguration{" +
                "id=" + id +
                ", greenDuration=" + greenDuration +
                ", redDuration=" + redDuration +
                ", orangeDuration=" + orangeDuration +
                ", startCronExpression='" + startCronExpression + '\'' +
                ", endCronExpression='" + endCronExpression + '\'' +
                ", priority=" + priority +
                ", defaultConfiguration=" + defaultConfiguration +
                ", active=" + toBeEnabled +
                '}';
    }


    @Override
    public int compareTo(TrafficLightConfiguration other) {

        if(other != null && !other.equals(this)) {
            if (other.getPriority() > this.priority)
                return -1;
            else if(other.getPriority() < this.priority)
                return 1;
            else if(other.getPriority().equals(this.priority))
                return other.getSeq() > this.thisSeq ? -1 : 1;
        }
        else if(other != null && other.equals(this))
            return 0;
        else if(other == null)
            return 1;

        return 0;
    }
}
