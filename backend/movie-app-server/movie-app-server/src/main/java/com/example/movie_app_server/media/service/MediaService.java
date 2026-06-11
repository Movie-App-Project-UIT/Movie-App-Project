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
        List<Media> mediaList = mediaRepository.findByTitleContainingIgnoreCase(keyword);

        return mediaList.stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());
    }

    // --- LOGIC LỌC PHIM (ĐÃ XÓA HÀM BỊ LẶP) ---
    public Page<MediaItemDto> filterMedia(Long genreId, Long countryId, Long ageRatingId,
                                          Integer releaseYear, Boolean isPlayable, String mediaType, Pageable pageable) {
        Page<Media> mediaPage = mediaRepository.filterMediaDynamically(
                genreId, countryId, ageRatingId, releaseYear, isPlayable, mediaType, pageable);

        return mediaPage.map(this::convertToItemDto);
    }

    // --- LOGIC LẤY CHI TIẾT PHIM ---
    public MediaDetailResponse getMediaDetail(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phim", HttpStatus.NOT_FOUND));

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
                .directors(directors)
                .cast(cast)
                .subtitles(subtitles)
                .build();
    }

    // --- LOGIC PHÁT VIDEO KÈM KIỂM TRA QUYỀN VIP ---
    public String getPlayableVideoUrl(Long mediaId, String firebaseUid) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new AppException("Không tìm thấy phim", HttpStatus.NOT_FOUND));

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