package com.example.movie_app_server.user.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DbFixer implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        // Fix for existing users when is_active column was added
        jdbcTemplate.execute("UPDATE users SET is_active = 1 WHERE is_active = 0");
    }
}
