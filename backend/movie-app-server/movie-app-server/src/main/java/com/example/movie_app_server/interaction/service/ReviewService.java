package com.example.movie_app_server.interaction.service;

import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.interaction.entity.Review;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.media.repository.EpisodeRepository;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.interaction.repository.ReviewRepository;
import com.example.movie_app_server.interaction.entity.enums.NotificationType;
import com.example.movie_app_server.user.repository.UserRepository;
import com.example.movie_app_server.interaction.service.NotificationService;
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
    private final NotificationService notificationService;
    private final com.example.movie_app_server.interaction.repository.ReviewReportRepository reviewReportRepository;

    // Lấy danh sách bình luận GỐC của phim, xếp mới nhất lên đầu.
    public List<com.example.movie_app_server.interaction.dto.ReviewResponseDto> getReviewsByMedia(Long mediaId, String uid) {
        List<Review> allReviews = reviewRepository.findAllByMediaIdWithUser(mediaId);
        
        java.util.Set<Long> reportedReviewIds = new java.util.HashSet<>();
        if (uid != null && !uid.equals("anonymousUser")) {
            User user = userRepository.findByFirebaseUid(uid).orElse(null);
            if (user != null) {
                reportedReviewIds = reviewReportRepository.findByUserId(user.getId()).stream()
                    .map(report -> report.getReview().getId())
                    .collect(java.util.stream.Collectors.toSet());
            }
        }
        
        java.util.Map<Long, List<Review>> repliesMap = allReviews.stream()
                .filter(r -> r.getParent() != null)
                .collect(java.util.stream.Collectors.groupingBy(r -> r.getParent().getId()));

        final java.util.Set<Long> finalReportedReviewIds = reportedReviewIds;
        List<Review> roots = allReviews.stream()
                .filter(r -> r.getParent() == null)
                .filter(r -> !finalReportedReviewIds.contains(r.getId()))
                .sorted(java.util.Comparator.comparing(Review::getCreatedAt).reversed())
                .toList();

        return roots.stream().map(r -> convertToResponseDtoWithMap(r, repliesMap, finalReportedReviewIds)).toList();
    }

    private com.example.movie_app_server.interaction.dto.ReviewResponseDto convertToResponseDtoWithMap(Review review, java.util.Map<Long, List<Review>> repliesMap, java.util.Set<Long> reportedReviewIds) {
        List<Review> replies = repliesMap.getOrDefault(review.getId(), java.util.Collections.emptyList())
                .stream()
                .filter(r -> !reportedReviewIds.contains(r.getId()))
                .collect(java.util.stream.Collectors.toList());
        replies.sort(java.util.Comparator.comparing(Review::getCreatedAt));

        return com.example.movie_app_server.interaction.dto.ReviewResponseDto.builder()
                .id(review.getId())
                .parentId(review.getParent() != null ? review.getParent().getId() : null)
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .user(com.example.movie_app_server.user.dto.UserSummaryDto.builder()
                        .username(review.getUser().getUsername())
                        .avatarUrl(review.getUser().getAvatarUrl())
                        .build())
                .replies(replies.stream().map(r -> convertToResponseDtoWithMap(r, repliesMap, reportedReviewIds)).toList())
                .build();
    }

    /**
     * Tạo một bình luận mới.
     * Hỗ trợ 3 trường hợp:
     * 1. Bình luận tổng cho phim lẻ (mediaId)
     * 2. Bình luận cho 1 tập phim cụ thể (kèm episodeId)
     * 3. Trả lời/Reply một bình luận khác (kèm parentId)
     */
    @Transactional
    public com.example.movie_app_server.interaction.dto.ReviewResponseDto createReview(String uid, Long mediaId, Long episodeId, Long parentId, String content) {
        User user = userRepository.findByFirebaseUid(uid).orElseThrow();
        Media media = mediaRepository.findById(mediaId).orElseThrow();

        Review.ReviewBuilder builder = Review.builder().user(user).media(media).content(content);

        // Nếu user đang xem phim bộ và bình luận tập đó
        if (episodeId != null) builder.episode(episodeRepository.findById(episodeId).orElseThrow());

        // Nếu user đang bấm nút "Trả lời" bình luận của người khác
        if (parentId != null) {
            Review parent = reviewRepository.findById(parentId).orElseThrow();
            builder.parent(parent);
            Review saved = reviewRepository.save(builder.build());
            
            // Gửi thông báo cho người được trả lời
            if (!parent.getUser().getId().equals(user.getId())) {
                notificationService.createNotificationWithRelatedId(
                        parent.getUser(),
                        "Bình luận của bạn có phản hồi mới",
                        user.getUsername() + " đã trả lời bình luận của bạn.",
                        NotificationType.REPLY,
                        saved.getId()
                );
            }
            return convertToResponseDto(saved);
        }

        Review saved = reviewRepository.save(builder.build());
        return convertToResponseDto(saved);
    }

    @Transactional
    public com.example.movie_app_server.interaction.dto.ReviewResponseDto updateReview(Long id, String uid, String newContent) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));
        if (!review.getUser().getFirebaseUid().equals(uid)) {
            throw new RuntimeException("Bạn không có quyền sửa bình luận này");
        }
        review.setContent(newContent);
        Review saved = reviewRepository.save(review);
        return convertToResponseDto(saved);
    }

    @Transactional
    public void deleteReview(Long id, String uid) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));
        User user = userRepository.findByFirebaseUid(uid).orElseThrow();
        // Cho phép Admin xóa mọi comment hoặc chính chủ xóa comment của mình
        if (!review.getUser().getFirebaseUid().equals(uid) && user.getRole() != com.example.movie_app_server.user.entity.enums.Role.ADMIN) {
            throw new RuntimeException("Bạn không có quyền xóa bình luận này");
        }
        reviewRepository.delete(review);
    }

    private com.example.movie_app_server.interaction.dto.ReviewResponseDto convertToResponseDto(Review review) {
        return com.example.movie_app_server.interaction.dto.ReviewResponseDto.builder()
                .id(review.getId())
                .parentId(review.getParent() != null ? review.getParent().getId() : null)
                .parentUsername(review.getParent() != null ? review.getParent().getUser().getUsername() : null)
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .user(com.example.movie_app_server.user.dto.UserSummaryDto.builder()
                        .username(review.getUser().getUsername())
                        .avatarUrl(review.getUser().getAvatarUrl())
                        .build())
                .replies(review.getReplies() != null ? 
                         review.getReplies().stream().map(r -> convertToResponseDto(r)).toList() : null)
                .build();
    }


    @Transactional
    public void reportReview(Long reviewId, String uid, String reason) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        User user = userRepository.findByFirebaseUid(uid).orElseThrow();
        if (reviewReportRepository.existsByReviewIdAndUserId(reviewId, user.getId())) {
            throw new RuntimeException("Bạn đã báo cáo bình luận này rồi");
        }
        com.example.movie_app_server.interaction.entity.ReviewReport report = com.example.movie_app_server.interaction.entity.ReviewReport.builder()
                .review(review)
                .user(user)
                .reason(reason)
                .build();
        reviewReportRepository.save(report);
    }
}
