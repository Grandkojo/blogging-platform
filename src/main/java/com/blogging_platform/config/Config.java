package com.blogging_platform.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.blogging_platform.exceptions.ConfigurationException;

public class Config {
    private static final Map<String, String> envVars = loadEnvFile();

    private static Map<String, String> loadEnvFile() {
        Map<String, String> vars = new HashMap<>();
        
        // Try to load from .env file in project root
        File envFile = new File(".env");
        if (envFile.exists()) {
            try (Scanner scanner = new Scanner(envFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    // Skip empty lines and comments
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    // Parse KEY=VALUE format
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
                    }
                }
            } catch (FileNotFoundException e) {
                // .env file not found, will fall back to system environment variables
            }
        }
        
        return vars;
    }

    public static String get(String key) throws ConfigurationException {
        // First check .env file, then fall back to system environment variables
        String value = envVars.get(key);
        if (value != null) {
            return value;
        }
        // Fall back to system environment variables
        String envValue = System.getenv(key);
        if (envValue == null) {
            throw new ConfigurationException(key, "Configuration key '" + key + "' not found in .env file or environment variables");
        }
        return envValue;
    }
}
