pipeline {
    agent any
    stages {
        stage('Clean'){
            steps {
                sh './gradlew --stacktrace clean'
            }
        }
        stage('Check'){
            steps {
                sh './gradlew --stacktrace check'
            }
        }
        stage('Test'){
            steps {
                sh './gradlew --stacktrace test'
                junit 'build/test-results/**/*.xml'
            }
        }
        stage('Build'){
            steps {
                sh './gradlew --stacktrace build'
                archiveArtifacts artifacts: 'build/libs/**/*.jar', fingerprint: true
                echo "Running ${env.BUILD_ID}"
            }
        }
    }
}