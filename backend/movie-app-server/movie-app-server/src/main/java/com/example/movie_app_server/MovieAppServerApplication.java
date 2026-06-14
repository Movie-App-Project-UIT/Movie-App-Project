package com.example.movie_app_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MovieAppServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovieAppServerApplication.class, args);
	}

}
