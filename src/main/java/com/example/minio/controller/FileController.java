package com.example.minio.controller;

import com.example.minio.service.MinioService;
import io.minio.StatObjectResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private MinioService minioService;

    /**
     * Upload file endpoint
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file size (100MB limit)
            if (file.getSize() > 100 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "File size exceeds 100MB limit");
                return ResponseEntity.badRequest().body(response);
            }

            // Upload file
            String fileName = minioService.uploadFile(file);

            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("fileName", fileName);
            response.put("originalName", file.getOriginalFilename());
            response.put("size", file.getSize());
            response.put("contentType", file.getContentType());

            logger.info("File uploaded successfully: {}", fileName);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error uploading file: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get files endpoint
     */
    @GetMapping("")
    public ResponseEntity<List<String>> getFiles() {
        try {
            List<String> files = minioService.listAllFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            logger.error("Error downloading file: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Download file endpoint
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName) {
        try {
            InputStream inputStream = minioService.downloadFile(fileName + ".jpg");
            StatObjectResponse fileInfo = minioService.getFileInfo(fileName + ".jpg");

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.add(HttpHeaders.CONTENT_TYPE, fileInfo.contentType());
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileInfo.size()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            logger.error("Error downloading file: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get file info endpoint
     */
    @GetMapping("{fileName}")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String fileName) {
        Map<String, Object> response = new HashMap<>();

        try {
            StatObjectResponse fileInfo = minioService.getFileInfo(fileName + ".jpg");

            response.put("success", true);
            response.put("fileName", fileName);
            response.put("size", fileInfo.size());
            response.put("contentType", fileInfo.contentType());
            response.put("lastModified", fileInfo.lastModified());
            response.put("etag", fileInfo.etag());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting file info: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "File not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete file endpoint
     */
    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String fileName) {
        Map<String, Object> response = new HashMap<>();

        try {
            minioService.deleteFile(fileName);

            response.put("success", true);
            response.put("message", "File deleted successfully");
            response.put("fileName", fileName);

            logger.info("File deleted successfully: {}", fileName);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting file: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to delete file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "MinIO File Service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
