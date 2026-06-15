package com.campusride.campusride;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.campusride.campusride.repository")
public class CampusrideApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusrideApplication.class, args);
    }
}
