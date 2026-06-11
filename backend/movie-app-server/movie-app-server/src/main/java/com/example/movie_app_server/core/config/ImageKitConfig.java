package com.example.movie_app_server.core.config;

import io.imagekit.client.ImageKitClient;
import io.imagekit.client.okhttp.ImageKitOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageKitConfig {

    @Value("${imagekit.private-key}")
    private String privateKey;


    // Khởi tạo ImageKitClient làm một Bean
    @Bean
    public ImageKitClient imageKitClient() {
        return ImageKitOkHttpClient.builder()
                .privateKey(privateKey)
                .build();
    }
}