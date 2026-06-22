package com.example.movie_app_server.media.controller;

import com.example.movie_app_server.media.service.CloudinaryService;
import com.example.movie_app_server.media.service.ImageKitService;
import com.example.movie_app_server.media.service.LocalFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/upload")
@RequiredArgsConstructor
public class AdminUploadController {

    private final CloudinaryService cloudinaryService;
    private final ImageKitService imageKitService;
    private final LocalFileService localFileService;

    // API Up Ảnh -> Nhận file và trả về link ImageKit
    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        if (!isImage(file)) throw new com.example.movie_app_server.common.exception.AppException("Định dạng ảnh không hợp lệ", org.springframework.http.HttpStatus.BAD_REQUEST);
        String imageUrl = imageKitService.uploadImage(file);
        return ResponseEntity.ok(imageUrl);
    }

    // API Up Video -> Nhận file và lưu cục bộ, trả về link stream
    @PostMapping("/video")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) throws Exception {
        if (!isVideo(file)) throw new com.example.movie_app_server.common.exception.AppException("Định dạng video không hợp lệ", org.springframework.http.HttpStatus.BAD_REQUEST);
        String videoUrl = localFileService.storeFile(file);
        return ResponseEntity.ok(videoUrl);
    }

    // API Up Subtitle -> Nhận file (.vtt/.srt) và trả về link Cloudinary
    @PostMapping("/subtitle")
    public ResponseEntity<String> uploadSubtitle(@RequestParam("file") MultipartFile file) throws Exception {
        if (!isSubtitle(file)) throw new com.example.movie_app_server.common.exception.AppException("Chỉ chấp nhận file .srt hoặc .vtt", org.springframework.http.HttpStatus.BAD_REQUEST);
        String subtitleUrl = cloudinaryService.uploadSubtitle(file);
        return ResponseEntity.ok(subtitleUrl);
    }

    private boolean isImage(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) return false;
        name = name.toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp");
    }

    private boolean isVideo(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) return false;
        name = name.toLowerCase();
        return name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".avi");
    }

    private boolean isSubtitle(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) return false;
        name = name.toLowerCase();
        return name.endsWith(".srt") || name.endsWith(".vtt");
    }
}