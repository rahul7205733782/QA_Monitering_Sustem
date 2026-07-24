package com.rahul.dailytask.repository;

import com.rahul.dailytask.entity.BugSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugSheetRepository extends JpaRepository<BugSheet, Long> {

    List<BugSheet> findByOrderByCreatedDateDesc();

    List<BugSheet> findByHospitalNameOrderByCreatedDateDesc(String hospitalName);
}