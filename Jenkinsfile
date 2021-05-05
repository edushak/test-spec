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
        stage('Publish Artifact to Nexus'){
            steps {
                nexusArtifactUploader artifacts: [
                                        [artifactId: 'test-spec',
                                         classifier: '',
                                         file: 'build/libs/workspace-0.0.1.jar',
                                         type: 'jar']],
                                         credentialsId: 'nexuslogin',
                                         groupId: 'DEV',
                                         nexusUrl: 'nexus-nexus-repo.bnsf-nonprod-dfw-e648741016b5b16f9b585588dcd0ed80-0000.us-south.containers.appdomain.cloud',
                                         nexusVersion: 'nexus3',
                                         protocol: 'http',
                                         repository: 'spec-test-release',
                                         version: 'Version.1.0.${BUILD_ID}'
            }
        }
    }
}