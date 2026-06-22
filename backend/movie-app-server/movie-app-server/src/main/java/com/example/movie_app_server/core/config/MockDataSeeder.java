package com.example.movie_app_server.core.config;

import com.example.movie_app_server.interaction.entity.subscription.SubscriptionPlan;
import com.example.movie_app_server.interaction.repository.SubscriptionPlanRepository;
import com.example.movie_app_server.media.service.TmdbSyncService;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.entity.enums.Role;
import com.example.movie_app_server.user.entity.enums.Tier;
import com.example.movie_app_server.user.repository.UserRepository;
import com.example.movie_app_server.media.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Script tự động nạp dữ liệu mẫu (Mock Data).
 * Khi thành viên khác pull code về, server chạy lên sẽ tự động có data để test.
 * ĐỂ TẮT CHỨC NĂNG NÀY: Hãy comment dòng @Component ngay bên dưới lại (hoặc return ngay đầu hàm run).
 */
@Component
@Order(2) // Đảm bảo chạy sau TmdbDataSeeder (Order 1) để có sẵn Genre và Country
@RequiredArgsConstructor
@Slf4j
public class MockDataSeeder implements CommandLineRunner {

    private final TmdbSyncService tmdbSyncService;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final MediaRepository mediaRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("--- BẮT ĐẦU CHẠY SCRIPT NẠP DỮ LIỆU MẪU (MockDataSeeder) ---");

        createSubscriptionPlans();
        syncMockMovies();

        log.info("--- HOÀN TẤT NẠP DỮ LIỆU MẪU ---");
    }

    private void createSubscriptionPlans() {
        if (planRepository.count() == 0) {
            log.info("Đang tạo các gói dịch vụ mẫu...");
            
            SubscriptionPlan freePlan = SubscriptionPlan.builder()
                    .name("Gói Miễn Phí (Free)")
                    .description("Xem phim chất lượng tiêu chuẩn, có quảng cáo.")
                    .price(BigDecimal.ZERO)
                    .durationDays(3650) // 10 năm
                    .isActive(true)
                    .build();

            SubscriptionPlan premiumPlan = SubscriptionPlan.builder()
                    .name("Gói Premium (VIP)")
                    .description("Xem phim chất lượng 4K, không quảng cáo, mở khóa toàn bộ phim VIP.")
                    .price(new BigDecimal("99000"))
                    .durationDays(30) // 1 tháng
                    .isActive(true)
                    .build();

            planRepository.save(freePlan);
            planRepository.save(premiumPlan);
        }
    }



    private void syncMockMovies() {
        if (mediaRepository.count() > 0) return;

        try {
            log.info("Đang tạo danh sách phim mẫu cứng (Hardcoded) vì máy bạn bị chặn kết nối đến TMDB...");

            com.example.movie_app_server.media.entity.Media m1 = com.example.movie_app_server.media.entity.Media.builder()
                .tmdbId(372058)
                .title("Your Name")
                .overview("Mitsuha, a high school girl living in the fictional town of Itomori, and Taki, a high school boy in Tokyo, begin to swap bodies...")
                .posterPath("https://upload.wikimedia.org/wikipedia/en/0/0b/Your_Name_poster.png")
                .backdropPath("https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/Los_Angeles_with_Mount_San_Antonio.jpg/1280px-Los_Angeles_with_Mount_San_Antonio.jpg")
                .mediaType(com.example.movie_app_server.media.entity.enums.MediaType.MOVIE)
                .voteAverage(8.5f)
                .releaseDate(java.time.LocalDate.of(2016, 8, 26))
                .isPremium(false)
                .videoUrl("https://www.w3schools.com/html/mov_bbb.mp4")
                .build();

            com.example.movie_app_server.media.entity.Media m2 = com.example.movie_app_server.media.entity.Media.builder()
                .tmdbId(157336)
                .title("Interstellar")
                .overview("The adventures of a group of explorers who make use of a newly discovered wormhole...")
                .posterPath("https://upload.wikimedia.org/wikipedia/en/b/bc/Interstellar_film_poster.jpg")
                .backdropPath("https://upload.wikimedia.org/wikipedia/commons/thumb/1/1c/KSC-99pp0117.jpg/1280px-KSC-99pp0117.jpg")
                .mediaType(com.example.movie_app_server.media.entity.enums.MediaType.MOVIE)
                .voteAverage(8.6f)
                .releaseDate(java.time.LocalDate.of(2014, 11, 5))
                .isPremium(true)
                .videoUrl("https://www.w3schools.com/html/mov_bbb.mp4")
                .build();
                
            com.example.movie_app_server.media.entity.Media m3 = com.example.movie_app_server.media.entity.Media.builder()
                .tmdbId(533535)
                .title("Deadpool & Wolverine")
                .overview("A listless Wade Wilson toils away in civilian life...")
                .posterPath("https://upload.wikimedia.org/wikipedia/en/4/4c/Deadpool_%26_Wolverine_poster.jpg")
                .backdropPath("https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/Logan_Movie_Logo.png/1280px-Logan_Movie_Logo.png")
                .mediaType(com.example.movie_app_server.media.entity.enums.MediaType.MOVIE)
                .voteAverage(7.8f)
                .releaseDate(java.time.LocalDate.of(2024, 7, 24))
                .isPremium(false)
                .videoUrl("https://www.w3schools.com/html/mov_bbb.mp4")
                .build();

            com.example.movie_app_server.media.entity.Media m4 = com.example.movie_app_server.media.entity.Media.builder()
                .tmdbId(129)
                .title("Spirited Away")
                .overview("A young girl, Chihiro, becomes trapped in a strange new world of spirits...")
                .posterPath("https://upload.wikimedia.org/wikipedia/en/d/db/Spirited_Away_Japanese_poster.png")
                .backdropPath("https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Tokyo_Shibuya_Scramble_Crossing_2018-10-09.jpg/1280px-Tokyo_Shibuya_Scramble_Crossing_2018-10-09.jpg")
                .mediaType(com.example.movie_app_server.media.entity.enums.MediaType.MOVIE)
                .voteAverage(8.5f)
                .releaseDate(java.time.LocalDate.of(2001, 7, 20))
                .isPremium(false)
                .videoUrl("https://www.w3schools.com/html/mov_bbb.mp4")
                .build();

            mediaRepository.save(m1);
            mediaRepository.save(m2);
            mediaRepository.save(m3);
            mediaRepository.save(m4);
            log.info("Tạo phim mẫu tĩnh thành công.");
        } catch (Exception e) {
            log.error("Lỗi khi tạo phim mẫu: {}", e.getMessage());
        }
    }
}
