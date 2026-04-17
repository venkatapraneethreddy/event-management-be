package com.college.eventclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventclubApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventclubApplication.class, args);
    }
}
