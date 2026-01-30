package com.blogging_platform.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.blogging_platform.exceptions.ConfigurationException;

/**
 * Loads configuration from a .env file and system environment variables.
 * Values are read once at startup; keys not in .env fall back to {@link System#getenv(String)}.
 */
public class Config {
    private static final Map<String, String> envVars = loadEnvFile();

    /**
     * Loads KEY=VALUE pairs from .env in the project root (skips comments and empty lines).
     *
     * @return map of keys to values (may be empty if .env is missing)
     */
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

    /**
     * Returns the value for the given configuration key.
     * Checks .env first, then system environment variables.
     *
     * @param key the configuration key (e.g. DB_NAME, USERNAME)
     * @return the value for the key
     * @throws ConfigurationException if the key is not found
     */
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
