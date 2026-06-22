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
        // Cấu hình tuỳ chỉnh: Chỉ lấy bản gốc (giới hạn ở 720p) và bản 480p
        String customHlsProfile = "c_limit,w_1280,h_720,vc_h264/c_limit,w_854,h_480,vc_h264/f_m3u8";
        
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