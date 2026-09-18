package com.rahul.dailytask.service;

import com.rahul.dailytask.entity.BugSheet;
import com.rahul.dailytask.entity.Hospital;
import com.rahul.dailytask.repository.BugSheetRepository;
import com.rahul.dailytask.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class BugSheetServiceImpl implements BugSheetService {

    private final BugSheetRepository bugSheetRepository;
    private final HospitalRepository hospitalRepository;

    @Value("${file.upload-dir:uploads/bugtracker}")
    private String uploadDir;

    public BugSheetServiceImpl(
            BugSheetRepository bugSheetRepository,
            HospitalRepository hospitalRepository) {

        this.bugSheetRepository = bugSheetRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @Override
    public BugSheet uploadSheet(
            Long hospitalId,
            String sheetName,
            String uploadedBy,
            MultipartFile file) throws IOException {

        // Find hospital
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() ->
                        new RuntimeException("Hospital not found"));

        // Original file name
        String originalName = file.getOriginalFilename();

        // File extension
        String extension = "";

        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(
                    originalName.lastIndexOf("."));
        }

        // Generate unique file name
        String storedFileName = UUID.randomUUID() + extension;

        // Upload folder
        Path folder = Paths.get(uploadDir);

        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }

        // Save file
        Files.copy(
                file.getInputStream(),
                folder.resolve(storedFileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        // Create BugSheet
        BugSheet sheet = new BugSheet();

        // Store hospital ID and hospital name
        sheet.setHospitalId(hospitalId);
        sheet.setHospitalName(hospital.getName());

        // Store sheet information
        sheet.setSheetName(sheetName);
        sheet.setOriginalFileName(originalName);
        sheet.setStoredFileName(storedFileName);
        sheet.setUploadedBy(uploadedBy);

        // Dates
        sheet.setCreatedDate(LocalDateTime.now());
        sheet.setUploadDate(LocalDateTime.now());
        sheet.setUploadTime(LocalTime.now());

        // Save
        return bugSheetRepository.save(sheet);
    }

    @Override
    public List<BugSheet> getSheetsByHospital(Long hospitalId) {

        // Find hospital
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() ->
                        new RuntimeException("Hospital not found"));

        // Get hospital name
        String hospitalName = hospital.getName();

        // Use existing repository method
        return bugSheetRepository
                .findByHospitalNameOrderByCreatedDateDesc(hospitalName);
    }

    @Override
    public Resource downloadSheet(Long id) throws IOException {

        BugSheet sheet = bugSheetRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Sheet not found"));

        Path path = Paths.get(uploadDir)
                .resolve(sheet.getStoredFileName());

        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("File not found");
        }

        return resource;
    }

    @Override
    public void deleteSheet(Long id) throws IOException {

        BugSheet sheet = bugSheetRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Sheet not found"));

        Path path = Paths.get(uploadDir)
                .resolve(sheet.getStoredFileName());

        Files.deleteIfExists(path);

        bugSheetRepository.delete(sheet);
    }
}