pipeline {
    agent any

    tools {
        maven 'Maven_3'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Cloning repository...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building...'
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Unit Tests') {
            steps {
                echo 'Running unit tests...'
                bat 'mvn test'
            }
        }

        stage('Integration Tests') {
            steps {
                echo 'Running integration tests...'
                bat 'mvn failsafe:integration-test failsafe:verify -Dspring.profiles.active='
            }
        }

        stage('Code Coverage') {
            steps {
                echo 'Generating coverage report...'
                bat 'mvn jacoco:report'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis...'
                withSonarQubeEnv('SonarQube') {
                    bat 'mvn sonar:sonar'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo 'Checking Quality Gate...'
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true,
                  testResults: '**/target/surefire-reports/*.xml'
            junit allowEmptyResults: true,
                  testResults: '**/target/failsafe-reports/*.xml'
        }
    }
}