package com.blogging_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import com.blogging_platform.config.EnvFileLoader;

/**
 * Application entry point for the Blogging Platform.
 */
@SpringBootApplication
@EnableCaching
public class Main {

    /**
     * Launches the blogging platform application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Main.class);
        // Register listener to load .env file before Spring Boot reads properties
        app.addListeners(new EnvFileLoader());
        app.run(args);
    }
}
