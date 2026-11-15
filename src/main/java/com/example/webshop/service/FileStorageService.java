package com.example.webshop.service;

import com.example.webshop.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Stores uploaded product images on disk in a safe way:
 * - the file type is detected from the file content (magic bytes), not from the client supplied name,
 * - only JPEG, PNG, GIF and WebP images are accepted (no SVG/HTML, which could carry scripts),
 * - the stored file gets a random name, so user input never becomes part of a file system path.
 */
@Service
@Slf4j
public class FileStorageService {

    public static final String PUBLIC_PREFIX = "/uploads/";

    private static final Pattern STORED_NAME = Pattern.compile("^[a-f0-9-]{36}\\.(jpg|png|gif|webp)$");

    private final Path root;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /** Stores the image and returns its public path (e.g. /uploads/3f2c....jpg). */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("upload.error.empty");
        }
        try {
            String extension = detectImageExtension(file);
            if (extension == null) {
                throw new BusinessException("upload.error.type");
            }
            String filename = UUID.randomUUID() + "." + extension;
            Path target = root.resolve(filename).normalize();
            if (!target.startsWith(root)) {
                throw new BusinessException("upload.error.failed");
            }
            Files.createDirectories(root);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target);
            }
            return PUBLIC_PREFIX + filename;
        } catch (IOException ex) {
            log.error("Could not store uploaded file", ex);
            throw new BusinessException("upload.error.failed");
        }
    }

    /** Deletes a previously uploaded image. Paths that were not created by this service are ignored. */
    public void deleteIfStored(String publicPath) {
        if (publicPath == null || !publicPath.startsWith(PUBLIC_PREFIX)) {
            return;
        }
        String filename = publicPath.substring(PUBLIC_PREFIX.length());
        if (!STORED_NAME.matcher(filename).matches()) {
            return;
        }
        try {
            Files.deleteIfExists(root.resolve(filename));
        } catch (IOException ex) {
            log.warn("Could not delete image {}", filename, ex);
        }
    }

    private String detectImageExtension(MultipartFile file) throws IOException {
        byte[] header;
        try (InputStream in = file.getInputStream()) {
            header = in.readNBytes(12);
        }
        if (startsWith(header, 0xFF, 0xD8, 0xFF)) {
            return "jpg";
        }
        if (startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "png";
        }
        String ascii = new String(header, StandardCharsets.US_ASCII);
        if (ascii.startsWith("GIF87a") || ascii.startsWith("GIF89a")) {
            return "gif";
        }
        if (header.length >= 12 && ascii.startsWith("RIFF") && ascii.startsWith("WEBP", 8)) {
            return "webp";
        }
        return null;
    }

    private boolean startsWith(byte[] data, int... signature) {
        if (data.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((data[i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
