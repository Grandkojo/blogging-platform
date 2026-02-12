package com.blogging_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

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
        SpringApplication.run(Main.class, args);
    }
}
