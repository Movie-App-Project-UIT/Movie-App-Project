package com.example.movie_app_server.core.config;

import com.example.movie_app_server.core.client.TmdbClient;
import com.example.movie_app_server.media.dto.tmdb.TmdbCountryDto;
import com.example.movie_app_server.media.dto.tmdb.TmdbGenreResponseDto;
import com.example.movie_app_server.media.entity.Country;
import com.example.movie_app_server.media.entity.Genre;
import com.example.movie_app_server.media.repository.CountryRepository;
import com.example.movie_app_server.media.repository.GenreRepository;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.media.service.TmdbSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import org.springframework.core.annotation.Order;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class TmdbDataSeeder implements CommandLineRunner {

    private final TmdbClient tmdbClient;
    private final GenreRepository genreRepository;
    private final CountryRepository countryRepository;
    private final MediaRepository mediaRepository;
    private final TmdbSyncService tmdbSyncService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting TMDB Data Seeder...");
        syncGenres();
        syncCountries();
        syncPopularMovies();
        log.info("TMDB Data Seeder completed.");
    }

    private void syncGenres() {
        try {
            Set<Integer> existingGenreIds = new HashSet<>();
            genreRepository.findAll().forEach(g -> existingGenreIds.add(g.getTmdbGenreId()));

            // Fetch Movie Genres
            TmdbGenreResponseDto movieGenres = tmdbClient.getMovieGenres();
            if (movieGenres != null && movieGenres.getGenres() != null) {
                for (TmdbGenreResponseDto.TmdbGenreDto dto : movieGenres.getGenres()) {
                    if (!existingGenreIds.contains(dto.getId())) {
                        Genre genre = Genre.builder()
                                .tmdbGenreId(dto.getId())
                                .name(dto.getName())
                                .build();
                        genreRepository.save(genre);
                        existingGenreIds.add(dto.getId());
                    }
                }
            }

            // Fetch TV Genres
            TmdbGenreResponseDto tvGenres = tmdbClient.getTvGenres();
            if (tvGenres != null && tvGenres.getGenres() != null) {
                for (TmdbGenreResponseDto.TmdbGenreDto dto : tvGenres.getGenres()) {
                    if (!existingGenreIds.contains(dto.getId())) {
                        Genre genre = Genre.builder()
                                .tmdbGenreId(dto.getId())
                                .name(dto.getName())
                                .build();
                        genreRepository.save(genre);
                        existingGenreIds.add(dto.getId());
                    }
                }
            }
            log.info("Genres synced successfully.");
        } catch (Exception e) {
            log.error("Failed to sync genres from TMDB: {}", e.getMessage());
        }
    }

    private void syncCountries() {
        try {
            TmdbCountryDto[] countries = tmdbClient.getCountries();
            if (countries != null) {
                for (TmdbCountryDto dto : countries) {
                    if (countryRepository.findByIsoCode(dto.getIso31661()).isEmpty()) {
                        try {
                            String name = dto.getNativeName() != null && !dto.getNativeName().isEmpty() 
                                    ? dto.getNativeName() : dto.getEnglishName();
                            Country country = Country.builder()
                                    .isoCode(dto.getIso31661())
                                    .name(name)
                                    .build();
                            countryRepository.save(country);
                        } catch (Exception e) {
                            log.warn("Could not save country {} ({}): {}", dto.getIso31661(), dto.getEnglishName(), e.getMessage());
                        }
                    }
                }
            }
            log.info("Countries synced successfully.");
        } catch (Exception e) {
            log.error("Failed to sync countries from TMDB: {}", e.getMessage());
        }
    }

    private void syncPopularMovies() {
        if (mediaRepository.count() > 0) return;
        try {
            log.info("Bắt đầu tự động lấy danh sách phim phổ biến từ TMDB...");
            var popularMovies = tmdbClient.getPopularMovies();
            if (popularMovies != null && popularMovies.getResults() != null) {
                for (int i = 0; i < Math.min(10, popularMovies.getResults().size()); i++) {
                    var tmdbId = popularMovies.getResults().get(i).getId();
                    try {
                        tmdbSyncService.syncMovieFromTmdb(tmdbId, "https://www.w3schools.com/html/mov_bbb.mp4", false);
                    } catch (Exception e) {
                        log.warn("Không thể đồng bộ phim {}: {}", tmdbId, e.getMessage());
                    }
                }
            }
            log.info("Hoàn tất lấy phim phổ biến từ TMDB.");
        } catch (Exception e) {
            log.error("Lỗi khi cào phim phổ biến: {}", e.getMessage());
        }
    }
}
