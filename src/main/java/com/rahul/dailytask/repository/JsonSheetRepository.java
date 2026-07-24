package com.rahul.dailytask.repository;

import com.rahul.dailytask.entity.JsonSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JsonSheetRepository extends JpaRepository<JsonSheet, Long> {

    List<JsonSheet> findByOrderByCreatedDateDesc();

    List<JsonSheet> findByModuleNameContainingIgnoreCase(String moduleName);
}