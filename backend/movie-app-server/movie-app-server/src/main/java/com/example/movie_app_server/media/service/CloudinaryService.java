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
        Map uploadResult = cloudinary.uploader().uploadLarge(file.getInputStream(),
                ObjectUtils.asMap(
                        "resource_type", "video", // BẮT BUỘC: Để Cloudinary biết đây là video và xử lý luồng phát
                        "folder", videoFolder // Tạo thư mục cho gọn gàng
                ));
        // Trả về link video (secure_url là link https)
        return uploadResult.get("secure_url").toString();
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