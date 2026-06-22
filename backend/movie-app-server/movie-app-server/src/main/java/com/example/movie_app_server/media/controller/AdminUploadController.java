package com.example.movie_app_server.media.controller;

import com.example.movie_app_server.media.service.CloudinaryService;
import com.example.movie_app_server.media.service.ImageKitService;
import com.example.movie_app_server.media.service.LocalFileService;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.media.entity.Media;
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
    private final MediaRepository mediaRepository;

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

    // API Up Video lên Cloudinary và ghi đè video_url của phim (nếu có mediaId)
    @PostMapping("/video/cloudinary")
    public ResponseEntity<String> uploadVideoCloudinary(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mediaId", required = false) Long mediaId) throws Exception {
        
        if (!isVideo(file)) throw new com.example.movie_app_server.common.exception.AppException("Định dạng video không hợp lệ", org.springframework.http.HttpStatus.BAD_REQUEST);
        
        String videoUrl = cloudinaryService.uploadVideo(file);
        
        if (mediaId != null) {
            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new com.example.movie_app_server.common.exception.AppException("Không tìm thấy phim", org.springframework.http.HttpStatus.NOT_FOUND));
            media.setVideoUrl(videoUrl);
            mediaRepository.save(media);
        }
        
        return ResponseEntity.ok(videoUrl);
    }

    // API Up Subtitle -> Nhận file (.vtt/.srt) và trả về link Cloudinary
    @PostMapping("/subtitle")
    public ResponseEntity<String> uploadSubtitle(@RequestParam("file") MultipartFile file) throws Exception {
        if (!isSubtitle(file)) throw new com.example.movie_app_server.common.exception.AppException("Chỉ chấp nhận file .srt hoặc .vtt", org.springframework.http.HttpStatus.BAD_REQUEST);
        String subtitleUrl = cloudinaryService.uploadSubtitle(file);
        return ResponseEntity.ok(subtitleUrl);
    }

    // API Up Video từ Google Drive Link (< 100MB)
    @PostMapping("/drive-video")
    public ResponseEntity<String> uploadVideoFromDrive(@RequestParam("driveUrl") String driveUrl) throws Exception {
        if (driveUrl == null || driveUrl.trim().isEmpty()) {
            throw new com.example.movie_app_server.common.exception.AppException("Link Drive không được để trống", org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        String videoUrl = cloudinaryService.uploadVideoFromDrive(driveUrl);
        return ResponseEntity.ok(videoUrl);
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