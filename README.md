# DevOpsPulse

**BUILD. DEPLOY. MONITOR.**

A single, unified Java Spring Boot application that demonstrates the complete DevOps lifecycle end to end — from local development through Git, Gradle, Docker, Jenkins, and Azure — with continuous health monitoring at every stage.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Objectives](#2-objectives)
3. [Features](#3-features)
4. [Tech Stack](#4-tech-stack)
5. [Architecture](#5-architecture)
6. [Application Screenshots Guide](#6-application-screenshots-guide)
7. [Local Setup](#7-local-setup)
8. [Gradle Workflow](#8-gradle-workflow)
9. [Unit Testing](#9-unit-testing)
10. [Docker Setup](#10-docker-setup)
11. [Docker Health Check](#11-docker-health-check)
12. [Jenkins Controller-Agent Setup](#12-jenkins-controller-agent-setup)
13. [Jenkins Remoting](#13-jenkins-remoting)
14. [Azure Pipelines Config](#14-azure-pipelines-config)
15. [ACR Setup](#15-acr-setup)
16. [Azure App Service Deployment](#16-azure-app-service-deployment)
17. [E2E Lifecycle Flow](#17-e2e-lifecycle-flow)
18. [Health Monitoring](#18-health-monitoring)
19. [Troubleshooting Guide](#19-troubleshooting-guide)
20. [Project Structure](#20-project-structure)
21. [CodeAlpha Task Mapping](#21-codealpha-task-mapping)
22. [Future Scope](#22-future-scope)

---

## 1. Overview

DevOpsPulse is a reference-grade Spring Boot 3.2 application (Java 21) whose real purpose is not the UI, but the infrastructure and automation wrapped around it. It exposes a small web dashboard and a JSON health/status API, and ships with every artifact needed to build, containerize, test, and deploy it through a realistic dual CI/CD path: a self-hosted Jenkins Controller-Agent pipeline and a cloud-native Azure Pipelines flow targeting Azure Container Registry and Azure App Service.

The project is intentionally weighted **~20% frontend / ~80% infrastructure & DevOps engineering** — the dashboard exists mainly to visualize the pipeline and runtime health that the DevOps tooling produces.

## 2. Objectives

- Provide a working, non-trivial Spring Boot service with a real build, test, and packaging story.
- Demonstrate a reproducible Gradle Wrapper build requiring no local Gradle installation.
- Package the application into a minimal, secure, multi-stage Alpine Docker image.
- Automate build → test → containerize → verify using a distributed Jenkins Controller-Agent pipeline.
- Automate build → containerize → push → deploy → verify using Azure Pipelines, Azure Container Registry, and Azure App Service.
- Expose first-class health and status endpoints consumed by every stage of both pipelines and by the human-facing dashboard.

## 3. Features

- **Home page** — app identity, tagline, live health badge, and system overview cards.
- **SRE Dashboard** (`/dashboard`) — build status, uptime, JVM runtime metrics, environment details, and a visual CI/CD pipeline flow diagram.
- **About page** (`/about`) — architecture, tech stack, and task breakdown.
- **`GET /health`** — minimal JSON health probe used by Docker, Jenkins, and Azure Pipelines.
- **`GET /api/status`** — full status dictionary (application, build, deployment, health, runtime).
- **Spring Actuator** — `/actuator/health`, `/actuator/info`, `/actuator/metrics`.
- Dark, Grafana/Azure-DevOps-inspired dashboard UI.
- Full JUnit 5 + MockMvc test coverage of the health API.
- Multi-stage, non-root, Alpine-based Docker image with a built-in `HEALTHCHECK`.
- Jenkinsfile using strict Controller-Agent scheduling (`agent { label 'linux-docker' }`).
- Azure Pipelines YAML covering build, push, deploy, and post-deployment verification.

## 4. Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.x |
| Web | Spring Web (MVC + REST) |
| Views | Thymeleaf |
| Monitoring | Spring Boot Actuator |
| Build | Gradle 8.7 (via Gradle Wrapper) |
| Testing | JUnit 5, Spring Boot Test, MockMvc |
| Containers | Docker (multi-stage, Eclipse Temurin Alpine images) |
| Distributed CI | Jenkins (Controller-Agent, Jenkins Remoting) |
| Cloud CI/CD | Azure Pipelines |
| Registry | Azure Container Registry (ACR) |
| Hosting | Azure App Service (Web App for Containers) |

## 5. Architecture

```
                       ┌──────────────────────────┐
                       │   Thymeleaf Web Layer     │
                       │  (/, /dashboard, /about)  │
                       └────────────┬─────────────┘
                                    │
                       ┌────────────▼─────────────┐
                       │   REST API Layer          │
                       │  (/health, /api/status)   │
                       └────────────┬─────────────┘
                                    │
                       ┌────────────▼─────────────┐
                       │   StatusService           │
                       │  (build/env/runtime data) │
                       └───────────────────────────┘
```

Both the web dashboard and the REST API are backed by the same `StatusService`, so what an operator sees on `/dashboard` is guaranteed to match what automated pipelines observe via `/health` and `/api/status`.

## 6. Application Screenshots Guide

This repository ships as source only (no binary screenshots). To capture your own:

1. Run the app locally (see [Local Setup](#7-local-setup)).
2. Visit `http://localhost:8080/` for the home page — capture the hero section and status badge.
3. Visit `http://localhost:8080/dashboard` — capture the metrics cards and pipeline flow diagram.
4. Visit `http://localhost:8080/about` — capture the tech stack grid.
5. Hit `http://localhost:8080/health` and `http://localhost:8080/api/status` in a browser or via `curl | jq` to capture the JSON payloads.

Store screenshots under a `docs/screenshots/` directory and reference them here if you want a fully illustrated README.

## 7. Local Setup

**Prerequisites:** JDK 21 (Gradle Wrapper handles the rest — no local Gradle install required).

```bash
git clone <your-repo-url> devopspulse
cd devopspulse
chmod +x gradlew          # Linux/macOS only
./gradlew bootRun         # Linux/macOS
# or
gradlew.bat bootRun       # Windows
```

The application starts on `http://localhost:8080`.

Optional: override the environment label shown in the dashboard:

```bash
APP_ENV=Development ./gradlew bootRun
```

## 8. Gradle Workflow

| Command | Purpose |
|---|---|
| `./gradlew clean` | Remove previous build output |
| `./gradlew build` | Full build: compile, test, package |
| `./gradlew test` | Run the JUnit 5 test suite only |
| `./gradlew bootJar` | Package the executable jar (`build/libs/devopspulse.jar`) |
| `./gradlew bootRun` | Run the application directly with Gradle |

The wrapper (`gradlew` / `gradlew.bat`) pins the project to Gradle 8.7 (see `gradle/wrapper/gradle-wrapper.properties`), so every environment — a developer laptop, a Jenkins agent, or an Azure Pipelines hosted runner — builds with an identical Gradle version.

## 9. Unit Testing

Tests live under `src/test/java/com/devopspulse` and use Spring Boot Test + MockMvc against the real Spring context.

```bash
./gradlew test
```

`HealthApiControllerTest` asserts:
- `GET /health` returns HTTP 200 with `status`, `application`, and `version` fields.
- `GET /api/status` returns HTTP 200 with nested `application`, `build`, `deployment`, `health`, and `runtime` sections.

Test reports are written to `build/reports/tests/test/index.html` and JUnit XML to `build/test-results/test/`, which both Jenkins (`junit` step) and Azure Pipelines (`PublishTestResults@2`) consume directly.

## 10. Docker Setup

Build the image locally:

```bash
docker build -t devopspulse:local .
```

Run it:

```bash
docker run -d -p 8080:8080 --name devopspulse devopspulse:local
```

Visit `http://localhost:8080`. The Dockerfile is a two-stage build:

1. **Build stage** — `eclipse-temurin:21-jdk-alpine`, runs `./gradlew bootJar --no-daemon`.
2. **Runtime stage** — `eclipse-temurin:21-jre-alpine`, copies only the built jar, runs as a non-root `devopspulse` user, and exposes port `8080`.

## 11. Docker Health Check

The image declares a native `HEALTHCHECK`:

```dockerfile
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1
```

Inspect it directly:

```bash
docker ps                      # STATUS column shows (healthy) / (unhealthy)
docker inspect --format='{{json .State.Health}}' devopspulse | jq
```

This same `/health` endpoint is what the Jenkins "Docker Health Audit" stage and the Azure Pipelines "Post-Deployment Health Verification" stage both call.

## 12. Jenkins Controller-Agent Setup

DevOpsPulse assumes a standard Jenkins **Controller-Agent** topology:

1. **Controller** — hosts the Jenkins UI, job definitions, and orchestrates scheduling. It does **not** execute build steps for this pipeline.
2. **Agent(s)** — one or more nodes labeled `linux-docker` (Linux, Docker Engine installed, JDK 21 available or provisioned via a tool installer) that actually execute every stage.

Register an agent under **Manage Jenkins → Nodes → New Node**, set its **Labels** field to `linux-docker`, and connect it either via SSH (controller-initiated) or the JNLP/WebSocket agent (agent-initiated) launch method.

The `Jenkinsfile` pins execution with:

```groovy
agent { label 'linux-docker' }
```

so the pipeline only ever schedules onto a matching agent, never the controller.

## 13. Jenkins Remoting

Jenkins Remoting is the protocol/channel Jenkins uses for controller ↔ agent communication (command dispatch, file transfer, log streaming) once an agent connects, whether over SSH or the JNLP/WebSocket inbound agent. Practically, this means:

- The controller never runs `sh` steps itself — they are serialized over the Remoting channel and executed on the connected agent's JVM.
- Build artifacts, test result files, and console output are streamed back to the controller over the same channel.
- Agents can be ephemeral (e.g., spun up per build, common in containerized/cloud agent setups) as long as they connect with the correct label before the pipeline's `agent { label 'linux-docker' }` block is scheduled.

No special configuration is needed in this repository beyond ensuring at least one connected agent advertises the `linux-docker` label.

## 14. Azure Pipelines Config

`azure-pipelines.yml` defines four sequential stages, triggered on pushes to `main`. It is deliberately **Docker-daemon-free**: nothing in the pipeline (or on your own machine, if you're working from Azure Cloud Shell) needs a local `docker` engine running. The image is built entirely inside Azure Container Registry's own build service.

1. **BuildAndTest** — installs JDK 21, runs `./gradlew clean test bootJar`, publishes JUnit results and the built jar as a pipeline artifact.
2. **BuildAndPushImage** — uses the `AzureCLI@2` task to run `az acr build`, which uploads the build context and builds+pushes the image **inside ACR itself**, tagged with `$(Build.BuildId)` and `latest`. No `Docker@2` task, no Docker Registry service connection, no daemon anywhere in the chain.
3. **DeployToAppService** — uses `AzureWebAppContainer@1` to point the target App Service at the newly pushed image tag.
4. **HealthVerification** — waits briefly for the app to warm up, then `curl`s `/health` on the live App Service URL and fails the pipeline if it doesn't return HTTP 200.

Before running the pipeline, create **one** service connection in **Project Settings → Service connections**:

- `devopspulse-azure-connection` — an Azure Resource Manager (ARM) service connection whose service principal has both `AcrPush`/`Contributor` rights on the ACR instance and rights to deploy to the target App Service. This single connection is reused by both the `AzureCLI@2` build step and the `AzureWebAppContainer@1` deploy step.

Update `variables.acrName`, `variables.acrRegistry`, `variables.appServiceName`, and `variables.appServiceUrl` to match your Azure resources.

## 15. ACR Setup

```bash
# Create a resource group (skip if one already exists)
az group create --name devopspulse-rg --location eastus

# Create the Azure Container Registry
az acr create --resource-group devopspulse-rg \
  --name devopspulseacr --sku Basic

# Build and push the image entirely in the cloud — no local Docker
# daemon required. This works from Azure Cloud Shell, a laptop with
# only the Azure CLI installed, or any CI agent.
az acr build --registry devopspulseacr --image devopspulse:latest .
```

`az acr build` uploads the current directory as build context and runs the multi-stage `Dockerfile` inside ACR's managed build service, then pushes the resulting image straight to the registry — so a full round trip (build + push) is a single command with nothing but the Azure CLI installed.

In Azure DevOps, you do **not** need a separate Docker Registry service connection for this approach. Instead, the single ARM service connection described in [Azure Pipelines Config](#14-azure-pipelines-config) is granted `AcrPush` (or `Contributor`) on the registry, and the pipeline's `AzureCLI@2` task runs `az acr build` on your behalf.

## 16. Azure App Service Deployment

```bash
# Create an App Service plan for Linux containers
az appservice plan create --name devopspulse-plan \
  --resource-group devopspulse-rg --is-linux --sku B1

# Create the Web App for Containers, seeded with an initial image
az webapp create --resource-group devopspulse-rg \
  --plan devopspulse-plan --name devopspulse-app \
  --deployment-container-image-name devopspulseacr.azurecr.io/devopspulse:latest

# Point the App Service at your ACR and enable continuous deployment
az webapp config container set --name devopspulse-app \
  --resource-group devopspulse-rg \
  --container-image-name devopspulseacr.azurecr.io/devopspulse:latest \
  --container-registry-url https://devopspulseacr.azurecr.io

# Set the runtime environment label surfaced on the dashboard
az webapp config appsettings set --name devopspulse-app \
  --resource-group devopspulse-rg --settings APP_ENV=Production WEBSITES_PORT=8080
```

`WEBSITES_PORT=8080` tells Azure App Service which port the container listens on, matching the `EXPOSE 8080` / `server.port=8080` configuration in this repo.

## 17. E2E Lifecycle Flow

```
Developer commit
      │
      ▼
     Git  ── push to main ──▶  GitHub
      │                           │
      │                           ├──▶ Jenkins Controller-Agent Pipeline
      │                           │        Checkout → Gradle Build & Test
      │                           │        → Docker Build → Docker Health Audit
      │                           │
      │                           └──▶ Azure Pipelines
      │                                    Build & Unit Test
      │                                    → az acr build (cloud-side, no Docker daemon) → ACR
      │                                    → Deploy → Azure App Service
      │                                    → Post-Deployment Health Verification
      ▼
 Live service at https://<app-name>.azurewebsites.net
 Monitored continuously via /health and /api/status
```

Both pipelines are independent and can run in parallel (e.g., Jenkins for internal validation, Azure Pipelines as the deployment path of record), or either can be adopted standalone.

## 18. Health Monitoring

Three layers of health signal are available:

| Layer | Endpoint | Consumer |
|---|---|---|
| Application | `GET /health` | Docker `HEALTHCHECK`, Jenkins Health Audit, Azure Pipelines verification, uptime monitors |
| Application | `GET /api/status` | SRE dashboard, dashboards/alerting tooling |
| Framework | `GET /actuator/health` | Kubernetes-style liveness/readiness probes, Azure App Service health check path |

For Azure App Service's built-in health check feature, configure the health check path to `/health`:

```bash
az webapp config set --name devopspulse-app --resource-group devopspulse-rg \
  --generic-configurations '{"healthCheckPath": "/health"}'
```

## 19. Troubleshooting Guide

| Symptom | Likely Cause | Fix |
|---|---|---|
| `./gradlew: Permission denied` | Wrapper script not executable | `chmod +x gradlew` |
| Gradle build fails to resolve dependencies | No network access / proxy blocking Maven Central | Configure a Gradle mirror or proxy in `gradle.properties` |
| Docker build fails at `bootJar` step | Source copied before wrapper, or wrapper jar missing | Ensure `gradle/wrapper/gradle-wrapper.jar` is committed and not `.gitignore`d |
| `HEALTHCHECK` shows `unhealthy` | App still starting, or port mismatch | Increase `--start-period`, confirm `EXPOSE 8080` matches `server.port` |
| Jenkins stage stuck in "waiting for next available executor" | No agent with label `linux-docker` connected | Connect/label an agent as described in [Jenkins Controller-Agent Setup](#12-jenkins-controller-agent-setup) |
| `az acr build` fails with auth/permission error | ARM service connection lacks `AcrPush` rights on the registry | Recreate/update `devopspulse-azure-connection`'s service principal to grant `AcrPush` (or `Contributor`) on the ACR instance |
| Post-deployment health check fails with HTTP 000 | App Service not warmed up yet, or wrong `WEBSITES_PORT` | Increase the `sleep` delay; confirm `WEBSITES_PORT=8080` app setting |
| Dashboard shows `Environment: Production` unexpectedly | `APP_ENV` not set in the target environment | Set `APP_ENV` via `bootRun`, `docker run -e APP_ENV=...`, or App Service app settings |

## 20. Project Structure

```
devopspulse/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── Dockerfile
├── Jenkinsfile
├── azure-pipelines.yml
├── README.md
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── src/
    ├── main/
    │   ├── java/com/devopspulse/
    │   │   ├── DevOpsPulseApplication.java
    │   │   ├── controller/
    │   │   │   ├── HomeController.java
    │   │   │   └── HealthApiController.java
    │   │   └── service/
    │   │       └── StatusService.java
    │   └── resources/
    │       ├── application.properties
    │       ├── static/css/style.css
    │       └── templates/
    │           ├── index.html
    │           ├── dashboard.html
    │           └── about.html
    └── test/
        └── java/com/devopspulse/
            └── HealthApiControllerTest.java
```

## 21. CodeAlpha Task Mapping

If this project is submitted as part of a CodeAlpha DevOps internship track, the deliverables map as follows:

| CodeAlpha Task | Where it lives in DevOpsPulse |
|---|---|
| Set up a CI/CD pipeline | `Jenkinsfile` and `azure-pipelines.yml` |
| Containerize an application | `Dockerfile` (multi-stage, Alpine) |
| Deploy to a cloud platform | `azure-pipelines.yml` → ACR → Azure App Service stages |
| Implement health checks / monitoring | `/health`, `/api/status`, Docker `HEALTHCHECK`, Actuator |
| Version control workflow | Git-based trigger (`trigger: branches: include: [main]`) |
| Automated testing | `HealthApiControllerTest.java`, wired into both pipelines |

Adjust this mapping to match the exact task list provided by your program if it differs.

## 22. Future Scope

- Add a persistence layer (PostgreSQL / Azure Database) with a real readiness check that verifies DB connectivity.
- Introduce Kubernetes manifests (or Helm chart) as an alternative deployment target to Azure App Service.
- Add Prometheus metrics export (`micrometer-registry-prometheus`) alongside Actuator for external monitoring stacks.
- Integrate SonarQube/SonarCloud static analysis as an additional Jenkins/Azure Pipelines stage.
- Add blue-green or canary deployment slots on Azure App Service with automated slot-swap verification.
- Add role-based authentication (Spring Security) protecting `/dashboard` in non-development environments.

---

**DevOpsPulse v1.0.0** — Java 21 · Spring Boot 3.2 · Gradle · Docker · Jenkins · Azure
