package com.example.movie_app_server.interaction.service;

import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.interaction.entity.Review;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.media.repository.EpisodeRepository;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.interaction.repository.ReviewRepository;
import com.example.movie_app_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final EpisodeRepository episodeRepository;

    // Lấy danh sách bình luận GỐC của phim, xếp mới nhất lên đầu.
    public List<Review> getReviewsByMedia(Long mediaId) {
        return reviewRepository.findRootReviewsByMediaIdWithUser(mediaId);
    }

    /**
     * Tạo một bình luận mới.
     * Hỗ trợ 3 trường hợp:
     * 1. Bình luận tổng cho phim lẻ (mediaId)
     * 2. Bình luận cho 1 tập phim cụ thể (kèm episodeId)
     * 3. Trả lời/Reply một bình luận khác (kèm parentId)
     */
    @Transactional
    public Review createReview(String uid, Long mediaId, Long episodeId, Long parentId, String content) {
        User user = userRepository.findByFirebaseUid(uid).orElseThrow();
        Media media = mediaRepository.findById(mediaId).orElseThrow();

        Review.ReviewBuilder builder = Review.builder().user(user).media(media).content(content);

        // Nếu user đang xem phim bộ và bình luận tập đó
        if (episodeId != null) builder.episode(episodeRepository.findById(episodeId).orElseThrow());

        // Nếu user đang bấm nút "Trả lời" bình luận của người khác
        if (parentId != null) builder.parent(reviewRepository.findById(parentId).orElseThrow());

        return reviewRepository.save(builder.build());
    }
}
