package com.blogging_platform.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for configuration exceptions.
 */
@DisplayName("Configuration Exception Tests")
class ConfigurationExceptionTest {

    @Test
    @DisplayName("ConfigurationException should have correct error code")
    void testConfigurationException() {
        String message = "Configuration error";
        ConfigurationException exception = new ConfigurationException(message);
        
        assertEquals("CONFIG_ERROR", exception.getErrorCode());
        assertTrue(exception.getUserMessage().contains("system administrator"));
    }

    @Test
    @DisplayName("ConfigurationException should store configuration key")
    void testConfigurationExceptionWithKey() {
        String configKey = "DB_NAME";
        String message = "Database name not set";
        ConfigurationException exception = new ConfigurationException(configKey, message);
        
        assertEquals(configKey, exception.getConfigKey());
        assertTrue(exception.getMessage().contains(configKey));
    }

    @Test
    @DisplayName("ConfigurationException should preserve cause")
    void testConfigurationExceptionWithCause() {
        String message = "Configuration error";
        Throwable cause = new RuntimeException("File not found");
        ConfigurationException exception = new ConfigurationException(message, cause);
        
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("ConfigurationException should handle null config key")
    void testConfigurationExceptionWithNullKey() {
        String message = "Configuration error";
        ConfigurationException exception = new ConfigurationException(message);
        
        assertNull(exception.getConfigKey());
    }
}

