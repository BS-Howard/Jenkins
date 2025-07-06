# Jenkins Shared Library

這是一個 Jenkins 共享函式庫，提供可重用的 Pipeline 函式。

## 📁 目錄結構

```
jenkins-shared-library/
├── vars/                    # 全域變數和函式
│   ├── buildDotNet.groovy   # .NET 建置函式
│   ├── deployDocker.groovy  # Docker 部署函式
│   └── utils.groovy         # 通用工具函式
├── src/                     # 源碼
│   └── com/
│       └── company/
│           └── jenkins/
│               ├── BuildUtils.groovy
│               └── DeployUtils.groovy
├── resources/               # 資源檔案
│   └── scripts/
└── README.md
```

## 🚀 使用方法

### 1. 在 Jenkins 中設定共享函式庫

1. 進入 Jenkins 管理 > 系統配置
2. 找到 "Global Pipeline Libraries"
3. 新增函式庫：
   - Name: `jenkins-shared-library`
   - Source Code Management: Git
   - Project Repository: `https://github.com/your-org/jenkins-shared-library.git`

### 2. 在 Jenkinsfile 中使用

```groovy
@Library('jenkins-shared-library') _

// 使用 .NET 建置函式
buildDotNet(
    solutionPath: 'MyApp/MyApp.sln',
    buildConfig: 'Release',
    skipTests: false
)

// 使用 Docker 部署函式
deployDocker(
    imageName: 'my-app',
    containerName: 'my-app-container',
    port: 8080
)
```

## 📋 可用函式

### buildDotNet(Map config)

建置 .NET 應用程式

**參數：**
- `solutionPath`: 解決方案檔案路徑 (預設: 'JenkinsDemo/JenkinsDemo.sln')
- `projectPath`: 專案檔案路徑 (預設: 'JenkinsDemo/JenkinsDemo/JenkinsDemo.csproj')
- `buildConfig`: 建置配置 (預設: 'Release')
- `outputPath`: 輸出路徑 (預設: 'publish')
- `sonarQube`: SonarQube 實例名稱 (預設: 'MySonar')
- `skipTests`: 是否跳過測試 (預設: false)
- `timeout`: 建置超時時間 (預設: 10 分鐘)

### deployDocker(Map config)

部署 Docker 容器

**參數：**
- `imageName`: Docker 映像名稱 (預設: 'jenkins-demo')
- `containerName`: 容器名稱 (預設: 'jenkins-demo')
- `port`: 容器端口 (預設: 8081)
- `hostPort`: 主機端口 (預設: 8081)
- `healthCheckUrl`: 健康檢查 URL (預設: 'http://localhost:8081')
- `healthCheckTimeout`: 健康檢查超時時間 (預設: 30 秒)
- `deployScript`: 部署腳本路徑 (預設: 'scripts/deploy.sh')
- `healthCheckScript`: 健康檢查腳本路徑 (預設: 'scripts/health-check.sh')
- `cleanupScript`: 清理腳本路徑 (預設: 'scripts/cleanup.sh')

## 🔧 開發指南

### 新增新函式

1. 在 `vars/` 目錄下建立新的 `.groovy` 檔案
2. 實作 `call(Map config = [:])` 方法
3. 更新此 README 文件

### 測試函式

```groovy
@Library('jenkins-shared-library') _

// 測試新函式
yourNewFunction(
    param1: 'value1',
    param2: 'value2'
)
```

## 📝 注意事項

- 所有函式都支援預設參數
- 函式會自動處理錯誤和通知
- 支援 Slack 整合
- 包含自動清理機制 