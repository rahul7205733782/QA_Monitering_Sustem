package com.rahul.dailytask.repository;

import com.rahul.dailytask.entity.ResponseSheetHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseSheetHistoryRepository extends JpaRepository<ResponseSheetHistory, Long> {

    List<ResponseSheetHistory> findAllByOrderByGeneratedDateDesc();

    List<ResponseSheetHistory> findByUploadedBy(String uploadedBy);
}