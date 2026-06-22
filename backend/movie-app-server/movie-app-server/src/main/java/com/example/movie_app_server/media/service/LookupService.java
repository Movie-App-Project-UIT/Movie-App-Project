package com.example.movie_app_server.media.service;

import com.example.movie_app_server.media.entity.AgeRating;
import com.example.movie_app_server.media.entity.Country;
import com.example.movie_app_server.media.entity.Genre;
import com.example.movie_app_server.media.repository.AgeRatingRepository;
import com.example.movie_app_server.media.repository.CountryRepository;
import com.example.movie_app_server.media.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service xử lý logic liên quan đến các bảng danh mục (dữ liệu tĩnh).
 * Cung cấp dữ liệu để hiển thị lên các bộ lọc (Filter) hoặc Menu thả xuống trên App.
 */
@Service
@RequiredArgsConstructor
public class LookupService {
    private final CountryRepository countryRepository;
    private final AgeRatingRepository ageRatingRepository;
    private final GenreRepository genreRepository;

    // Lấy toàn bộ danh sách Quốc gia
    @Cacheable("countries")
    public List<Country> getAllCountries() { return countryRepository.findAll(); }

    // Lấy toàn bộ danh sách Phân loại độ tuổi (Ví dụ: T13, T18...)
    @Cacheable("ageRatings")
    public List<AgeRating> getAllAgeRatings() { return ageRatingRepository.findAll(); }

    // Lấy toàn bộ danh sách Thể loại phim (Hành động, Hài, Tình cảm...)
    @Cacheable("genres")
    public List<Genre> getAllGenres() { return genreRepository.findAll(); }
}