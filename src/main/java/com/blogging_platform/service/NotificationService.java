package com.blogging_platform.service;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for handling notifications asynchronously.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final Executor taskExecutor;

    public NotificationService(@Qualifier("taskExecutor") Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public Executor taskExecutor() {
        return this.taskExecutor;
    }

    /**
     * Simulates sending a notification (e.g., email or push) asynchronously.
     *
     * @param recipient the recipient of the notification
     * @param message   the message content
     */
    @Async("taskExecutor")
    public void sendNotification(String recipient, String message) {
        log.info("Starting to send notification to {} in thread {}...", recipient, Thread.currentThread().getName());
        try {
            // Simulate a delay (e.g., network latency or email provider processing)
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Notification interrupted for {}", recipient);
        }
        log.info("Notification sent successfully to {}.", recipient);
    }
}
