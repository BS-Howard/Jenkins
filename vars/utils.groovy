#!/usr/bin/env groovy

// 取得 Git 資訊
def getGitInfo() {
    def gitInfo = [:]
    gitInfo.branch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
    gitInfo.commit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
    gitInfo.author = sh(script: 'git log -1 --pretty=format:"%an"', returnStdout: true).trim()
    gitInfo.message = sh(script: 'git log -1 --pretty=format:"%s"', returnStdout: true).trim()
    return gitInfo
}

// 格式化時間戳記
def getTimestamp() {
    return new Date().format('yyyy-MM-dd HH:mm:ss', TimeZone.getTimeZone('Asia/Taipei'))
}

// 取得建置時間
def getBuildDuration() {
    return currentBuild.durationString
}

// 檢查是否為主要分支
def isMainBranch() {
    def branch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
    return branch == 'main' || branch == 'master'
}

// 檢查是否為開發分支
def isDevelopBranch() {
    def branch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
    return branch == 'develop' || branch.startsWith('feature/')
}

// 發送 Slack 通知
def sendSlackNotification(String status, String message) {
    def gitInfo = getGitInfo()
    def ts = getTimestamp()
    def buildTime = getBuildDuration()
    
    def color = 'good'
    if (status == 'failure') {
        color = 'danger'
    } else if (status == 'unstable') {
        color = 'warning'
    }
    
    slackSend channel: '#jenkins-builds',
              color: color,
              tokenCredentialId: 'slack-bot-token',
              message: """
              ${status == 'success' ? '✅' : status == 'failure' ? '❌' : '⚠️'} Build #${env.BUILD_NUMBER} ${status == 'success' ? '成功' : status == 'failure' ? '失敗' : '不穩定'}！
              專案：${env.JOB_NAME}
              分支：`${gitInfo.branch}`
              提交：`${gitInfo.commit}`
              作者：${gitInfo.author}
              建置時間：${buildTime}
              時間：${ts}
              ${message}
              <${env.BUILD_URL}|查看詳情>
              """
}

// 執行健康檢查
def healthCheck(String url, int timeout = 30) {
    echo "🔍 執行健康檢查: ${url}"
    
    def maxRetries = 3
    def retryCount = 0
    
    while (retryCount < maxRetries) {
        try {
            def response = sh(
                script: "curl -f -s -o /dev/null -w '%{http_code}' --max-time ${timeout} ${url}",
                returnStdout: true
            ).trim()
            
            if (response == '200') {
                echo "✅ 健康檢查成功 (HTTP ${response})"
                return true
            } else {
                echo "⚠️ 健康檢查返回 HTTP ${response}"
                retryCount++
                if (retryCount < maxRetries) {
                    sleep(time: 5, unit: 'SECONDS')
                }
            }
        } catch (Exception e) {
            echo "❌ 健康檢查失敗: ${e.getMessage()}"
            retryCount++
            if (retryCount < maxRetries) {
                sleep(time: 5, unit: 'SECONDS')
            }
        }
    }
    
    error("健康檢查失敗，已重試 ${maxRetries} 次")
}

// 清理 Docker 資源
def cleanupDocker() {
    echo "🧹 清理 Docker 資源..."
    sh '''
        # 清理停止的容器
        docker container prune -f
        
        # 清理無標記的映像
        docker image prune -f
        
        # 清理未使用的網路
        docker network prune -f
        
        # 清理未使用的卷
        docker volume prune -f
    '''
}

// 檢查檔案是否存在
def fileExists(String path) {
    return fileExists(path)
}

// 取得檔案大小
def getFileSize(String path) {
    if (fileExists(path)) {
        return sh(script: "stat -c%s ${path}", returnStdout: true).trim()
    }
    return '0'
}

// 建立備份
def createBackup(String source, String backupDir = 'backups') {
    def timestamp = new Date().format('yyyyMMdd_HHmmss')
    def backupName = "${backupDir}/${source}_${timestamp}.tar.gz"
    
    sh "mkdir -p ${backupDir}"
    sh "tar -czf ${backupName} ${source}"
    
    echo "📦 備份已建立: ${backupName}"
    return backupName
}

// 驗證環境變數
def validateEnvironment(String[] requiredVars) {
    def missingVars = []
    
    requiredVars.each { var ->
        if (!env[var]) {
            missingVars.add(var)
        }
    }
    
    if (missingVars.size() > 0) {
        error("缺少必要的環境變數: ${missingVars.join(', ')}")
    }
    
    echo "✅ 環境變數驗證通過"
}

// 記錄建置資訊
def logBuildInfo() {
    def gitInfo = getGitInfo()
    def buildInfo = [
        jobName: env.JOB_NAME,
        buildNumber: env.BUILD_NUMBER,
        branch: gitInfo.branch,
        commit: gitInfo.commit,
        author: gitInfo.author,
        timestamp: getTimestamp(),
        workspace: env.WORKSPACE
    ]
    
    echo "📋 建置資訊:"
    buildInfo.each { key, value ->
        echo "  ${key}: ${value}"
    }
    
    return buildInfo
} 