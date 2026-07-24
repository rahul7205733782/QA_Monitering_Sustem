package com.rahul.dailytask.service;

import com.rahul.dailytask.entity.BugSheet;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BugSheetService {

    BugSheet uploadSheet(Long hospitalId,
                         String sheetName,
                         String uploadedBy,
                         MultipartFile file) throws IOException;

    List<BugSheet> getSheetsByHospital(Long hospitalId);

    Resource downloadSheet(Long id) throws IOException;

    void deleteSheet(Long id) throws IOException;

}