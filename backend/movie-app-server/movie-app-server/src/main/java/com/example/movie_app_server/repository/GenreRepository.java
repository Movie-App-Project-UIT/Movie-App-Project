package com.example.movie_app_server.repository;

import com.example.movie_app_server.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByTmdbGenreId(Integer tmdbGenreId);
}