package com.example.movie_app_server.media.service;

import com.example.movie_app_server.media.dto.TmdbMovieResponse;
import com.example.movie_app_server.media.entity.Genre;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.entity.Country;
import com.example.movie_app_server.media.entity.Credit;
import com.example.movie_app_server.media.entity.enums.MediaType;
import com.example.movie_app_server.media.repository.CountryRepository;
import com.example.movie_app_server.media.repository.GenreRepository;
import com.example.movie_app_server.media.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TmdbService {

    private final RestTemplate restTemplate;
    private final MediaRepository mediaRepository;
    private final GenreRepository genreRepository;
    private final CountryRepository countryRepository;

    @Value("${tmdb.base-url}")
    private String tmdbBaseUrl;

    @Value("${tmdb.token}")
    private String tmdbToken;

    public TmdbService(RestTemplate restTemplate, MediaRepository mediaRepository, GenreRepository genreRepository, CountryRepository countryRepository) {
        this.restTemplate = restTemplate;
        this.mediaRepository = mediaRepository;
        this.genreRepository = genreRepository;
        this.countryRepository = countryRepository;
    }

    public Media fetchAndSaveMovie(Integer tmdbId) {
        // Kiểm tra xem phim đã có trong DB chưa
        Optional<Media> existingMedia = mediaRepository.findByTmdbId(tmdbId); // Giả sử có hàm này
        if (existingMedia.isPresent()) {
            return existingMedia.get(); // Nếu có rồi thì trả về luôn, hoặc có thể code thêm logic update
        }

        // Bổ sung &include_video_language=vi,en,null
        String url = tmdbBaseUrl + "/movie/" + tmdbId + "?language=vi-VN&append_to_response=credits,videos&include_video_language=vi,en,null";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tmdbToken);
        headers.set("accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<TmdbMovieResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, TmdbMovieResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            TmdbMovieResponse dto = response.getBody();

            Media media = Media.builder()
                    .tmdbId(dto.getId())
                    .title(dto.getTitle())
                    .overview(dto.getOverview())
                    .posterPath(dto.getPoster_path())
                    .backdropPath(dto.getBackdrop_path())
                    .mediaType(MediaType.MOVIE)
                    .voteAverage(dto.getVote_average())
                    .build();

            if (dto.getRelease_date() != null && !dto.getRelease_date().isEmpty()) {
                media.setReleaseDate(LocalDate.parse(dto.getRelease_date()));
            }

            media.setDuration(dto.getRuntime());

            if (dto.getProduction_countries() != null && !dto.getProduction_countries().isEmpty()) {
                String countryName = dto.getProduction_countries().get(0).getName();
                Country country = countryRepository.findByName(countryName).orElseGet(() -> {
                    Country newCountry = new Country();
                    newCountry.setName(countryName);
                    return countryRepository.save(newCountry);
                });
                media.setCountry(country);
            }

            if (dto.getVideos() != null && dto.getVideos().getResults() != null) {
                for (TmdbMovieResponse.VideoResultDto video : dto.getVideos().getResults()) {
                    if ("YouTube".equals(video.getSite()) && "Trailer".equals(video.getType())) {
                        media.setTrailerUrl("https://www.youtube.com/watch?v=" + video.getKey());
                        break;
                    }
                }
            }

            if (dto.getCredits() != null) {
                List<Credit> credits = new ArrayList<>();
                if (dto.getCredits().getCast() != null) {
                    int limit = Math.min(10, dto.getCredits().getCast().size());
                    for (int i = 0; i < limit; i++) {
                        TmdbMovieResponse.CastDto c = dto.getCredits().getCast().get(i);
                        Credit credit = Credit.builder()
                                .tmdbPersonId(c.getId())
                                .name(c.getName())
                                .characterName(c.getCharacter())
                                .profilePath(c.getProfile_path())
                                .department(c.getKnown_for_department())
                                .media(media)
                                .build();
                        credits.add(credit);
                    }
                }
                if (dto.getCredits().getCrew() != null) {
                    for (TmdbMovieResponse.CrewDto c : dto.getCredits().getCrew()) {
                        if ("Director".equals(c.getJob())) {
                            Credit credit = Credit.builder()
                                    .tmdbPersonId(c.getId())
                                    .name(c.getName())
                                    .department(c.getDepartment())
                                    .profilePath(c.getProfile_path())
                                    .media(media)
                                    .build();
                            credits.add(credit);
                        }
                    }
                }
                media.setCredits(credits);
            }

            // Xử lý thể loại (Genres)
            List<Genre> genres = new ArrayList<>();
            if (dto.getGenres() != null) {
                for (TmdbMovieResponse.GenreDto g : dto.getGenres()) {
                    Genre genre = genreRepository.findByTmdbGenreId(g.getId())
                            .orElseGet(() -> {
                                Genre newGenre = new Genre();
                                newGenre.setTmdbGenreId(g.getId());
                                newGenre.setName(g.getName());
                                return genreRepository.save(newGenre);
                            });
                    genres.add(genre);
                }
            }
            media.setGenres(genres);

            return mediaRepository.save(media);
        }

        throw new RuntimeException("Không thể lấy dữ liệu từ TMDB cho ID: " + tmdbId);
    }
}
