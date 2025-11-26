package com.example.studybot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudybotApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudybotApplication.class, args);
    }
}


//package com.example.studybot;
//
//import io.github.cdimascio.dotenv.Dotenv;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class StudybotApplication {
//
//    public static void main(String[] args) {
//        // Load bot.env file
//        Dotenv dotenv = Dotenv.configure()
//                .filename("bot.env")
//                .load();
//
//        // Set all env variables as system properties for Spring Boot
//        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
//
//        SpringApplication.run(StudybotApplication.class, args);
//    }
//}


