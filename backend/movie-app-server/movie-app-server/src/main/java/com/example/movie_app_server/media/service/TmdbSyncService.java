package com.example.movie_app_server.media.service;

import com.example.movie_app_server.common.exception.AppException;
import com.example.movie_app_server.core.client.TmdbClient;
import com.example.movie_app_server.media.dto.tmdb.TmdbCreditsResponseDto;
import com.example.movie_app_server.media.dto.tmdb.TmdbMovieDetailsDto;
import com.example.movie_app_server.media.dto.tmdb.TmdbSeasonDetailsDto;
import com.example.movie_app_server.media.dto.tmdb.TmdbTvDetailsDto;
import com.example.movie_app_server.media.entity.Credit;
import com.example.movie_app_server.media.entity.Episode;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.entity.Season;
import com.example.movie_app_server.media.entity.enums.MediaType;
import com.example.movie_app_server.media.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TmdbSyncService {

    private final TmdbClient tmdbClient;
    private final MediaRepository mediaRepository;
    private final com.example.movie_app_server.media.repository.GenreRepository genreRepository;
    private final com.example.movie_app_server.media.repository.CountryRepository countryRepository;

    public Media previewMovieFromTmdb(Integer tmdbId) {
        TmdbMovieDetailsDto movieDto = tmdbClient.getMovieDetails(tmdbId, "vi-VN");
        TmdbCreditsResponseDto creditsDto = tmdbClient.getMovieCredits(tmdbId);

        // Determine if we need to fetch English fallback
        boolean titleNeedsFallback = !("vi".equals(movieDto.getOriginalLanguage())) 
                                  && !("en".equals(movieDto.getOriginalLanguage())) 
                                  && movieDto.getTitle() != null 
                                  && movieDto.getTitle().equals(movieDto.getOriginalTitle());
        boolean overviewNeedsFallback = movieDto.getOverview() == null || movieDto.getOverview().isEmpty();

        if (titleNeedsFallback || overviewNeedsFallback) {
            TmdbMovieDetailsDto englishDto = tmdbClient.getMovieDetails(tmdbId, "en-US");
            if (titleNeedsFallback && englishDto.getTitle() != null && !englishDto.getTitle().isEmpty()) {
                movieDto.setTitle(englishDto.getTitle());
            }
            if (overviewNeedsFallback && englishDto.getOverview() != null && !englishDto.getOverview().isEmpty()) {
                movieDto.setOverview(englishDto.getOverview());
            }
        }

        String trailerUrl = null;
        if (movieDto.getVideos() != null && movieDto.getVideos().getResults() != null) {
            trailerUrl = movieDto.getVideos().getResults().stream()
                    .filter(v -> "YouTube".equals(v.getSite()) && "Trailer".equals(v.getType()))
                    .findFirst()
                    .map(v -> "https://www.youtube.com/embed/" + v.getKey())
                    .orElse(null);
        }

        Media media = Media.builder()
                .tmdbId(movieDto.getId())
                .title(movieDto.getTitle())
                .overview(movieDto.getOverview())
                .posterPath(movieDto.getPosterPath())
                .backdropPath(movieDto.getBackdropPath())
                .releaseDate(movieDto.getReleaseDate() != null && !movieDto.getReleaseDate().isEmpty()
                        ? LocalDate.parse(movieDto.getReleaseDate()) : null)
                .voteAverage(movieDto.getVoteAverage())
                .mediaType(MediaType.MOVIE)
                .language(movieDto.getOriginalLanguage() != null ? java.util.Locale.of(movieDto.getOriginalLanguage()).getDisplayLanguage(java.util.Locale.of("vi", "VN")) : null)
                .trailerUrl(trailerUrl)
                .duration(movieDto.getRuntime())
                .build();

        // Map Genres
        if (movieDto.getGenres() != null) {
            for (var gDto : movieDto.getGenres()) {
                genreRepository.findByTmdbGenreId(gDto.getId()).ifPresent(media.getGenres()::add);
            }
        }

        // Map Country
        if (movieDto.getProductionCountries() != null && !movieDto.getProductionCountries().isEmpty()) {
            var firstCountry = movieDto.getProductionCountries().get(0);
            countryRepository.findByIsoCode(firstCountry.getIso31661()).ifPresent(media::setCountry);
        }

        List<Credit> creditEntities = new ArrayList<>();

        if (creditsDto.getCrew() != null) {
            creditsDto.getCrew().stream()
                    .filter(crew -> "Directing".equals(crew.getDepartment()) || "Director".equals(crew.getJob()))
                    .forEach(crew -> creditEntities.add(Credit.builder()
                            .tmdbPersonId(crew.getId())
                            .name(crew.getName())
                            .department("Directing")
                            .profilePath(crew.getProfilePath())
                            .media(media)
                            .build()));
        }

        if (creditsDto.getCast() != null) {
            creditsDto.getCast().stream().limit(15).forEach(cast ->
                    creditEntities.add(Credit.builder()
                            .tmdbPersonId(cast.getId())
                            .name(cast.getName())
                            .characterName(cast.getCharacter())
                            .department("Acting")
                            .profilePath(cast.getProfilePath())
                            .media(media)
                            .build())
            );
        }

        media.setCredits(creditEntities);
        return media;
    }

    @Transactional
    public Media syncMovieFromTmdb(Integer tmdbId, String videoUrl, boolean isPremium) {
        if (mediaRepository.findByTmdbId(tmdbId).isPresent()) {
            throw new AppException("Phim này đã tồn tại trong hệ thống!", HttpStatus.CONFLICT);
        }
        
        Media media = previewMovieFromTmdb(tmdbId);
        media.setVideoUrl(videoUrl);
        media.setPremium(isPremium);
        
        return mediaRepository.save(media);
    }

    public Media previewTvSeriesFromTmdb(Integer tmdbId) {
        TmdbTvDetailsDto tvDto = tmdbClient.getTvDetails(tmdbId, "vi-VN");
        TmdbCreditsResponseDto creditsDto = tmdbClient.getTvCredits(tmdbId);

        // Determine if we need to fetch English fallback
        boolean titleNeedsFallback = !("vi".equals(tvDto.getOriginalLanguage())) 
                                  && !("en".equals(tvDto.getOriginalLanguage())) 
                                  && tvDto.getName() != null 
                                  && tvDto.getName().equals(tvDto.getOriginalName());
        boolean overviewNeedsFallback = tvDto.getOverview() == null || tvDto.getOverview().isEmpty();

        if (titleNeedsFallback || overviewNeedsFallback) {
            TmdbTvDetailsDto englishDto = tmdbClient.getTvDetails(tmdbId, "en-US");
            if (titleNeedsFallback && englishDto.getName() != null && !englishDto.getName().isEmpty()) {
                tvDto.setName(englishDto.getName());
            }
            if (overviewNeedsFallback && englishDto.getOverview() != null && !englishDto.getOverview().isEmpty()) {
                tvDto.setOverview(englishDto.getOverview());
            }
        }

        String trailerUrl = null;
        if (tvDto.getVideos() != null && tvDto.getVideos().getResults() != null) {
            trailerUrl = tvDto.getVideos().getResults().stream()
                    .filter(v -> "YouTube".equals(v.getSite()) && "Trailer".equals(v.getType()))
                    .findFirst()
                    .map(v -> "https://www.youtube.com/embed/" + v.getKey())
                    .orElse(null);
        }

        Media media = Media.builder()
                .tmdbId(tvDto.getId())
                .title(tvDto.getName())
                .overview(tvDto.getOverview())
                .posterPath(tvDto.getPosterPath())
                .backdropPath(tvDto.getBackdropPath())
                .releaseDate(tvDto.getFirstAirDate() != null && !tvDto.getFirstAirDate().isEmpty()
                        ? LocalDate.parse(tvDto.getFirstAirDate()) : null)
                .voteAverage(tvDto.getVoteAverage())
                .mediaType(MediaType.TV_SERIES)
                .language(tvDto.getOriginalLanguage() != null ? java.util.Locale.of(tvDto.getOriginalLanguage()).getDisplayLanguage(java.util.Locale.of("vi", "VN")) : null)
                .trailerUrl(trailerUrl) // Thêm trailerUrl
                .isPremium(false)
                .build();

        // Map Genres
        if (tvDto.getGenres() != null) {
            for (var gDto : tvDto.getGenres()) {
                genreRepository.findByTmdbGenreId(gDto.getId()).ifPresent(media.getGenres()::add);
            }
        }

        // Map Country
        if (tvDto.getProductionCountries() != null && !tvDto.getProductionCountries().isEmpty()) {
            var firstCountry = tvDto.getProductionCountries().get(0);
            countryRepository.findByIsoCode(firstCountry.getIso31661()).ifPresent(media::setCountry);
        }

        List<Credit> creditEntities = new ArrayList<>();
        if (creditsDto.getCrew() != null) {
            creditsDto.getCrew().stream().filter(crew -> "Directing".equals(crew.getDepartment()) || "Director".equals(crew.getJob()))
                    .forEach(crew -> creditEntities.add(Credit.builder().tmdbPersonId(crew.getId()).name(crew.getName())
                            .department("Directing").profilePath(crew.getProfilePath()).media(media).build()));
        }
        if (creditsDto.getCast() != null) {
            creditsDto.getCast().stream().limit(15).forEach(cast -> creditEntities.add(Credit.builder().tmdbPersonId(cast.getId())
                    .name(cast.getName()).characterName(cast.getCharacter()).department("Acting")
                    .profilePath(cast.getProfilePath()).media(media).build()));
        }
        media.setCredits(creditEntities);

        return media;
    }

    @Transactional
    public Media syncTvSeriesFromTmdb(Integer tmdbId) {
        if (mediaRepository.findByTmdbId(tmdbId).isPresent()) {
            throw new AppException("Phim bộ này đã tồn tại trong hệ thống!", HttpStatus.CONFLICT);
        }
        
        Media media = previewTvSeriesFromTmdb(tmdbId);
        TmdbTvDetailsDto tvDto = tmdbClient.getTvDetails(tmdbId, "vi-VN");

        List<Season> seasonEntities = new ArrayList<>();
        if (tvDto.getSeasons() != null) {
            for (TmdbTvDetailsDto.TmdbSeasonBasicDto basicSeason : tvDto.getSeasons()) {
                if (basicSeason.getSeasonNumber() == 0) continue;

                TmdbSeasonDetailsDto seasonDto = tmdbClient.getSeasonDetails(tmdbId, basicSeason.getSeasonNumber());

                Season season = Season.builder()
                        .seasonNumber(seasonDto.getSeasonNumber())
                        .title(seasonDto.getName())
                        .posterPath(seasonDto.getPosterPath())
                        .media(media)
                        .build();

                List<Episode> episodeEntities = new ArrayList<>();
                if (seasonDto.getEpisodes() != null) {
                    for (TmdbSeasonDetailsDto.TmdbEpisodeDto epDto : seasonDto.getEpisodes()) {
                        episodeEntities.add(Episode.builder()
                                .episodeNumber(epDto.getEpisodeNumber())
                                .title(epDto.getName())
                                .overview(epDto.getOverview())
                                .stillPath(epDto.getStillPath())
                                .duration(epDto.getRuntime())
                                .season(season)
                                .build());
                    }
                }
                season.setEpisodes(episodeEntities);
                seasonEntities.add(season);
            }
        }
        media.setSeasons(seasonEntities);
        return mediaRepository.save(media);
    }
}