package com.taskmanagment.app.serviceImpl;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.taskmanagment.app.Exceptions.BadRequestException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FileStorageUtil {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Set<String> ALLOWED_ATTACHMENT_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/plain", "text/csv"
    );

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Stores a file under uploadDir/{subDir}/UUID_originalName.
     *
     * @return relative path (subDir/UUID_filename)
     */
    public String storeFile(MultipartFile file, String subDir) {
        return storeFile(file, subDir, ALLOWED_ATTACHMENT_TYPES);
    }

    public String storeAvatarFile(MultipartFile file) {
        return storeFile(file, "avatars", ALLOWED_IMAGE_TYPES);
    }

    private String storeFile(MultipartFile file, String subDir, Set<String> allowedTypes) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new BadRequestException("File type not allowed: " + contentType);
        }

        String originalName = Objects.requireNonNull(file.getOriginalFilename(), "Filename missing");
        String safeOriginal = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedName   = UUID.randomUUID() + "_" + safeOriginal;

        try {
            Path dir = Paths.get(uploadDir, subDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
            return subDir + "/" + storedName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalName, e);
        }
    }

    /** Loads a file as a Spring Resource for streaming download. */
    public Resource loadFileAsResource(String relativePath) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(relativePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new BadRequestException("File not found or not readable: " + relativePath);
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid file path: " + relativePath);
        }
    }

    /** Deletes a file by its relative path. Silently ignores missing files. */
    public void deleteFile(String relativePath) {
        if (relativePath == null) return;
        try {
            Path filePath = Paths.get(uploadDir).resolve(relativePath).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", relativePath, e);
        }
    }
}
