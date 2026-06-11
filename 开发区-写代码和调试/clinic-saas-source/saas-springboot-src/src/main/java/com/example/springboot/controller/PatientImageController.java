package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.PatientImage;
import com.example.springboot.service.PatientImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/patient-images")
public class PatientImageController {

    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/.local/uploads/patient-images/";

    @Autowired
    private PatientImageService service;

    @GetMapping("/selectByPatientId")
    public Result selectByPatientId(@RequestParam Long patientId) {
        return Result.success(service.selectByPatientId(patientId));
    }

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file,
                         @RequestParam Long patientId,
                         @RequestParam String patientName,
                         @RequestParam(required = false, defaultValue = "其他") String imageType,
                         @RequestParam(required = false) String imageDate,
                         @RequestParam(required = false) String notes) {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + ext;
            Path dest = Paths.get(UPLOAD_DIR + filename);
            file.transferTo(dest.toFile());

            PatientImage img = new PatientImage();
            img.setPatient_id(patientId);
            img.setPatient_name(patientName);
            img.setImage_name(original);
            img.setImage_type(imageType);
            img.setFile_path(filename);
            img.setNotes(notes);
            img.setSent_to_patient(Boolean.FALSE);
            img.setSent_at(null);
            if (imageDate != null && !imageDate.isEmpty()) {
                img.setImage_date(java.sql.Date.valueOf(imageDate));
            } else {
                img.setImage_date(new java.sql.Date(new Date().getTime()));
            }
            service.add(img);
            return Result.success("上传成功");
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable Long id) {
        PatientImage img = service.selectById(id);
        if (img == null) return ResponseEntity.notFound().build();
        File file = new File(UPLOAD_DIR + img.getFile_path());
        if (!file.exists()) return ResponseEntity.notFound().build();
        Resource resource = new FileSystemResource(file);
        String contentType = "application/octet-stream";
        try { contentType = Files.probeContentType(file.toPath()); } catch (Exception ignored) {}
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + img.getImage_name() + "\"")
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }

    @PostMapping("/send/{id}")
    public Result sendToPatient(@PathVariable Long id) {
        PatientImage image = service.markSentToPatient(id);
        if (image == null) {
            return Result.error("影像不存在");
        }
        return Result.success(image);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        PatientImage img = service.selectById(id);
        if (img != null) {
            new File(UPLOAD_DIR + img.getFile_path()).delete();
            service.delete(id);
        }
        return Result.success("删除成功");
    }
}
