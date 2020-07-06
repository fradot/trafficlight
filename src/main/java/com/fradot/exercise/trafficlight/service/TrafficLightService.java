package com.fradot.exercise.trafficlight.service;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import com.fradot.exercise.trafficlight.repository.TrafficLightConfigurationRepository;
import com.fradot.exercise.trafficlight.scheduler.TrafficLightRunnableTask;
import com.fradot.exercise.trafficlight.scheduler.TrafficLightScheduler;
import com.sun.istack.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

/** This class maintains the logic to handle the {@link TrafficLightConfiguration} life cycle. */
@Service
@Transactional
public class TrafficLightService {

    @Value("${trafficlight.database.synch.cron}")
    private String synchCron;

    private static final Logger log = LoggerFactory.getLogger(TrafficLightService.class);
    public static final String ENABLING = "enabling";
    public static final String DISABLING = "disabling";

    private TrafficLightScheduler trafficLightScheduler;
    private PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue;
    private TrafficLightConfigurationRepository trafficLightConfigurationRepository;

    @Autowired
    public TrafficLightService(
            TrafficLightScheduler trafficLightScheduler,
            PriorityBlockingQueue<TrafficLightConfiguration> trafficLightConfigurationQueue,
            TrafficLightConfigurationRepository trafficLightConfigurationRepository) {
        this.trafficLightScheduler = trafficLightScheduler;
        this.trafficLightConfigurationQueue = trafficLightConfigurationQueue;
        this.trafficLightConfigurationRepository = trafficLightConfigurationRepository;
    }

    public void synchWithDatabase() {
        this.enableConfigurations(this.trafficLightConfigurationRepository.findAllConfigurationsToBeEnabled());
        this.disableConfigurations(this.trafficLightConfigurationRepository.findAllConfigurationsToBeDisabled());
    }

    @Transactional
    private void disableConfigurations(@NotNull List<TrafficLightConfiguration> trafficLightConfigurationList) {

        for (TrafficLightConfiguration configuration : trafficLightConfigurationList) {
            if (!configuration.isDefaultConfiguration()
                    && configuration.getActive()
                    && configuration.getToBeDisabled()
                    && !configuration.getToBeEnabled()) {

                // remove tasks
                trafficLightScheduler.deleteCronTask(configuration.getId() + DISABLING);
                trafficLightScheduler.deleteCronTask(configuration.getId() + ENABLING);

                // remove from the queue
                if (trafficLightConfigurationQueue.contains(configuration)) {
                    trafficLightConfigurationQueue.remove(configuration);
                }

                configuration.setActive(false);
                configuration.setToBeDisabled(false);
                configuration.setToBeEnabled(false);

                trafficLightConfigurationRepository.save(configuration);
            }
        }
    }

    @Transactional
    private void enableConfigurations(@NotNull List<TrafficLightConfiguration> trafficLightConfigurationList) {

        // Schedule enabling and disabling tasks
        for (TrafficLightConfiguration configuration : trafficLightConfigurationList) {
            if (!configuration.isDefaultConfiguration()
                    && !configuration.getActive()
                    && !configuration.getToBeDisabled()
                    && configuration.getToBeEnabled()) {
                // enabling task
                if (configuration.getStartCronExpression() == null
                        || (configuration.getStartCronExpression() != null
                                && configuration.getStartCronExpression().isEmpty())) {
                    log.warn("Start cron expression is not defined, configuration will never be activated!");
                } else {
                    String enablingTaskId = configuration.getId().toString() + ENABLING;
                    TrafficLightRunnableTask enabling = new TrafficLightRunnableTask(
                            this.trafficLightConfigurationQueue, configuration, true, false);
                    trafficLightScheduler.addCronTask(enablingTaskId, enabling, configuration.getStartCronExpression());
                }

                // disabling task
                if (configuration.getEndCronExpression() == null
                        || (configuration.getEndCronExpression() != null
                                && configuration.getEndCronExpression().isEmpty())) {
                    log.warn("Start cron expression is not defined, configuration will never be activated!");
                } else {
                    TrafficLightRunnableTask disabling = new TrafficLightRunnableTask(
                            this.trafficLightConfigurationQueue, configuration, false, true);
                    String disablingTaskId = configuration.getId().toString() + DISABLING;
                    trafficLightScheduler.addCronTask(disablingTaskId, disabling, configuration.getEndCronExpression());
                }

                configuration.setActive(true);
                configuration.setToBeDisabled(false);
                configuration.setToBeEnabled(false);

                this.trafficLightConfigurationRepository.save(configuration);
            }
        }
    }

    private void enableAllActiveConfigurationsAtStartup() {
        log.info("Loading all configurations from the database");
        Optional<TrafficLightConfiguration> defaultConfiguration =
                trafficLightConfigurationRepository.findDefaultConfiguration();

        if (defaultConfiguration.isPresent()) {
            trafficLightConfigurationQueue.add(defaultConfiguration.get());
            List<TrafficLightConfiguration> trafficLightConfigurationList =
                    trafficLightConfigurationRepository.findAllActiveConfigurations();

            this.enableConfigurations(trafficLightConfigurationList.stream()
                    .filter(c -> !c.isDefaultConfiguration())
                    .map(c -> {
                        c.setActive(false);
                        c.setToBeEnabled(true);
                        return c;
                    })
                    .collect(Collectors.toList()));
        } else {
            throw new IllegalStateException("Default configuration not present in the database");
        }
    }

    @PostConstruct
    public void init() {
        trafficLightScheduler.addStartupTask(this::enableAllActiveConfigurationsAtStartup);
        trafficLightScheduler.addStartupTask(
                () -> trafficLightScheduler.addCronTask("synch", this::synchWithDatabase, synchCron));
    }
}
