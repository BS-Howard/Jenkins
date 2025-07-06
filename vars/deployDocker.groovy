#!/usr/bin/env groovy

def call(Map config = [:]) {
    def defaults = [
        imageName: 'jenkins-demo',
        containerName: 'jenkins-demo',
        port: 8081,
        hostPort: 8081,
        healthCheckUrl: 'http://localhost:8081',
        healthCheckTimeout: 30,
        deployScript: 'scripts/deploy.sh',
        healthCheckScript: 'scripts/health-check.sh',
        cleanupScript: 'scripts/cleanup.sh'
    ]

    config = defaults + config

    pipeline {
        agent any

        stages {
            stage('Docker Build') {
                steps {
                    script {
                        echo '🐳 Building Docker image...'
                        sh "docker build -t ${config.imageName}:latest ."
                    }
                }
            }

            stage('Stop Previous Container') {
                steps {
                    script {
                        echo '🛑 Stopping previous container...'
                        sh """
                        docker ps -a --filter "name=${config.containerName}" --format '{{.ID}}' | \
                          xargs -r docker rm -f
                        """
                    }
                }
            }

            stage('Deploy Container') {
                steps {
                    script {
                        echo '🚀 Deploying new container...'
                        sh 'chmod +x net-example/scripts/deploy.sh'
                        sh 'net-example/scripts/deploy.sh'
                    }
                }
            }

            stage('Health Check') {
                steps {
                    script {
                        echo '🔍 Performing health check...'
                        sh 'chmod +x net-example/scripts/health-check.sh'

                        // 等待服務啟動
                        sleep(time: 10, unit: 'SECONDS')

                        // 重試機制
                        retry(3) {
                            sh 'net-example/scripts/health-check.sh localhost 8081'
                        }
                    }
                }
            }

            stage('Cleanup') {
                steps {
                    script {
                        echo '🧹 Cleaning up...'
                        sh 'chmod +x net-example/scripts/cleanup.sh'
                        sh 'net-example/scripts/cleanup.sh'
                    }
                }
            }
        }

        post {
            success {
                script {
                    def branch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
                    def ts = new Date().format('yyyy-MM-dd HH:mm:ss', TimeZone.getTimeZone('Asia/Taipei'))
                    def buildTime = currentBuild.durationString

                    slackSend channel: '#jenkins-builds',
                              color: 'good',
                              tokenCredentialId: 'slack-bot-token',
                              message: """
                              ✅ Docker Deploy #${env.BUILD_NUMBER} 成功！
                              專案：${env.JOB_NAME}
                              分支：`${branch}`
                              容器：${config.containerName}
                              端口：${config.port}
                              建置時間：${buildTime}
                              時間：${ts}
                              <${env.BUILD_URL}|查看詳情>
                              """
                }
            }
            failure {
                script {
                    def branch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
                    def ts = new Date().format('yyyy-MM-dd HH:mm:ss', TimeZone.getTimeZone('Asia/Taipei'))

                    slackSend channel: '#jenkins-builds',
                              color: 'danger',
                              tokenCredentialId: 'slack-bot-token',
                              message: """
                              ❌ Docker Deploy #${env.BUILD_NUMBER} 失敗！
                              專案：${env.JOB_NAME}
                              分支：`${branch}`
                              階段：${currentBuild.description ?: '未知'}
                              時間：${ts}
                              <${env.BUILD_URL}|查看日誌>
                              """
                }
            }
        }
    }
}
