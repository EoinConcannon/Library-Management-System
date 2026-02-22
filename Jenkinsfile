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
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Unit Tests') {
            steps {
                echo 'Running tests...'
                sh 'mvn test'
            }
        }
    }
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
    }
}