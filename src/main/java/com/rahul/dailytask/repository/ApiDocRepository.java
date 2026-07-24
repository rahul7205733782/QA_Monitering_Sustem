package com.rahul.dailytask.repository;

import com.rahul.dailytask.entity.ApiDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiDocRepository extends JpaRepository<ApiDoc, Long> {
    
    // Find by hospital name (contains, case insensitive)
    List<ApiDoc> findByHospitalNameContainingIgnoreCase(String hospitalName);
    
    // Find by template type (exact match)
    List<ApiDoc> findByTemplateType(String templateType);
    
    // Find by template type (contains, case insensitive)
    List<ApiDoc> findByTemplateTypeContainingIgnoreCase(String templateType);
    
    // Count by template type
    long countByTemplateType(String templateType);
}