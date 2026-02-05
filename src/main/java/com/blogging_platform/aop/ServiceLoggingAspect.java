package com.blogging_platform.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;

/**
 * Cross‑cutting logging and performance monitoring for service layer methods.
 *
 * Uses:
 * - {@link Before}  to log method entry and arguments
 * - {@link AfterReturning} to log successful completion
 * - {@link Around} to measure execution time and log slow calls
 */
@Aspect
@Component
public class ServiceLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoggingAspect.class);

    /**
     * Pointcut for all public methods on service classes.
     * This covers the main CRUD and analytics operations in the application.
     */
    @Pointcut("execution(public * com.blogging_platform.service..*(..))")
    public void serviceMethods() {}

    /**
     * Logs method name and arguments before execution.
     */
    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Entering {} with args {}", joinPoint.getSignature(), Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * Logs method name and return value after successful execution.
     */
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (result instanceof Collection<?> collection) {
            log.info("Completed {} with result collection of size {}",
                    joinPoint.getSignature(), collection.size());
        } else {
            log.info("Completed {} with result {}",
                    joinPoint.getSignature(), result);
        }
    }

    /**
     * Measures execution time around critical service methods.
     * Logs a warning if the method exceeds the configured threshold.
     */
    @Around("serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            if (durationMs > 500) {
                log.warn("Slow service call: {} took {} ms", pjp.getSignature(), durationMs);
            } else {
                log.info("Service call: {} took {} ms", pjp.getSignature(), durationMs);
            }
        }
    }
}

