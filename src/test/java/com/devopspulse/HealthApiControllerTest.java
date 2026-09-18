package com.devopspulse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full MockMvc test coverage for the health and status REST endpoints.
 * These tests are exercised by the Gradle "test" task, the Jenkins
 * "Gradle Build & Test" stage, and the Azure Pipelines "Build & Unit Test"
 * stage before any artifact is packaged or deployed.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HealthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointReturnsOkWithExpectedJsonStructure() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("DevOpsPulse"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    void statusEndpointReturnsOkWithFullStatusDictionary() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.application.name").value("DevOpsPulse"))
                .andExpect(jsonPath("$.application.version").value("1.0.0"))
                .andExpect(jsonPath("$.build.status").value("PASSING"))
                .andExpect(jsonPath("$.build.tool").value("Gradle"))
                .andExpect(jsonPath("$.deployment.registry").value("Azure Container Registry"))
                .andExpect(jsonPath("$.health.healthy").value(true))
                .andExpect(jsonPath("$.runtime.availableProcessors").exists());
    }

    @Test
    void statusEndpointReflectsAppEnvironmentDefault() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application.environment").exists());
    }
}
