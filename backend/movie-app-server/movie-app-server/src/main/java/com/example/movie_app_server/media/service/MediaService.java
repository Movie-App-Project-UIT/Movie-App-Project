package com.example.movie_app_server.media.service;

import com.example.movie_app_server.common.exception.AppException;
import com.example.movie_app_server.media.dto.*;
import com.example.movie_app_server.media.entity.Episode;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.entity.Season;
import com.example.movie_app_server.media.repository.EpisodeRepository;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.media.repository.SeasonRepository;
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
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
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
                .genres(media.getGenres().stream().map(g -> g.getName()).collect(Collectors.toList()))
                .isDeleted(media.isDeleted())
                .language(media.getLanguage() != null ? media.getLanguage() : "N/A")
                .country(media.getCountry() != null ? media.getCountry().getName() : "N/A")
                .hiddenByGenreId(media.getHiddenByGenreId())
                // TODO: XÓA ĐOẠN FAKE DATA NÀY KHI CÓ HỆ THỐNG ĐẾM LƯỢT XEM THỰC TẾ
                .viewCount(media.getId() != null ? (int) (media.getId() * 1234 % 50000) : 0) // Tạo lượt xem giả ngẫu nhiên nhưng cố định theo ID phim
                .build();
    }

    // --- LOGIC LẤY DANH SÁCH & TÌM KIẾM ---
    public List<MediaItemDto> searchMedia(String query) {
        return mediaRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(query, org.springframework.data.domain.PageRequest.of(0, 20)).stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());
    }

    // --- LOGIC LỌC PHIM ---
    public Page<MediaItemDto> filterMedia(String keyword, Long genreId, Long countryId, Long ageRatingId,
                                          Integer releaseYear, Boolean isPlayable, String mediaType, Pageable pageable) {
        
        java.time.LocalDate startDate = null;
        java.time.LocalDate endDate = null;
        if (releaseYear != null) {
            startDate = java.time.LocalDate.of(releaseYear, 1, 1);
            endDate = java.time.LocalDate.of(releaseYear, 12, 31);
        }

        Page<Media> mediaPage = mediaRepository.filterMediaDynamically(
                keyword, genreId, countryId, ageRatingId, startDate, endDate, isPlayable, mediaType, pageable);

        return mediaPage.map(this::convertToItemDto);
    }

    // --- LOGIC LẤY DỮ LIỆU TRANG CHỦ ---
    public java.util.Map<String, List<MediaItemDto>> getHomepageData() {
        // Lấy 10 phim được đánh giá cao nhất (bắt buộc >= 8.0)
        List<MediaItemDto> topRated = mediaRepository.findTop10ByIsDeletedFalseAndVoteAverageGreaterThanEqualOrderByVoteAverageDesc(8.0f).stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());

        List<MediaItemDto> recentlyAdded = mediaRepository.findTop10ByIsDeletedFalseOrderByIdDesc().stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());

        // Lấy danh sách Trending: top 10 phim được xem nhiều nhất trong 7 ngày qua
        List<MediaItemDto> trending = mediaRepository
                .findTrendingMedia(java.time.LocalDateTime.now().minusDays(7), org.springframework.data.domain.PageRequest.of(0, 10))
                .stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());

        // Nếu hệ thống mới chạy chưa có lượt xem nào → dùng danh sách theo lượt xem giả lập làm mặc định
        if (trending.isEmpty()) {
            trending = mediaRepository.findAll().stream()
                    .filter(m -> !m.isDeleted())
                    .map(this::convertToItemDto)
                    .sorted((a, b) -> Integer.compare(b.getViewCount(), a.getViewCount()))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        java.util.Map<String, List<MediaItemDto>> response = new java.util.HashMap<>();
        response.put("trending", trending);
        response.put("topRated", topRated);
        response.put("recentlyAdded", recentlyAdded);

        return response;
    }

    // --- LOGIC LẤY CHI TIẾT PHIM (DÀNH CHO NGƯỜI DÙNG) ---
    public MediaDetailResponse getMediaDetail(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phim", HttpStatus.NOT_FOUND));

        if (media.isDeleted()) {
            throw new AppException("Không tìm thấy phim", HttpStatus.NOT_FOUND);
        }

        return convertToDetailResponse(media);
    }

    // --- LOGIC LẤY CHI TIẾT PHIM (DÀNH CHO ADMIN) ---
    public MediaDetailResponse getMediaDetailAdmin(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phim", HttpStatus.NOT_FOUND));
        return convertToDetailResponse(media);
    }

    public void incrementViewCount(Long mediaId) {
        Media media = mediaRepository.findById(mediaId).orElseThrow(() -> new AppException("Không tìm thấy phim", HttpStatus.NOT_FOUND));
        media.setViewCount((media.getViewCount() == null ? 0 : media.getViewCount()) + 1);
        mediaRepository.save(media);
    }

    public MediaDetailResponse convertToDetailResponse(Media media) {
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

        List<SubtitleDto> subtitles = media.getSubtitles().stream()
                .map(sub -> SubtitleDto.builder()
                        .id(sub.getId())
                        .language(sub.getLanguage())
                        .fileUrl(sub.getFileUrl())
                        .build())
                .toList();

        // Chuyển đổi danh sách thể loại thành List<String> tên thể loại
        List<String> genreNames = media.getGenres().stream()
                .map(g -> g.getName())
                .toList();

        // Chuyển đổi danh sách Seasons (kèm Episodes) cho phim bộ
        List<SeasonDto> seasonDtos = media.getSeasons().stream()
                .map(this::convertToSeasonDto)
                .toList();

        return MediaDetailResponse.builder()
                .id(media.getId())
                .tmdbId(media.getTmdbId())
                .title(media.getTitle())
                .overview(media.getOverview())
                .voteAverage(media.getVoteAverage())
                .posterUrl(media.getPosterPath() != null ? "https://image.tmdb.org/t/p/w500" + media.getPosterPath() : null)
                .backdropUrl(media.getBackdropPath() != null ? "https://image.tmdb.org/t/p/w1280" + media.getBackdropPath() : null)
                .releaseDate(media.getReleaseDate())
                .releaseYear(media.getReleaseDate() != null ? media.getReleaseDate().getYear() : null)
                .viewCount(media.getViewCount())
                .favoriteCount(media.getWatchlists() != null ? media.getWatchlists().size() : 0)
                .mediaType(media.getMediaType().name())
                .isPremium(media.isPremium())
                .isDeleted(media.isDeleted())
                .trailerUrl(media.getTrailerUrl())
                .duration(media.getDuration())
                .language(media.getLanguage())
                .countryName(media.getCountry() != null ? media.getCountry().getName() : null)
                .ageRating(media.getAgeRating() != null ? media.getAgeRating().getName() : null)
                .genres(genreNames)
                .directors(directors)
                .cast(cast)
                .subtitles(subtitles)
                .seasons(seasonDtos)
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

    // --- LOGIC LẤY DANH SÁCH SEASONS ---
    public List<SeasonDto> getSeasonsByMediaId(Long mediaId) {
        // Kiểm tra phim có tồn tại không
        if (!mediaRepository.existsById(mediaId)) {
            throw new AppException("Không tìm thấy phim", HttpStatus.NOT_FOUND);
        }
        return seasonRepository.findByMediaIdOrderBySeasonNumberAsc(mediaId).stream()
                .map(this::convertToSeasonDto)
                .toList();
    }

    public SeasonDto getSeasonById(Long seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new AppException("Không tìm thấy phần phim", HttpStatus.NOT_FOUND));
        return convertToSeasonDto(season);
    }

    // --- LOGIC LẤY DANH SÁCH EPISODES ---
    public List<EpisodeDto> getEpisodesBySeasonId(Long seasonId) {
        if (!seasonRepository.existsById(seasonId)) {
            throw new AppException("Không tìm thấy phần phim", HttpStatus.NOT_FOUND);
        }
        return episodeRepository.findBySeasonIdOrderByEpisodeNumberAsc(seasonId).stream()
                .map(this::convertToEpisodeDto)
                .toList();
    }

    public EpisodeDto getEpisodeById(Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new AppException("Không tìm thấy tập phim", HttpStatus.NOT_FOUND));
        return convertToEpisodeDto(episode);
    }

    // --- HÀM CHUYỂN ĐỔI DÙNG CHUNG ---
    private SeasonDto convertToSeasonDto(Season season) {
        List<EpisodeDto> episodeDtos = season.getEpisodes().stream()
                .map(this::convertToEpisodeDto)
                .toList();

        return SeasonDto.builder()
                .id(season.getId())
                .seasonNumber(season.getSeasonNumber())
                .title(season.getTitle())
                .overview(season.getOverview())
                .posterUrl(season.getPosterPath() != null ? "https://image.tmdb.org/t/p/w342" + season.getPosterPath() : null)
                .episodes(episodeDtos)
                .build();
    }

    public EpisodeDto convertToEpisodeDto(Episode episode) {
        List<SubtitleDto> subtitleDtos = episode.getSubtitles().stream()
                .map(sub -> SubtitleDto.builder()
                        .id(sub.getId())
                        .language(sub.getLanguage())
                        .fileUrl(sub.getFileUrl())
                        .build())
                .toList();

        return EpisodeDto.builder()
                .id(episode.getId())
                .episodeNumber(episode.getEpisodeNumber())
                .title(episode.getTitle())
                .overview(episode.getOverview())
                .stillUrl(episode.getStillPath() != null ? "https://image.tmdb.org/t/p/w300" + episode.getStillPath() : null)
                .duration(episode.getDuration())
                .isPlayable(episode.getVideoUrl() != null && !episode.getVideoUrl().trim().isEmpty())
                .subtitles(subtitleDtos)
                .build();
    }
}