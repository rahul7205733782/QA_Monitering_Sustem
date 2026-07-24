package com.rahul.dailytask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // CRITICAL: Enables the scheduler
@EnableAsync       // Optional: Enables async processing
public class DailyTaskApplication {
    public static void main(String[] args) {
        SpringApplication.run(DailyTaskApplication.class, args);
        System.out.println("====================================");
        System.out.println(" Daily Task Manager Started ");
        System.out.println(" URL :http://localhost:8080 ");
        System.out.println(" Dashboard Monitor: http://localhost:8080/dashboard-monitor.html");
        System.out.println(" API: http://localhost:8080/api/dashboard-monitor");
        System.out.println(" Scheduler: Running every 5 minutes");
        System.out.println("====================================");
    }
}