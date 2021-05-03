pipeline {
    agent any
    stages {
        stage('Clean'){
            steps {
                sh './gradlew --stacktrace clean'
            }
        }
        stage('Build'){
            steps {
                sh './gradlew --stacktrace build'
            }
        }
        stage('Test'){
            steps {
                sh './gradlew --stacktrace test'
            }
        }
    }
}