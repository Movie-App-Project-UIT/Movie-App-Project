package com.example.movie_app_server.core.client;

import com.example.movie_app_server.media.dto.tmdb.TmdbCreditsResponseDto;
import com.example.movie_app_server.media.dto.tmdb.TmdbMovieDetailsDto;
import com.example.movie_app_server.media.dto.tmdb.TmdbSeasonDetailsDto;
import com.example.movie_app_server.media.dto.tmdb.TmdbTvDetailsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class TmdbClient {

    @Value("${tmdb.token}")
    private String tmdbToken;

    @Value("${tmdb.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Hàm tiện ích để đóng gói Header chứa Token
    private HttpEntity<String> createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tmdbToken); // Nhét token vào Header
        headers.set("accept", "application/json");
        return new HttpEntity<>(headers);
    }

    // 1. Gọi API lấy chi tiết phim (Đã bỏ đoạn ?api_key=...)
    public TmdbMovieDetailsDto getMovieDetails(Integer tmdbId) {
        String url = baseUrl + "/movie/" + tmdbId + "?language=vi-VN&append_to_response=videos&include_video_language=vi,en";

        ResponseEntity<TmdbMovieDetailsDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHeaders(),
                TmdbMovieDetailsDto.class
        );
        return response.getBody();
    }

    // 2. Gọi API lấy danh sách Đạo diễn / Diễn viên
    public TmdbCreditsResponseDto getMovieCredits(Integer tmdbId) {
        String url = baseUrl + "/movie/" + tmdbId + "/credits?language=vi-VN";

        ResponseEntity<TmdbCreditsResponseDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHeaders(),
                TmdbCreditsResponseDto.class
        );
        return response.getBody();
    }

    // --- CÁC HÀM CÀO PHIM BỘ (TV SERIES) ---

    public TmdbTvDetailsDto getTvDetails(Integer tmdbId) {
        String url = baseUrl + "/tv/" + tmdbId + "?language=vi-VN&append_to_response=videos&include_video_language=vi,en";
        return restTemplate.exchange(url, HttpMethod.GET, createHeaders(), TmdbTvDetailsDto.class).getBody();
    }

    public TmdbCreditsResponseDto getTvCredits(Integer tmdbId) {
        String url = baseUrl + "/tv/" + tmdbId + "/credits?language=vi-VN";
        return restTemplate.exchange(url, HttpMethod.GET, createHeaders(), TmdbCreditsResponseDto.class).getBody();
    }

    // Hàm để lấy danh sách Episode của từng Season
    public TmdbSeasonDetailsDto getSeasonDetails(Integer tmdbId, Integer seasonNumber) {
        String url = baseUrl + "/tv/" + tmdbId + "/season/" + seasonNumber + "?language=vi-VN";
        return restTemplate.exchange(url, HttpMethod.GET, createHeaders(), TmdbSeasonDetailsDto.class).getBody();
    }

    // --- CÁC HÀM CÀO CONFIGURATION (GENRE, COUNTRY) ---

    public com.example.movie_app_server.media.dto.tmdb.TmdbGenreResponseDto getMovieGenres() {
        String url = baseUrl + "/genre/movie/list?language=vi-VN";
        return restTemplate.exchange(url, HttpMethod.GET, createHeaders(), com.example.movie_app_server.media.dto.tmdb.TmdbGenreResponseDto.class).getBody();
    }

    public com.example.movie_app_server.media.dto.tmdb.TmdbGenreResponseDto getTvGenres() {
        String url = baseUrl + "/genre/tv/list?language=vi-VN";
        return restTemplate.exchange(url, HttpMethod.GET, createHeaders(), com.example.movie_app_server.media.dto.tmdb.TmdbGenreResponseDto.class).getBody();
    }

    public com.example.movie_app_server.media.dto.tmdb.TmdbCountryDto[] getCountries() {
        String url = baseUrl + "/configuration/countries?language=vi-VN";
        return restTemplate.exchange(url, HttpMethod.GET, createHeaders(), com.example.movie_app_server.media.dto.tmdb.TmdbCountryDto[].class).getBody();
    }
}