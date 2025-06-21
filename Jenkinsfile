pipeline {
    agent any
    environment {
        DOTNET_ROOT = '/usr/bin'
        PATH = "${env.DOTNET_ROOT}:${env.PATH}"
        SONARQUBE = 'MySonar'
        SCANNER_HOME = tool name: 'SonarQube_Scanner'
    }
    stages {
        stage('Env Setup') {
            steps {
                sh 'chmod +x scripts/env-setup.sh'
                sh 'sudo scripts/env-setup.sh'
            }
        }
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
                withSonarQubeEnv(SONARQUBE) {
                    script {
                        def key = env.JOB_NAME.replace('/', '_')
                        sh """
                        ${tool 'SonarQube_Scanner'}/bin/sonar-scanner \
                            -Dsonar.projectKey=${key} \
                            -Dsonar.sources=.
                        """
                    }
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
        stage('Docker Deploy') {
            steps {
                sh 'chmod +x scripts/deploy.sh'
                sh 'scripts/deploy.sh'
            }
        }
        stage('Health Check') {
            steps {
                sh 'chmod +x scripts/health-check.sh'
                sh 'scripts/health-check.sh localhost 8081'
            }
        }
        stage('Cleanup') {
            steps {
                sh 'chmod +x scripts/cleanup.sh'
                sh 'scripts/cleanup.sh'
            }
        }
    }
    post {
        success {
            script {
                def branch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
                def ts = new Date().format('yyyy-MM-dd HH:mm:ss', TimeZone.getTimeZone('Asia/Taipei'))

                slackSend channel: '#jenkins-builds',
                          color: 'good',
                          tokenCredentialId: 'slack-bot-token',
                          message: "✅ Build #${env.BUILD_NUMBER} 成功！\n分支：`${branch}`\n時間：${ts}\n<${env.BUILD_URL}|查看詳情>"
            }
        }
        failure {
            script {
                def branch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
                def ts = new Date().format('yyyy-MM-dd HH:mm:ss', TimeZone.getTimeZone('Asia/Taipei'))

                slackSend channel: '#jenkins-builds',
                          color: 'danger',
                          tokenCredentialId: 'slack-bot-token',
                          message: "❌ Build #${env.BUILD_NUMBER} 失敗！\n分支：`${branch}`\n時間：${ts}\n<${env.BUILD_URL}|查看日誌>"
            }
        }
        always {
            echo "Finished build of ${env.BRANCH_NAME}"
        }
    }
}
