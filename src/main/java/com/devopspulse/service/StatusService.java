package com.devopspulse.service;

import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service layer responsible for calculating and exposing runtime metrics,
 * application metadata, and build/deployment status information that is
 * surfaced via the REST API and the Thymeleaf dashboard.
 */
@Service
public class StatusService {

    private static final String APP_NAME = "DevOpsPulse";
    private static final String APP_VERSION = "1.0.0";
    private static final String TAGLINE = "BUILD. DEPLOY. MONITOR.";
    private static final Instant START_TIME = Instant.now();

    /**
     * Returns the environment the application is currently running in.
     * Reads the APP_ENV environment variable and falls back to "Production"
     * when it has not been explicitly set (e.g. local development, or a
     * container started without the variable configured).
     */
    public String getEnvironment() {
        String env = System.getenv("APP_ENV");
        return (env == null || env.isBlank()) ? "Production" : env;
    }

    public String getApplicationName() {
        return APP_NAME;
    }

    public String getVersion() {
        return APP_VERSION;
    }

    public String getTagline() {
        return TAGLINE;
    }

    public boolean isHealthy() {
        // In a real-world scenario this would check downstream dependencies
        // (database connectivity, message brokers, external APIs, etc).
        // For DevOpsPulse the application is considered healthy as long as
        // the JVM is up and serving requests.
        return true;
    }

    public String getBuildStatus() {
        return isHealthy() ? "PASSING" : "FAILING";
    }

    public String getUptimeFormatted() {
        Duration uptime = Duration.between(START_TIME, Instant.now());
        long hours = uptime.toHours();
        long minutes = uptime.toMinutesPart();
        long seconds = uptime.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String getStartTimeFormatted() {
        return DateTimeFormatter.ISO_INSTANT.format(START_TIME);
    }

    public long getUptimeMillis() {
        long jvmUptime = ManagementFactory.getRuntimeMXBean().getUptime();
        return jvmUptime;
    }

    public String getJavaVersion() {
        return System.getProperty("java.version");
    }

    public int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    public long getFreeMemoryMb() {
        return Runtime.getRuntime().freeMemory() / (1024 * 1024);
    }

    public long getTotalMemoryMb() {
        return Runtime.getRuntime().totalMemory() / (1024 * 1024);
    }

    public long getMaxMemoryMb() {
        return Runtime.getRuntime().maxMemory() / (1024 * 1024);
    }

    /**
     * Builds the full status dictionary returned by GET /api/status,
     * combining application metadata, build status, deployment context,
     * and live JVM runtime metrics into a single ordered map.
     */
    public Map<String, Object> getFullStatus() {
        Map<String, Object> status = new LinkedHashMap<>();

        Map<String, Object> application = new LinkedHashMap<>();
        application.put("name", getApplicationName());
        application.put("version", getVersion());
        application.put("tagline", getTagline());
        application.put("environment", getEnvironment());
        status.put("application", application);

        Map<String, Object> build = new LinkedHashMap<>();
        build.put("status", getBuildStatus());
        build.put("tool", "Gradle");
        build.put("javaVersion", getJavaVersion());
        status.put("build", build);

        Map<String, Object> deployment = new LinkedHashMap<>();
        deployment.put("containerized", true);
        deployment.put("orchestration", "Azure App Service (Web App for Containers)");
        deployment.put("registry", "Azure Container Registry");
        deployment.put("ciPipeline", "Jenkins (Controller-Agent) + Azure Pipelines");
        status.put("deployment", deployment);

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("healthy", isHealthy());
        health.put("uptime", getUptimeFormatted());
        health.put("startedAt", getStartTimeFormatted());
        status.put("health", health);

        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("availableProcessors", getAvailableProcessors());
        runtime.put("freeMemoryMb", getFreeMemoryMb());
        runtime.put("totalMemoryMb", getTotalMemoryMb());
        runtime.put("maxMemoryMb", getMaxMemoryMb());
        status.put("runtime", runtime);

        return status;
    }
}
