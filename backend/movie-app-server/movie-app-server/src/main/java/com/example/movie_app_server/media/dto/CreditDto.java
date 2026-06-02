package com.example.movie_app_server.media.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreditDto {
    private String name;
    private String characterName;
    private String profileUrl; // Chứa link ảnh đầy đủ để Android chỉ việc load
}
