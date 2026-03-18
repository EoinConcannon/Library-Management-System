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
        
        stage('End to End Tests') {
    steps {
        echo 'Starting application for E2E tests...'
        bat 'start /B mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djwt.secret=thisisatestjwtsecretthatisfullylongenoughtosatisfyhmacsha256requirements -Djwt.expiration=86400000"'
        bat 'ping -n 45 127.0.0.1 > nul'
        echo 'Running end to end tests...'
        bat 'mvn test -Dtest=CucumberRunnerE2E'
    }
    post {
        always {
            echo 'Stopping application...'
            bat 'FOR /F "tokens=5" %%P IN (\'netstat -ano ^| findstr ":8080"\') DO taskkill /PID %%P /F 2>nul & exit /b 0'
        }
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
        echo 'SonarQube analysis complete. Check results at http://localhost:9000'
    }
}
    }

    post {
    always {
        junit allowEmptyResults: true,
              testResults: '**/target/surefire-reports/*.xml'
        junit allowEmptyResults: true,
              testResults: '**/target/failsafe-reports/*.xml'
        publishHTML(target: [
            allowMissing: true,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: 'target/site/jacoco',
            reportFiles: 'index.html',
            reportName: 'JaCoCo Code Coverage'
        ])
    }
}
}