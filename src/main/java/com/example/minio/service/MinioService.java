package com.example.minio.service;

import com.example.minio.config.MinioProperties;
import io.minio.*;
import io.minio.messages.Item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MinioService {

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties minioProperties;

    /**
     * List all files in bucket
     */
    public List<String> listAllFiles() throws Exception {
        try {
            List<String> fileNames = new ArrayList<>();

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .recursive(true) // để lấy tất cả, kể cả trong "folder"
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                fileNames.add(item.objectName());
            }

            logger.info("Retrieved {} files from bucket {}", fileNames.size(), minioProperties.getBucketName());
            return fileNames;
        } catch (Exception e) {
            logger.error("Error listing files: {}", e.getMessage());
            throw new Exception("Failed to list files: " + e.getMessage());
        }
    }

    /**
     * Upload file to MinIO
     */
    public String uploadFile(MultipartFile file) throws Exception {
        try {
            // Check if bucket exists, if not create it
            createBucketIfNotExists();

            // Generate unique filename
            String fileName = generateUniqueFileName(file.getOriginalFilename());

            // Upload file
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            logger.info("File uploaded successfully: {}", fileName);
            return fileName;

        } catch (Exception e) {
            logger.error("Error uploading file: {}", e.getMessage());
            throw new Exception("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Download file from MinIO
     */
    public InputStream downloadFile(String fileName) throws Exception {
        try {
            Thread.sleep(1000);
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            logger.error("Error downloading file: {}", e.getMessage());
            throw new Exception("Failed to download file: " + e.getMessage());
        }
    }

    /**
     * Delete file from MinIO
     */
    public void deleteFile(String fileName) throws Exception {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .build());
            logger.info("File deleted successfully: {}", fileName);
        } catch (Exception e) {
            logger.error("Error deleting file: {}", e.getMessage());
            throw new Exception("Failed to delete file: " + e.getMessage());
        }
    }

    /**
     * Get file info
     */
    public StatObjectResponse getFileInfo(String fileName) throws Exception {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            logger.error("Error getting file info: {}", e.getMessage());
            throw new Exception("Failed to get file info: " + e.getMessage());
        }
    }

    /**
     * Create bucket if it doesn't exist
     */
    private void createBucketIfNotExists() throws Exception {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .build());

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .build());
                logger.info("Bucket created: {}", minioProperties.getBucketName());
            }
        } catch (Exception e) {
            logger.error("Error creating bucket: {}", e.getMessage());
            throw new Exception("Failed to create bucket: " + e.getMessage());
        }
    }

    /**
     * Generate unique filename
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
