package com.example.movie_app_server.user.controller;

import com.example.movie_app_server.admin.entity.AdminHistory;
import com.example.movie_app_server.admin.service.AdminHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/history")
@RequiredArgsConstructor
public class AdminHistoryController {

    private final AdminHistoryService adminHistoryService;

    @GetMapping
    public ResponseEntity<List<AdminHistory>> getAdminHistory() {
        return ResponseEntity.ok(adminHistoryService.getAllHistories());
    }
}
