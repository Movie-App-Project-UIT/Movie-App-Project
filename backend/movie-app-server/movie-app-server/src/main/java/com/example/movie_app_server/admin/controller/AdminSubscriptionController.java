package com.example.movie_app_server.admin.controller;

import com.example.movie_app_server.interaction.entity.subscription.SubscriptionPlan;
import com.example.movie_app_server.interaction.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @GetMapping
    public ResponseEntity<List<SubscriptionPlan>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionPlanRepository.findAll());
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<Void> toggleSubscriptionStatus(@PathVariable Long id) {
        return subscriptionPlanRepository.findById(id).map(plan -> {
            plan.setIsActive(!plan.getIsActive()); // Toggle
            subscriptionPlanRepository.save(plan);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
