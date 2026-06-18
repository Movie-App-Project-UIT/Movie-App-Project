package com.example.movie_app_server.interaction.service;

import com.example.movie_app_server.interaction.repository.WatchlistRepository;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.interaction.entity.Watchlist;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {
    private final WatchlistRepository watchlistRepo;
    private final UserRepository userRepo;
    private final MediaRepository mediaRepo;
    private final com.example.movie_app_server.media.service.MediaService mediaService;

    // Lấy danh sách phim trong mục "Yêu thích" của cá nhân user
    public List<com.example.movie_app_server.interaction.dto.WatchlistItemDto> getMyWatchlist(String uid) {
        User user = userRepo.findByFirebaseUid(uid).orElseThrow();
        List<Watchlist> watchlists = watchlistRepo.findByUserIdOrderByAddedAtDesc(user.getId());
        return watchlists.stream()
                .map(w -> com.example.movie_app_server.interaction.dto.WatchlistItemDto.builder()
                        .id(w.getId())
                        .addedAt(w.getAddedAt())
                        .media(mediaService.convertToItemDto(w.getMedia()))
                        .build())
                .toList();
    }

    /**
     * Nút công tắc (Toggle): Ấn lần 1 là Thêm, Ấn lần 2 là Xóa.
     * Thường dùng cho nút hình Trái tim trên App Android.
     */
    @Transactional
    public Watchlist toggleWatchlist(String uid, Long mediaId) {
        User user = userRepo.findByFirebaseUid(uid).orElseThrow();

        return watchlistRepo.findByUserIdAndMediaId(user.getId(), mediaId)
                .map(existing -> {
                    // Nếu đã lưu rồi -> User bấm lại nghĩa là muốn BỎ LƯU
                    watchlistRepo.delete(existing);
                    return existing;
                }).orElseGet(() -> {
                    // Nếu chưa lưu -> User bấm vào nghĩa là muốn THÊM MỚI
                    Media media = mediaRepo.findById(mediaId).orElseThrow();
                    return watchlistRepo.save(Watchlist.builder().user(user).media(media).build());
                });
    }
}