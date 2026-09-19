pipeline {


agent any

options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '20'))
    timeout(time: 30, unit: 'MINUTES')
}

environment {
    IMAGE_NAME     = 'devopspulse'
    IMAGE_TAG      = "${env.BUILD_NUMBER}"
    HOST_PORT      = '18081'
    CONTAINER_PORT = '8081'
    CONTAINER_NAME = "devopspulse-health-audit-${env.BUILD_NUMBER}"
}

stages {

    stage('Checkout') {
        steps {
            echo "Checking out DevOpsPulse source on agent: ${env.NODE_NAME}"
            checkout scm
        }
    }

    stage('Environment Check') {
        steps {
            echo 'Checking Java and Docker availability...'

            bat 'java -version'
            bat 'docker --version'
            bat 'docker info'
        }
    }

    stage('Gradle Build & Test') {
        steps {
            echo 'Running Gradle build and JUnit 5 test suite...'

            bat 'gradlew.bat clean test bootJar --no-daemon'
        }

        post {
            always {
                junit(
                    testResults: 'build/test-results/test/*.xml',
                    allowEmptyResults: true
                )

                archiveArtifacts(
                    artifacts: 'build/libs/*.jar',
                    fingerprint: true
                )
            }
        }
    }

    stage('Docker Build') {
        steps {
            echo "Building Docker image ${IMAGE_NAME}:${IMAGE_TAG}"

            bat 'docker build -t %IMAGE_NAME%:%IMAGE_TAG% -t %IMAGE_NAME%:latest .'
        }
    }

    stage('Docker Health Audit') {
        steps {

            echo 'Starting temporary Docker container...'

            bat """
                docker run -d ^
                    --name %CONTAINER_NAME% ^
                    -p %HOST_PORT%:%CONTAINER_PORT% ^
                    %IMAGE_NAME%:%IMAGE_TAG%
            """

            script {

                def healthy = false

                echo 'Waiting for DevOpsPulse container to become healthy...'

                for (int attempt = 1; attempt <= 15; attempt++) {

                    def httpCode = bat(
                        script: '@powershell -NoProfile -Command "try { (Invoke-WebRequest -UseBasicParsing http://localhost:%HOST_PORT%/actuator/health).StatusCode } catch { 0 }"',
                        returnStdout: true
                    ).trim()

                    echo "Health check attempt ${attempt}: HTTP ${httpCode}"

                    if (httpCode == '200') {
                        healthy = true
                        echo 'DevOpsPulse health check PASSED.'
                        break
                    }

                    sleep 3
                }

                if (!healthy) {

                    echo 'Container logs:'

                    bat 'docker logs %CONTAINER_NAME%'

                    error(
                        'Docker Health Audit FAILED: /actuator/health did not return HTTP 200.'
                    )
                }
            }
        }

        post {
            always {
                bat 'docker rm -f %CONTAINER_NAME% 2>nul || exit /b 0'
            }
        }
    }
}

post {

    success {
        echo "DevOpsPulse build #${env.BUILD_NUMBER} completed successfully."
        echo "Docker image: ${IMAGE_NAME}:${IMAGE_TAG}"
    }

    failure {
        echo "DevOpsPulse build #${env.BUILD_NUMBER} FAILED. Check the stage logs."
    }

    always {
        bat 'docker rmi %IMAGE_NAME%:%IMAGE_TAG% 2>nul || exit /b 0'
    }
}


}

