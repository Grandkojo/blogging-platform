package com.blogging_platform.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Loads .env file and makes variables available to Spring Boot's property resolution.
 * This runs BEFORE Spring Boot reads application.properties, so ${VAR_NAME} placeholders work.
 */
@Order(1) // Run early
public class EnvFileLoader implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        
        // Load .env file
        Map<String, Object> envVars = loadEnvFile();
        
        if (!envVars.isEmpty()) {
            // Add as a property source with highest precedence (after system properties)
            environment.getPropertySources().addFirst(
                new MapPropertySource("envFile", envVars)
            );
        }
    }

    private Map<String, Object> loadEnvFile() {
        Map<String, Object> vars = new HashMap<>();
        
        File envFile = findEnvFile();
        if (envFile != null && envFile.exists()) {
            try (Scanner scanner = new Scanner(envFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > 0) {
                        String key = line.substring(0, equalsIndex).trim();
                        String value = line.substring(equalsIndex + 1).trim();
                        // Remove quotes if present
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("'") && value.endsWith("'")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        vars.put(key, value);
                        // Also set as System property for compatibility
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore - fall back to system env vars
            }
        }
        
        return vars;
    }

    private File findEnvFile() {
        Path current = Paths.get("").toAbsolutePath();
        Path search = current;
        for (int i = 0; i < 5; i++) {
            File pomFile = search.resolve("pom.xml").toFile();
            if (pomFile.exists()) {
                return search.resolve(".env").toFile();
            }
            search = search.getParent();
            if (search == null) {
                break;
            }
        }
        return new File(".env");
    }
}
