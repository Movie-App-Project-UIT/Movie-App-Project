package com.example.movie_app_server.admin.dto;

import lombok.Data;
import java.util.List;

@Data
public class GiftSubscriptionRequest {
    private List<Long> userIds;
}
