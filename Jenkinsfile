pipeline {
    agent any
    environment {
        DOTNET_ROOT = '/usr/bin'
        PATH = "${env.DOTNET_ROOT}:${env.PATH}"
        SONARQUBE = 'MySonar'
        SCANNER_HOME = tool name: 'SonarQube Scanner'
    }
    stages {
        stage('Check Folder') {
            steps {
                sh 'pwd'
                sh 'ls -al'
            }
        }
        stage('Restore') {
            steps {
                sh 'dotnet restore JenkinsDemo/JenkinsDemo.sln'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE}") {
                    sh "${SCANNER_HOME}/bin/sonar-scanner " +
                        "-Dsonar.projectKey=${env.JOB_NAME} " +
                        '-Dsonar.sources=. ' +
                        "-Dsonar.host.url=${env.SONAR_HOST_URL} " +
                        "-Dsonar.login=${env.SONAR_AUTH_TOKEN}"
                }
            }
        }
        stage('Build') {
            steps {
                sh 'dotnet build JenkinsDemo/JenkinsDemo.sln --no-restore -c Release'
            }
        }
        stage('Test') {
            steps {
                sh 'dotnet test JenkinsDemo/JenkinsDemo.sln --no-build --verbosity normal'
            }
        }
        stage('Publish') {
            steps {
                sh 'dotnet publish JenkinsDemo/JenkinsDemo/JenkinsDemo.csproj -c Release -o publish'
            }
        }
        stage('Archive Artifact') {
            steps {
                sh 'zip -r publish.zip publish'
                archiveArtifacts artifacts: 'publish.zip', fingerprint: true
            }
        }
        stage('Docker Build & Run') {
            steps {
                sh '''
                  docker build -t jenkins-demo:latest .
                  docker stop jenkins-demo || true
                  docker rm jenkins-demo || true
                  docker run -d -p 8081:80 --name jenkins-demo jenkins-demo:latest
                '''
            }
        }
    }

    post {
        success {
            slackSend channel: '#jenkins-builds', color: 'good',
              tokenCredentialId: 'slack-bot-token',
              message: "✅ Build #${env.BUILD_NUMBER} 成功！ (<${env.BUILD_URL}|查看>)"
            // Email 通知
            emailext subject: "✅ Build #${env.BUILD_NUMBER} Success",
             body: """\
                    Jenkins Job: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    Status: SUCCESS
                    See: ${env.BUILD_URL}
                    """,
             to: 'howard199887@gmail.com'
        }
        failure {
            slackSend channel: '#jenkins-builds', color: 'danger',
              tokenCredentialId: 'slack-bot-token',
              message: "❌ Build #${env.BUILD_NUMBER} 失败！ (<${env.BUILD_URL}|查看>)"
            emailext subject: "❌ Build #${env.BUILD_NUMBER} Failed",
             body: """\
                    Jenkins Job: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    Status: FAILURE
                    See: ${env.BUILD_URL}console
                    """,
             to: 'howard199887@gmail.com'
        }
        always {
            echo "Finished build of ${env.BRANCH_NAME}"
        }
    }
}
