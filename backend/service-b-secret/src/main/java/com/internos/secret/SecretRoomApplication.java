package com.internos.secret;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SecretRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretRoomApplication.class, args);
    }
}

