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
            script {
                // 1. 获取当前分支和时间
                def branch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
                def ts = new Date().format('yyyy-MM-dd HH:mm:ss', TimeZone.getTimeZone('Asia/Taipei'))

                // 2. Slack 通知
                slackSend channel: '#jenkins-builds',
                color: 'good',
                tokenCredentialId: 'slack-bot-token',
                message: "✅ Build #${env.BUILD_NUMBER} 成功！\n" +
                         "分支：`${branch}`\n" +
                         "时间：${ts}\n" +
                         "<${env.BUILD_URL}|查看详情>"

                // 3. Email 通知
                emailext subject: "✅ [${branch}] Build #${env.BUILD_NUMBER} Success",
               body: """\
                        Jenkins Job: ${env.JOB_NAME}
                        Branch: ${branch}
                        Build Number: ${env.BUILD_NUMBER}
                        Time: ${ts}
                        Status: SUCCESS
                        Details: ${env.BUILD_URL}
                        """,
               to: 'howard199887@gmail.com'
            }
        }
        failure {
            script {
                def branch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
                def ts = new Date().format('yyyy-MM-dd HH:mm:ss', TimeZone.getTimeZone('Asia/Taipei'))

                slackSend channel: '#jenkins-builds',
                color: 'danger',
                tokenCredentialId: 'slack-bot-token',
                message: "❌ Build #${env.BUILD_NUMBER} 失败！\n" +
                         "分支：`${branch}`\n" +
                         "时间：${ts}\n" +
                         "<${env.BUILD_URL}|查看日志>"

                emailext subject: "❌ [${branch}] Build #${env.BUILD_NUMBER} Failed",
               body: """\
                        Jenkins Job: ${env.JOB_NAME}
                        Branch: ${branch}
                        Build Number: ${env.BUILD_NUMBER}
                        Time: ${ts}
                        Status: FAILURE
                        Log: ${env.BUILD_URL}console
                        """,
               to: 'howard199887@gmail.com'
            }
        }
        always {
            echo "Finished build of ${env.BRANCH_NAME}"
        }
    }
}
