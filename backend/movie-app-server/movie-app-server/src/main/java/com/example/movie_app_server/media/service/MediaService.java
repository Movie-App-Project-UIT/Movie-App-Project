package com.example.movie_app_server.media.service;

import com.example.movie_app_server.common.exception.AppException;
import com.example.movie_app_server.media.dto.CreditDto;
import com.example.movie_app_server.media.dto.MediaDetailResponse;
import com.example.movie_app_server.media.dto.MediaItemDto;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service trung tâm xử lý logic duyệt phim, tìm kiếm và lọc phim.
 */
@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final UserService userService;

    // --- HÀM TIỆN ÍCH DÙNG CHUNG ---
    public MediaItemDto convertToItemDto(Media media) {
        return MediaItemDto.builder()
                .id(media.getId())
                .title(media.getTitle())
                .posterUrl(media.getPosterPath() != null ? "https://image.tmdb.org/t/p/w342" + media.getPosterPath() : null)
                .backdropUrl(media.getBackdropPath() != null ? "https://image.tmdb.org/t/p/w780" + media.getBackdropPath() : null)
                .voteAverage(media.getVoteAverage())
                .isPremium(media.isPremium())
                .mediaType(media.getMediaType().name())
                .isPlayable(media.getVideoUrl() != null && !media.getVideoUrl().trim().isEmpty()) // Trim khoảng trắng an toàn hơn
                .build();
    }

    // --- LOGIC LẤY DANH SÁCH & TÌM KIẾM ---
    public List<MediaItemDto> searchMedia(String keyword) {
        List<Media> mediaList = mediaRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(keyword);

        return mediaList.stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());
    }

    // --- LOGIC LỌC PHIM ---
    public Page<MediaItemDto> filterMedia(Long genreId, Long countryId, Long ageRatingId,
                                          Integer releaseYear, Boolean isPlayable, String mediaType, Pageable pageable) {
        
        java.time.LocalDate startDate = null;
        java.time.LocalDate endDate = null;
        if (releaseYear != null) {
            startDate = java.time.LocalDate.of(releaseYear, 1, 1);
            endDate = java.time.LocalDate.of(releaseYear, 12, 31);
        }

        Page<Media> mediaPage = mediaRepository.filterMediaDynamically(
                genreId, countryId, ageRatingId, startDate, endDate, isPlayable, mediaType, pageable);

        return mediaPage.map(this::convertToItemDto);
    }

    // --- LOGIC LẤY DỮ LIỆU TRANG CHỦ ---
    public java.util.Map<String, List<MediaItemDto>> getHomepageData() {
        List<MediaItemDto> topRated = mediaRepository.findTop10ByIsDeletedFalseOrderByVoteAverageDesc().stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());

        List<MediaItemDto> recentlyAdded = mediaRepository.findTop10ByIsDeletedFalseOrderByIdDesc().stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());

        // Đổi trending thành topRated theo yêu cầu của user
        List<MediaItemDto> trending = topRated; 

        java.util.Map<String, List<MediaItemDto>> response = new java.util.HashMap<>();
        response.put("trending", trending);
        response.put("topRated", topRated);
        response.put("recentlyAdded", recentlyAdded);

        return response;
    }

    // --- LOGIC LẤY CHI TIẾT PHIM ---
    public MediaDetailResponse getMediaDetail(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phim", HttpStatus.NOT_FOUND));

        if (media.isDeleted()) {
            throw new AppException("Không tìm thấy phim", HttpStatus.NOT_FOUND);
        }

        String tmdbImageBaseUrl = "https://image.tmdb.org/t/p/w185";

        List<CreditDto> directors = media.getCredits().stream()
                .filter(c -> "Directing".equals(c.getDepartment()))
                .map(c -> CreditDto.builder()
                        .name(c.getName())
                        .profileUrl(c.getProfilePath() != null ? tmdbImageBaseUrl + c.getProfilePath() : null)
                        .build())
                .toList();

        List<CreditDto> cast = media.getCredits().stream()
                .filter(c -> "Acting".equals(c.getDepartment()))
                .map(c -> CreditDto.builder()
                        .name(c.getName())
                        .characterName(c.getCharacterName())
                        .profileUrl(c.getProfilePath() != null ? tmdbImageBaseUrl + c.getProfilePath() : null)
                        .build())
                .toList();

        List<com.example.movie_app_server.media.dto.SubtitleDto> subtitles = media.getSubtitles().stream()
                .map(sub -> com.example.movie_app_server.media.dto.SubtitleDto.builder()
                        .id(sub.getId())
                        .language(sub.getLanguage())
                        .fileUrl(sub.getFileUrl())
                        .build())
                .toList();

        return MediaDetailResponse.builder()
                .id(media.getId())
                .title(media.getTitle())
                .overview(media.getOverview())
                .voteAverage(media.getVoteAverage())
                .posterUrl(media.getPosterPath() != null ? "https://image.tmdb.org/t/p/w500" + media.getPosterPath() : null)
                .backdropUrl(media.getBackdropPath() != null ? "https://image.tmdb.org/t/p/w1280" + media.getBackdropPath() : null)
                .releaseYear(media.getReleaseDate() != null ? media.getReleaseDate().getYear() : null)
                .isPremium(media.isPremium())
                .mediaType(media.getMediaType() != null ? media.getMediaType().name() : null)
                .genre(media.getGenres() != null && !media.getGenres().isEmpty() ? media.getGenres().iterator().next().getName() : "Không có")
                .country(media.getCountry() != null ? media.getCountry().getName() : "Không có")
                .language(media.getLanguage() != null ? media.getLanguage() : "Đang cập nhật")
                .viewCount((int) (Math.random() * 1000) + 100) // Dummy view count
                .duration(120) // Dummy duration since we don't have it in Media entity
                .directors(directors)
                .cast(cast)
                .subtitles(subtitles)
                .build();
    }

    // --- LOGIC PHÁT VIDEO KÈM KIỂM TRA QUYỀN VIP ---
    public String getPlayableVideoUrl(Long mediaId, String firebaseUid) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new AppException("Không tìm thấy phim", HttpStatus.NOT_FOUND));

        if (media.isDeleted()) {
            throw new AppException("Không tìm thấy phim", HttpStatus.NOT_FOUND);
        }

        if (media.isPremium()) {
            if (!userService.hasPremiumAccess(firebaseUid)) {
                throw new AppException("YÊU CẦU_NÂNG_CẤP: Bạn cần gói Premium để xem phim này", HttpStatus.PAYMENT_REQUIRED);
            }
        }

        if (media.getVideoUrl() == null || media.getVideoUrl().trim().isEmpty()) {
            throw new AppException("Phim hiện chưa có link xem", HttpStatus.NO_CONTENT);
        }

        return media.getVideoUrl();
    }
}