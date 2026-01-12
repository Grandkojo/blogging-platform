package com.blogging_platform.exceptions;

/**
 * Exception thrown when configuration is missing or invalid.
 */
public class ConfigurationException extends BloggingPlatformException {
    
    private final String configKey;
    
    public ConfigurationException(String message) {
        super("CONFIG_ERROR", message, "Configuration error. Please contact system administrator.");
        this.configKey = null;
    }
    
    public ConfigurationException(String configKey, String message) {
        super("CONFIG_ERROR", 
              String.format("Configuration error for key '%s': %s", configKey, message),
              "Configuration error. Please contact system administrator.");
        this.configKey = configKey;
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super("CONFIG_ERROR", message, "Configuration error. Please contact system administrator.", cause);
        this.configKey = null;
    }
    
    /**
     * Returns the configuration key that caused the exception, if available.
     * 
     * @return the configuration key, or null if not available
     */
    public String getConfigKey() {
        return configKey;
    }
}

