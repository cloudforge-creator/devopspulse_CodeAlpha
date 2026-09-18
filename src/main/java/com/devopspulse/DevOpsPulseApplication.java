package com.devopspulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the DevOpsPulse application.
 *
 * DevOpsPulse is a single, unified Spring Boot application demonstrating
 * the complete DevOps lifecycle: Development -> Git -> Gradle Build ->
 * Unit Testing -> Docker -> Jenkins Controller/Agent -> Azure CI/CD ->
 * Azure Container Registry -> Azure App Service -> Health Monitoring.
 */
@SpringBootApplication
public class DevOpsPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevOpsPulseApplication.class, args);
    }
}
