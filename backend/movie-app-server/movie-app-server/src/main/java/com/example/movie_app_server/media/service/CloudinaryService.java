package com.example.movie_app_server.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    @Value("${cloudinary.folder.videos:movie_app/videos}")
    private String videoFolder;

    @Value("${cloudinary.folder.subtitles:movie_app/subtitles}")
    private String subtitleFolder;

    // Hàm upload Video (Dành cho phim)
    public String uploadVideo(MultipartFile file) throws IOException {
        // Cấu hình tuỳ chỉnh siêu tiết kiệm: Chỉ lấy 720p (ép bitrate 1500k) và 480p (ép bitrate thấp 500k)
        String customHlsProfile = "c_limit,w_1280,h_720,vc_h264,br_1500k/c_limit,w_854,h_480,vc_h264,br_500k/f_m3u8";
        
        Map uploadResult = cloudinary.uploader().uploadLarge(file.getInputStream(),
                ObjectUtils.asMap(
                        "resource_type", "video", // BẮT BUỘC: Để Cloudinary biết đây là video và xử lý luồng phát
                        "folder", videoFolder, // Tạo thư mục cho gọn gàng
                        "eager", java.util.Arrays.asList(
                                new com.cloudinary.EagerTransformation().rawTransformation(customHlsProfile)
                        ),
                        "eager_async", true
                ));
        // Trả về link video dạng m3u8 (Adaptive Bitrate Streaming) thay vì mp4 gốc
        String originalUrl = uploadResult.get("secure_url").toString();
        
        // Cập nhật lại đường dẫn với cấu hình tuỳ chỉnh
        String hlsUrl = originalUrl.replace("/upload/", "/upload/" + customHlsProfile + "/");
        hlsUrl = hlsUrl.replaceAll("\\.(mp4|mkv|avi)$", ".m3u8");
        
        return hlsUrl;
    }

    // Hàm upload Video trực tiếp từ Google Drive Link
    public String uploadVideoFromDrive(String driveUrl) throws IOException {
        String fileId = null;
        if (driveUrl.contains("/d/")) {
            String[] parts = driveUrl.split("/d/");
            if (parts.length > 1) {
                fileId = parts[1].split("/")[0];
            }
        } else if (driveUrl.contains("id=")) {
            String[] parts = driveUrl.split("id=");
            if (parts.length > 1) {
                fileId = parts[1].split("&")[0];
            }
        }

        if (fileId == null) {
            throw new IllegalArgumentException("Không thể nhận diện ID từ Link Drive cung cấp.");
        }

        String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
        String customHlsProfile = "c_limit,w_1280,h_720,vc_h264,br_1500k/c_limit,w_854,h_480,vc_h264,br_500k/f_m3u8";

        java.net.URL url = new java.net.URL(downloadUrl);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        // Cấu hình HttpURLConnection để theo dõi các Redirects (Vì Google Drive hay chuyển hướng URL tải)
        connection.setInstanceFollowRedirects(true);

        try (java.io.InputStream inputStream = connection.getInputStream()) {
            Map uploadResult = cloudinary.uploader().uploadLarge(inputStream,
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder", videoFolder,
                            "eager", java.util.Arrays.asList(
                                    new com.cloudinary.EagerTransformation().rawTransformation(customHlsProfile)
                            ),
                            "eager_async", true
                    ));
            
            String originalUrl = uploadResult.get("secure_url").toString();
            String hlsUrl = originalUrl.replace("/upload/", "/upload/" + customHlsProfile + "/");
            hlsUrl = hlsUrl.replaceAll("\\.(mp4|mkv|avi|webm)$", ".m3u8");
            
            return hlsUrl;
        } finally {
            connection.disconnect();
        }
    }

    // Hàm upload file phụ đề (Dành cho .vtt, .srt)
    public String uploadSubtitle(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "raw", // BẮT BUỘC: Để tải lên file raw (text)
                        "folder", subtitleFolder
                ));
        return uploadResult.get("secure_url").toString();
    }

    // Hàm xóa file raw
    public void deleteRawFile(String fileUrl) {
        try {
            int uploadIndex = fileUrl.indexOf("/upload/");
            if (uploadIndex != -1) {
                String afterUpload = fileUrl.substring(uploadIndex + 8);
                int versionEndIndex = afterUpload.indexOf('/');
                if (versionEndIndex != -1 && afterUpload.substring(0, versionEndIndex).matches("v\\d+")) {
                    afterUpload = afterUpload.substring(versionEndIndex + 1);
                }
                cloudinary.uploader().destroy(afterUpload, ObjectUtils.asMap("resource_type", "raw"));
            }
        } catch (Exception e) {
            log.error("Lỗi khi xóa file trên Cloudinary", e);
        }
    }
}