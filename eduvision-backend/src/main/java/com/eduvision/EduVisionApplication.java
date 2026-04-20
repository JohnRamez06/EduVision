package com.eduvision;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
@SpringBootApplication
@EnableAsync
public class EduVisionApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduVisionApplication.class, args);
    }
}
