package com.example.movie_app_server.media.controller;

import com.example.movie_app_server.media.dto.MediaDetailResponse;
import com.example.movie_app_server.media.dto.MediaItemDto;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller phục vụ các màn hình hiển thị danh sách phim và chi tiết phim trên App.
 */
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    // API: GET /api/v1/media/{id} -> Lấy toàn bộ thông tin chi tiết của 1 bộ phim
    @GetMapping("/{id}")
    public ResponseEntity<MediaDetailResponse> getMediaById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaDetail(id));
    }

    // API: GET /api/v1/media/search?keyword=batman -> Tìm phim theo tên
    @GetMapping("/search")
    public ResponseEntity<List<MediaItemDto>> searchMedia(@RequestParam String keyword) {
        return ResponseEntity.ok(mediaService.searchMedia(keyword));
    }

    /**
     * API: GET /api/v1/media/filter
     * Tác dụng: lấy danh sách phim cho mọi màn hình.
     * Cung cấp khả năng lọc động kết hợp (vd: Hành động + Hàn Quốc + Có video xem được).
     * Có hỗ trợ Phân trang (Pagination) và Sắp xếp (Sort).
     */
    @GetMapping("/filter")
    public ResponseEntity<Page<MediaItemDto>> filterMedia(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> genreIds,
            @RequestParam(required = false) List<Long> countryIds,
            @RequestParam(required = false) List<String> languages,
            @RequestParam(required = false) Long ageRatingId,
            @RequestParam(required = false) Integer releaseYear,
            @RequestParam(required = false) Boolean isPlayable,
            @RequestParam(required = false) String mediaType, // THÊM DÒNG NÀY (Đại diện cho các Tab)
            @RequestParam(required = false) Boolean isPremium,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "Mới nhất") String sortBy) {

        Sort sort = Sort.unsorted();
        switch (sortBy) {
            case "Mới nhất": sort = Sort.by(Sort.Direction.DESC, "releaseDate", "id"); break;
            case "Cũ nhất": sort = Sort.by(Sort.Direction.ASC, "releaseDate", "id"); break;
            case "Điểm TMDB": sort = Sort.by(Sort.Direction.DESC, "voteAverage"); break;
        }

        return ResponseEntity.ok(mediaService.filterMedia(
                keyword, genreIds, countryIds, languages, ageRatingId, releaseYear, isPlayable, mediaType, isPremium, PageRequest.of(page, size, sort)));
    }

    /**
     * API: GET /api/v1/media/home
     * Lấy dữ liệu cho trang chủ (Trending, Top Rated, Recently Added)
     */
    @GetMapping("/home")
    public ResponseEntity<java.util.Map<String, List<MediaItemDto>>> getHomepageData() {
        return ResponseEntity.ok(mediaService.getHomepageData());
    }

    private String getUid() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    @GetMapping("/{id}/play")
    public ResponseEntity<String> playMovie(@PathVariable Long id) {
        // Truyền cả ID phim và ID người dùng xuống Service để kiểm tra
        String videoUrl = mediaService.getPlayableVideoUrl(id, getUid());
        return ResponseEntity.ok(videoUrl);
    }

    @GetMapping("/{id}/episodes/{episodeId}/play")
    public ResponseEntity<String> playEpisode(@PathVariable Long id, @PathVariable Long episodeId) {
        String videoUrl = mediaService.getPlayableEpisodeUrl(id, episodeId, getUid());
        return ResponseEntity.ok(videoUrl);
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long id) {
        mediaService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }
}