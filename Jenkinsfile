pipeline {
    agent any
    environment {
        DOTNET_ROOT = '/usr/bin'
        PATH = "${env.DOTNET_ROOT}:${env.PATH}"
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
            echo '全流程完成，服务已部署在 http://<你的 Jenkins 主机>:8081'
        }
        failure {
            echo '构建失败，请查看日志'
        }
    }
}
