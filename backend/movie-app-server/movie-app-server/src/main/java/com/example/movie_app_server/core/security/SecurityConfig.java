package com.example.movie_app_server.core.security;

import com.example.movie_app_server.core.firebase.FirebaseTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final FirebaseTokenFilter firebaseTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF (Không cần thiết cho REST API)
                .csrf(AbstractHttpConfigurer::disable)

                // Tắt session (Vì hệ thống dùng Token nên không cần lưu phiên đăng nhập trên server)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Phân quyền các đường dẫn API
                .authorizeHttpRequests(auth -> auth

                        // 1. CÁC API CÔNG KHAI (Khách vãng lai không cần đăng nhập vẫn xem được)
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/media", "/api/v1/media/**",
                                "/api/v1/seasons", "/api/v1/seasons/**",
                                "/api/v1/episodes", "/api/v1/episodes/**",
                                "/api/v1/lookups", "/api/v1/lookups/**",
                                "/api/v1/reviews/media/**"
                        ).permitAll()
                        .requestMatchers("/error").permitAll()

                        // 2. CÁC API DÀNH RIÊNG CHO ADMIN (Kéo phim, cập nhật phim)
                        // Bắt buộc phải có Role là ADMIN mới được vào
                        .requestMatchers("/api/v1/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")

                        // 3. CÁC API YÊU CẦU ĐĂNG NHẬP (User bình thường, ví dụ: bình luận, xem phim VIP)
                        .anyRequest().authenticated()
                )
                // Chèn màng lọc Firebase của chúng ta vào hệ thống Spring Security
                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}