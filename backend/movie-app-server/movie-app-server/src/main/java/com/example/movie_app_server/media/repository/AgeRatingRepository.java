package com.example.movie_app_server.media.repository;

import com.example.movie_app_server.media.entity.AgeRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgeRatingRepository extends JpaRepository<AgeRating, Long> {
}
