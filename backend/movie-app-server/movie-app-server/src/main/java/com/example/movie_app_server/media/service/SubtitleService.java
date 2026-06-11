package com.example.movie_app_server.media.service;

import com.example.movie_app_server.common.exception.AppException;
import com.example.movie_app_server.media.dto.SubtitleDto;
import com.example.movie_app_server.media.dto.SubtitleRequest;
import com.example.movie_app_server.media.entity.Episode;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.entity.Subtitle;
import com.example.movie_app_server.media.repository.EpisodeRepository;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.media.repository.SubtitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubtitleService {

    private final SubtitleRepository subtitleRepository;
    private final MediaRepository mediaRepository;
    private final EpisodeRepository episodeRepository;
    private final CloudinaryService cloudinaryService;

    public SubtitleDto addSubtitle(SubtitleRequest request) {
        Subtitle subtitle = Subtitle.builder()
                .language(request.getLanguage())
                .fileUrl(request.getFileUrl())
                .build();

        if (request.getMediaId() != null) {
            if (subtitleRepository.existsByMediaIdAndLanguage(request.getMediaId(), request.getLanguage())) {
                throw new AppException("Đã tồn tại phụ đề ngôn ngữ này cho phim", HttpStatus.BAD_REQUEST);
            }
            Media media = mediaRepository.findById(request.getMediaId())
                    .orElseThrow(() -> new AppException("Không tìm thấy phim", HttpStatus.NOT_FOUND));
            subtitle.setMedia(media);
        } else if (request.getEpisodeId() != null) {
            if (subtitleRepository.existsByEpisodeIdAndLanguage(request.getEpisodeId(), request.getLanguage())) {
                throw new AppException("Đã tồn tại phụ đề ngôn ngữ này cho tập phim", HttpStatus.BAD_REQUEST);
            }
            Episode episode = episodeRepository.findById(request.getEpisodeId())
                    .orElseThrow(() -> new AppException("Không tìm thấy tập phim", HttpStatus.NOT_FOUND));
            subtitle.setEpisode(episode);
        } else {
            throw new AppException("Phải truyền mediaId hoặc episodeId", HttpStatus.BAD_REQUEST);
        }

        Subtitle savedSubtitle = subtitleRepository.save(subtitle);

        return SubtitleDto.builder()
                .id(savedSubtitle.getId())
                .language(savedSubtitle.getLanguage())
                .fileUrl(savedSubtitle.getFileUrl())
                .build();
    }

    public void deleteSubtitle(Long id) {
        Subtitle subtitle = subtitleRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phụ đề", HttpStatus.NOT_FOUND));
        
        cloudinaryService.deleteRawFile(subtitle.getFileUrl());
        subtitleRepository.delete(subtitle);
    }
}
