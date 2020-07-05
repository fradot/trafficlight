package com.fradot.exercise.trafficlight.repository;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrafficLightConfigurationRepository extends JpaRepository<TrafficLightConfiguration, Long> {

    @Query("SELECT c FROM TrafficLightConfiguration c WHERE c.active = true AND c.toBeDisabled=false AND c.toBeEnabled=false")
    List<TrafficLightConfiguration> findAllActiveConfigurations();

    @Query("SELECT c FROM TrafficLightConfiguration c WHERE c.active = false AND c.toBeDisabled=false AND c.toBeEnabled=false")
    List<TrafficLightConfiguration> findAllDisabledConfigurations();

    @Query("SELECT c FROM TrafficLightConfiguration c WHERE c.active = false AND c.toBeEnabled = true AND c.toBeDisabled = false")
    List<TrafficLightConfiguration> findAllConfigurationsToBeEnabled();

    @Query("SELECT c FROM TrafficLightConfiguration c WHERE c.active = true AND c.toBeEnabled = false AND c.toBeDisabled = true")
    List<TrafficLightConfiguration> findAllConfigurationsToBeDisabled();

    @Query("SELECT c FROM TrafficLightConfiguration c WHERE c.defaultConfiguration = true " +
            "AND c.active = true AND c.toBeDisabled=false AND c.toBeEnabled=false")
    Optional<TrafficLightConfiguration> findDefaultConfiguration();

}
