package com.example.webshop.service;

import com.example.webshop.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    private static final byte[] PNG_HEADER = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @TempDir
    Path uploadDir;

    @Test
    void storesImageUnderRandomNameAndDeletesIt() {
        FileStorageService service = new FileStorageService(uploadDir.toString());
        MockMultipartFile file = new MockMultipartFile("imageFile", "../../evil.png", "image/png", PNG_HEADER);

        String publicPath = service.store(file);

        assertThat(publicPath).startsWith("/uploads/").endsWith(".png").doesNotContain("evil");
        Path stored = uploadDir.resolve(publicPath.substring("/uploads/".length()));
        assertThat(Files.exists(stored)).isTrue();

        service.deleteIfStored(publicPath);
        assertThat(Files.exists(stored)).isFalse();
    }

    @Test
    void rejectsFilesThatAreNotImagesEvenWithImageContentType() {
        FileStorageService service = new FileStorageService(uploadDir.toString());
        MockMultipartFile file = new MockMultipartFile("imageFile", "photo.png", "image/png",
                "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.store(file)).isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsEmptyFile() {
        FileStorageService service = new FileStorageService(uploadDir.toString());
        MockMultipartFile file = new MockMultipartFile("imageFile", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service.store(file)).isInstanceOf(BusinessException.class);
    }
}
