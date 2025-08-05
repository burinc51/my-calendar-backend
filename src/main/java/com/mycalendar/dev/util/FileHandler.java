package com.mycalendar.dev.util;

import com.mycalendar.dev.enums.FileType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Component
public class FileHandler {

    private static String UPLOAD_DIR;

    @Value("${spring.path.file.upload}")
    private String uploadDir;

    public static String upload(MultipartFile file, FileType type, String existingFileName, String subDir) {
        if (file != null && !file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                String fileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));

                String newFileName;
                if (existingFileName != null) {
                    String[] parts = existingFileName.split("/");
                    String baseName = parts.length > 2 ? getFileNameWithoutExtension(parts[2]) : getFileNameWithoutExtension(existingFileName);
                    newFileName = baseName + fileExtension;
                } else {
                    newFileName = UUID.randomUUID() + fileExtension;
                }

                Path path = Paths.get((subDir != null ? UPLOAD_DIR + subDir : UPLOAD_DIR) + "/" + newFileName);
                Files.createDirectories(path.getParent());
                Files.write(path, bytes);

                return "/" + type.toString().toLowerCase() + subDir + "/" + newFileName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void delete(String url, String subDir) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        Path filePath = Paths.get(subDir != null ? UPLOAD_DIR + subDir : UPLOAD_DIR, fileName);

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete the file at " + filePath, e);
        }
    }

    public static String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
    }

    public static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex > 0) ? filename.substring(dotIndex) : "";
    }

    public static FileType getMimeType(String mimeType) {
        if (mimeType == null) return FileType.FILES;

        if (mimeType.startsWith("image/")) {
            return FileType.IMAGES;
        } else if (mimeType.startsWith("video/")) {
            return FileType.VIDEOS;
        } else if (mimeType.startsWith("application/")) {
            if (mimeType.contains("pdf") || mimeType.contains("word") ||
                    mimeType.contains("excel") || mimeType.contains("powerpoint") ||
                    mimeType.contains("officedocument")) {
                return FileType.DOCUMENTS;
            }
        } else if (mimeType.startsWith("text/")) {
            return FileType.DOCUMENTS;
        }

        return FileType.FILES;
    }

    @PostConstruct
    public void init() {
        UPLOAD_DIR = uploadDir;
    }
}