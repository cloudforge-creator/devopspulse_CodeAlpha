// =========================================================================
// DevOpsPulse - Jenkins Declarative Pipeline
// Runs on a distributed Jenkins Controller-Agent architecture over
// Jenkins Remoting. This pipeline never executes on the controller itself;
// every stage is constrained to an agent labeled 'linux-docker'.
// =========================================================================

pipeline {

    agent { label 'linux-docker' }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        IMAGE_NAME     = 'devopspulse'
        IMAGE_TAG      = "${env.BUILD_NUMBER}"
        HEALTH_PORT    = '8080'
        CONTAINER_NAME = "devopspulse-health-audit-${env.BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Checking out DevOpsPulse source on agent: ${env.NODE_NAME}"
                checkout scm
            }
        }

        stage('Gradle Build & Test') {
            steps {
                echo 'Running Gradle build and JUnit 5 test suite...'
                sh 'chmod +x ./gradlew'
                sh './gradlew clean test bootJar --no-daemon'
            }
            post {
                always {
                    junit testResults: 'build/test-results/test/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo "Building Docker image ${IMAGE_NAME}:${IMAGE_TAG}"
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
            }
        }

        stage('Docker Health Audit') {
            steps {
                echo 'Starting temporary container to verify /health endpoint...'
                sh """
                    docker run -d --rm \
                        --name ${CONTAINER_NAME} \
                        -p 0:${HEALTH_PORT} \
                        ${IMAGE_NAME}:${IMAGE_TAG}
                """
                script {
                    def hostPort = sh(
                        script: "docker port ${CONTAINER_NAME} ${HEALTH_PORT}/tcp | cut -d: -f2",
                        returnStdout: true
                    ).trim()

                    echo "Container mapped to host port ${hostPort}. Waiting for readiness..."

                    def healthy = false
                    for (int attempt = 1; attempt <= 10; attempt++) {
                        def httpCode = sh(
                            script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:${hostPort}/health || true",
                            returnStdout: true
                        ).trim()

                        if (httpCode == '200') {
                            healthy = true
                            echo "Health check passed on attempt ${attempt} (HTTP ${httpCode})"
                            break
                        }
                        echo "Attempt ${attempt}: health endpoint returned HTTP ${httpCode}. Retrying in 3s..."
                        sleep 3
                    }

                    if (!healthy) {
                        error 'Docker Health Audit FAILED: /health did not return HTTP 200 within the retry window.'
                    }
                }
            }
            post {
                always {
                    sh "docker stop ${CONTAINER_NAME} || true"
                }
            }
        }
    }

    post {
        success {
            echo "DevOpsPulse build #${env.BUILD_NUMBER} completed successfully on ${env.NODE_NAME}."
        }
        failure {
            echo "DevOpsPulse build #${env.BUILD_NUMBER} FAILED. Check stage logs above."
        }
        always {
            sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
        }
    }
}
