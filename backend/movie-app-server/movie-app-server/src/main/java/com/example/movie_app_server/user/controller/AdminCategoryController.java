package com.example.movie_app_server.user.controller;

import com.example.movie_app_server.admin.dto.AdminGenreDto;
import com.example.movie_app_server.media.dto.MediaItemDto;
import com.example.movie_app_server.media.entity.Genre;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.repository.GenreRepository;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.media.service.MediaService;
import com.example.movie_app_server.admin.service.AdminHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class AdminCategoryController {

    private final GenreRepository genreRepository;
    private final MediaRepository mediaRepository;
    private final MediaService mediaService;
    private final AdminHistoryService adminHistoryService;

    private AdminGenreDto convertToDto(Genre genre) {
        List<Media> mediaList = mediaRepository.findByGenres_Id(genre.getId());
        
        // Tạo viewCount giả định
        int fakeViewCount = 0;
        for (Media m : mediaList) {
            fakeViewCount += (m.getId() != null ? (int) (m.getId() * 1234 % 50000) : 0);
        }

        // Tùy chọn color (mock cho giống design cũ)
        String[] colors = {"#EF4444", "#F59E0B", "#EC4899", "#1F2937", "#3B82F6", "#10B981", "#8B5CF6", "#06B6D4"};
        String color = colors[(int) (genre.getId() % colors.length)];

        return AdminGenreDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .colorCode(color)
                .viewCount(fakeViewCount)
                .mediaCount(mediaList.size())
                .isDeleted(genre.isDeleted())
                .build();
    }

    @GetMapping
    public ResponseEntity<List<AdminGenreDto>> getAllCategories() {
        return ResponseEntity.ok(genreRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminGenreDto> getCategoryById(@PathVariable Long id) {
        return genreRepository.findById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AdminGenreDto> createCategory(@RequestBody java.util.Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Genre genre = Genre.builder()
                .name(name)
                .tmdbGenreId((int) (Math.random() * 100000) + 10000) // Random TMDB ID cho Thể loại tự tạo
                .isDeleted(false)
                .build();
        genreRepository.save(genre);
        adminHistoryService.logAction("CREATE", "CATEGORY", genre.getId().toString(), "Thêm thể loại mới: " + genre.getName());
        return ResponseEntity.ok(convertToDto(genre));
    }

    @PutMapping("/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteCategory(@PathVariable Long id) {
        return genreRepository.findById(id).map(genre -> {
            boolean willDelete = !genre.isDeleted();
            genre.setDeleted(willDelete);
            genreRepository.save(genre);

            if (willDelete) {
                // Xóa thể loại -> Ẩn các phim đang hoạt động
                List<Media> activeMedia = mediaRepository.findByIsDeletedFalseAndGenres_Id(id);
                for (Media m : activeMedia) {
                    m.setDeleted(true);
                    m.setHiddenByGenreId(id);
                }
                mediaRepository.saveAll(activeMedia);
            } else {
                // Khôi phục thể loại -> Khôi phục các phim đã bị ẩn VÌ thể loại này
                List<Media> allMediaInGenre = mediaRepository.findByGenres_Id(id);
                for (Media m : allMediaInGenre) {
                    if (m.isDeleted() && m.getHiddenByGenreId() != null && m.getHiddenByGenreId().equals(id)) {
                        m.setDeleted(false);
                        m.setHiddenByGenreId(null);
                    }
                }
                mediaRepository.saveAll(allMediaInGenre);
            }

            adminHistoryService.logAction(
                willDelete ? "DELETE" : "RESTORE",
                "CATEGORY",
                genre.getId().toString(),
                (willDelete ? "Xóa thể loại: " : "Khôi phục thể loại: ") + genre.getName()
            );

            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/media")
    public ResponseEntity<List<MediaItemDto>> getMediaInGenre(@PathVariable Long id) {
        List<Media> mediaList = mediaRepository.findByGenres_Id(id);
        return ResponseEntity.ok(mediaList.stream()
                .map(mediaService::convertToItemDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}/media/exclude")
    public ResponseEntity<List<MediaItemDto>> getMediaNotInGenre(@PathVariable Long id) {
        return genreRepository.findById(id).map(genre -> {
            List<Media> notInGenre = mediaRepository.findMediaNotContainingGenre(genre);
            return ResponseEntity.ok(notInGenre.stream()
                    .map(mediaService::convertToItemDto)
                    .collect(Collectors.toList()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/media/{mediaId}")
    public ResponseEntity<Void> addMediaToGenre(@PathVariable Long id, @PathVariable Long mediaId) {
        return genreRepository.findById(id).map(genre -> {
            mediaRepository.findById(mediaId).ifPresent(media -> {
                if (!media.getGenres().contains(genre)) {
                    media.getGenres().add(genre);
                    mediaRepository.save(media);
                    adminHistoryService.logAction("UPDATE", "CATEGORY", genre.getId().toString(), "Thêm phim '" + media.getTitle() + "' vào thể loại: " + genre.getName());
                }
            });
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/media/{mediaId}")
    public ResponseEntity<Void> removeMediaFromGenre(@PathVariable Long id, @PathVariable Long mediaId) {
        return genreRepository.findById(id).map(genre -> {
            mediaRepository.findById(mediaId).ifPresent(media -> {
                if (media.getGenres().contains(genre)) {
                    media.getGenres().remove(genre);
                    mediaRepository.save(media);
                    adminHistoryService.logAction("UPDATE", "CATEGORY", genre.getId().toString(), "Xóa phim '" + media.getTitle() + "' khỏi thể loại: " + genre.getName());
                }
            });
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
