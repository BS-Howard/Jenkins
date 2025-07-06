#!/usr/bin/env groovy

def call(Map config = [:]) {
    def defaults = [
        solutionPath: 'JenkinsDemo/JenkinsDemo.sln',
        projectPath: 'JenkinsDemo/JenkinsDemo/JenkinsDemo.csproj',
        buildConfig: 'Release',
        outputPath: 'publish',
        sonarQube: 'MySonar',
        skipTests: false,
        timeout: 10
    ]
    
    config = defaults + config
    
    pipeline {
        agent any
        
        environment {
            DOTNET_ROOT = '/usr/bin'
            PATH = "${env.DOTNET_ROOT}:${env.PATH}"
            SCANNER_HOME = tool name: 'SonarQube_Scanner'
        }
        
        stages {
            stage('Environment Setup') {
                steps {
                    script {
                        echo "🔧 Setting up environment for .NET build..."
                        sh 'chmod +x scripts/env-setup.sh'
                        sh 'sudo scripts/env-setup.sh'
                    }
                }
            }
            
            stage('Restore Dependencies') {
                steps {
                    script {
                        echo "📦 Restoring .NET dependencies..."
                        sh "dotnet restore ${config.solutionPath}"
                    }
                }
            }
            
            stage('Code Quality Analysis') {
                when {
                    anyOf {
                        branch 'main'
                        branch 'develop'
                        expression { !config.skipTests }
                    }
                }
                steps {
                    script {
                        echo "🔍 Running SonarQube analysis..."
                        withSonarQubeEnv(config.sonarQube) {
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
                options {
                    timeout(time: config.timeout, unit: 'MINUTES')
                    retry(3)
                }
                steps {
                    script {
                        echo "🏗️ Building .NET application..."
                        sh "dotnet build ${config.solutionPath} --no-restore -c ${config.buildConfig}"
                    }
                }
            }
            
            stage('Test') {
                when {
                    expression { !config.skipTests }
                }
                steps {
                    script {
                        echo "🧪 Running tests..."
                        parallel(
                            "Unit Tests": {
                                sh "dotnet test ${config.solutionPath} --no-build --verbosity normal --filter 'Category=Unit'"
                            },
                            "Integration Tests": {
                                sh "dotnet test ${config.solutionPath} --no-build --verbosity normal --filter 'Category=Integration'"
                            }
                        )
                    }
                }
            }
            
            stage('Publish') {
                steps {
                    script {
                        echo "📤 Publishing application..."
                        sh "dotnet publish ${config.projectPath} -c ${config.buildConfig} -o ${config.outputPath}"
                    }
                }
            }
            
            stage('Archive Artifacts') {
                steps {
                    script {
                        echo "📦 Archiving artifacts..."
                        sh "zip -r ${config.outputPath}.zip ${config.outputPath}"
                        archiveArtifacts artifacts: "${config.outputPath}.zip", fingerprint: true
                    }
                }
            }
        }
        
        post {
            always {
                cleanWs()
            }
            success {
                script {
                    def branch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
                    def ts = new Date().format('yyyy-MM-dd HH:mm:ss', TimeZone.getTimeZone('Asia/Taipei'))
                    def buildTime = currentBuild.durationString

                    slackSend channel: '#jenkins-builds',
                              color: 'good',
                              tokenCredentialId: 'slack-bot-token',
                              message: """
                              ✅ .NET Build #${env.BUILD_NUMBER} 成功！
                              專案：${env.JOB_NAME}
                              分支：`${branch}`
                              配置：`${config.buildConfig}`
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
                              ❌ .NET Build #${env.BUILD_NUMBER} 失敗！
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