package com.blogging_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the Blogging Platform.
 * Delegates to {@link App#main(String[])} to launch the JavaFX application.
 */
// @SpringBootApplication
public class Main {

    /**
     * Launches the blogging platform JavaFX application.
     *
     * @param args command-line arguments (passed to JavaFX)
     */
    public static void main(String[] args) {
        // SpringApplication.run(App.class, args);
        App.main(args);
    }
}
