package com.rahul.dailytask.repository;

import com.rahul.dailytask.entity.JourneyMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JourneyMapRepository extends JpaRepository<JourneyMap, Long> {

    List<JourneyMap> findByOrderByCreatedDateDesc();

    List<JourneyMap> findByNameContainingIgnoreCase(String name);

    List<JourneyMap> findByModuleContainingIgnoreCase(String module);

    List<JourneyMap> findByStatus(String status);
}