package com.rahul.dailytask.service;

import com.rahul.dailytask.entity.ApiDoc;
import com.rahul.dailytask.repository.ApiDocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ApiDocService {

    @Autowired
    private ApiDocRepository apiDocRepository;

    // =========================================
    // GET ALL DOCS
    // =========================================
    public List<ApiDoc> getAllDocs() {
        return apiDocRepository.findAll();
    }

    // =========================================
    // GET DOC BY ID
    // =========================================
    public Optional<ApiDoc> getDocById(Long id) {
        return apiDocRepository.findById(id);
    }

    // =========================================
    // GET DOCS BY HOSPITAL NAME
    // =========================================
    public List<ApiDoc> getDocsByHospitalName(String hospitalName) {
        return apiDocRepository.findByHospitalNameContainingIgnoreCase(hospitalName);
    }

    // =========================================
    // GET DOCS BY TEMPLATE TYPE
    // =========================================
    public List<ApiDoc> getDocsByTemplateType(String templateType) {
        return apiDocRepository.findByTemplateTypeContainingIgnoreCase(templateType);
    }

    // =========================================
    // SAVE NEW DOC
    // =========================================
    public ApiDoc saveDoc(ApiDoc doc) {
        doc.setCreatedDate(LocalDateTime.now());
        doc.setUpdatedDate(null);
        return apiDocRepository.save(doc);
    }

    // =========================================
    // UPDATE EXISTING DOC
    // =========================================
    public ApiDoc updateDoc(Long id, ApiDoc docDetails) {
        System.out.println("📥 UPDATE REQUEST for ID: " + id);
        System.out.println("📝 Doc details: " + docDetails);

        ApiDoc existingDoc = apiDocRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doc not found with id: " + id));

        System.out.println("📝 Existing doc before update: " + existingDoc);

        // Update fields only if they are not null
        if (docDetails.getHospitalName() != null && !docDetails.getHospitalName().isEmpty()) {
            existingDoc.setHospitalName(docDetails.getHospitalName());
        }
        if (docDetails.getTemplateType() != null && !docDetails.getTemplateType().isEmpty()) {
            existingDoc.setTemplateType(docDetails.getTemplateType());
        }
        if (docDetails.getSheetLink() != null && !docDetails.getSheetLink().isEmpty()) {
            existingDoc.setSheetLink(docDetails.getSheetLink());
        }
        existingDoc.setUpdatedDate(LocalDateTime.now());

        ApiDoc updatedDoc = apiDocRepository.save(existingDoc);
        System.out.println("✅ Doc updated successfully: " + updatedDoc);

        return updatedDoc;
    }

    // =========================================
    // DELETE DOC
    // =========================================
    public void deleteDoc(Long id) {
        if (!apiDocRepository.existsById(id)) {
            throw new RuntimeException("Doc not found with id: " + id);
        }
        apiDocRepository.deleteById(id);
        System.out.println("🗑️ Doc deleted with ID: " + id);
    }

    // =========================================
    // GET STATISTICS
    // =========================================
    public long getTotalDocs() {
        return apiDocRepository.count();
    }

    public long getDocsByTemplate(String templateType) {
        return apiDocRepository.countByTemplateType(templateType);
    }

    public List<String> getAllTemplateTypes() {
        return apiDocRepository.findAll()
                .stream()
                .map(ApiDoc::getTemplateType)
                .distinct()
                .toList();
    }
}