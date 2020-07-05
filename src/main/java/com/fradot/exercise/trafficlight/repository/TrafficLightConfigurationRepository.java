package com.fradot.exercise.trafficlight.repository;

import com.fradot.exercise.trafficlight.model.TrafficLightConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrafficLightConfigurationRepository extends JpaRepository<TrafficLightConfiguration, Long> {

    @Query("SELECT c FROM TrafficLightConfiguration c WHERE c.active = true")
    List<TrafficLightConfiguration> findAllActiveConfigurations();

    @Query("SELECT c FROM TrafficLightConfiguration c WHERE c.active = false")
    List<TrafficLightConfiguration> findAllDisabledConfigurations();

    @Query("SELECT c FROM TrafficLightConfiguration c WHERE c.active IS NULL")
    List<TrafficLightConfiguration> findAllNewConfigurations();

    @Query("SELECT c FROM TrafficLightConfiguration c WHERE c.defaultConfiguration = true")
    Optional<TrafficLightConfiguration> findDefaultConfiguration();

}
