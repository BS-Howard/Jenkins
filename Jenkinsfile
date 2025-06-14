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
                sh 'dotnet restore JenkinsDemo.sln'
            }
        }
        stage('Build') {
            steps {
                sh 'dotnet build JenkinsDemo.sln --no-restore -c Release'
            }
        }
        stage('Test') {
            steps {
                sh 'dotnet test JenkinsDemo.sln --no-build --verbosity normal'
            }
        }
        stage('Publish') {
            steps {
                sh 'dotnet publish JenkinsDemo/JenkinsDemo.csproj -c Release -o publish'
            }
        }
    }
}
