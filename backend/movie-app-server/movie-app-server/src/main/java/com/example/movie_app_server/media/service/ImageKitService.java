package com.example.movie_app_server.media.service;

import io.imagekit.client.ImageKitClient;
import io.imagekit.models.files.FileUploadParams;
import io.imagekit.models.files.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageKitService {

    // Spring Boot sẽ tự động gắn cái Bean cấu hình lúc nãy vào đây
    private final ImageKitClient imageKitClient;

    public String uploadImage(MultipartFile file) throws Exception {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // Chuẩn bị gói hàng để gửi sang ImageKit
        FileUploadParams params = FileUploadParams.builder()
                .file(file.getInputStream()) // Lấy luồng dữ liệu của file ảnh
                .fileName(fileName)
                .folder("/movie_images") // Bỏ vào thư mục cho gọn
                .build();

        // Bấm nút gửi và hứng kết quả
        FileUploadResponse response = imageKitClient.files().upload(params);

        // Trả về đường link
        return response.url().get();
    }
}