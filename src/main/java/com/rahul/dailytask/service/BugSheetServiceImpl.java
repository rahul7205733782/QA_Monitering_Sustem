package com.rahul.dailytask.service;

import com.rahul.dailytask.entity.BugSheet;
import com.rahul.dailytask.entity.Hospital;
import com.rahul.dailytask.repository.BugSheetRepository;
import com.rahul.dailytask.repository.HospitalRepository;
import com.rahul.dailytask.service.BugSheetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BugSheetServiceImpl implements BugSheetService {

    private final BugSheetRepository bugSheetRepository;
    private final HospitalRepository hospitalRepository;

    @Value("${file.upload-dir:uploads/bugtracker}")
    private String uploadDir;

    public BugSheetServiceImpl(BugSheetRepository bugSheetRepository,
                               HospitalRepository hospitalRepository) {
        this.bugSheetRepository = bugSheetRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @Override
    public BugSheet uploadSheet(Long hospitalId,
                                String sheetName,
                                String uploadedBy,
                                MultipartFile file) throws IOException {

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));

        String extension = "";

        String originalName = file.getOriginalFilename();

        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        String storedFileName = UUID.randomUUID() + extension;

        Path folder = Paths.get(uploadDir);

        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }

        Files.copy(
                file.getInputStream(),
                folder.resolve(storedFileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        BugSheet sheet = new BugSheet();

        sheet.setHospital(hospital);
        sheet.setSheetName(sheetName);
        sheet.setOriginalFileName(originalName);
        sheet.setStoredFileName(storedFileName);
        sheet.setUploadedBy(uploadedBy);
        sheet.setUploadDate(LocalDateTime.now());

        return bugSheetRepository.save(sheet);
    }

    @Override
    public List<BugSheet> getSheetsByHospital(Long hospitalId) {

        return bugSheetRepository.findByHospitalIdOrderByUploadDateDesc(hospitalId);

    }

    @Override
    public Resource downloadSheet(Long id) throws IOException {

        BugSheet sheet = bugSheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sheet not found"));

        Path path = Paths.get(uploadDir)
                .resolve(sheet.getStoredFileName());

        return new UrlResource(path.toUri());

    }

    @Override
    public void deleteSheet(Long id) throws IOException {

        BugSheet sheet = bugSheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sheet not found"));

        Path path = Paths.get(uploadDir)
                .resolve(sheet.getStoredFileName());

        Files.deleteIfExists(path);

        bugSheetRepository.delete(sheet);

    }

}