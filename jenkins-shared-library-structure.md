# Jenkins 共享函式庫結構說明

## 📁 **正確的檔案結構**

```
Jenkins/
├── Jenkinsfile                    # 主流水線 (根目錄)
├── vars/                          # 共享函式庫 (根目錄)
│   ├── buildDotNet.groovy        # .NET 建置函式
│   ├── deployDocker.groovy       # Docker 部署函式
│   └── utils.groovy              # 通用工具函式
├── net-example/
│   ├── Jenkinsfile               # 原本的完整 Pipeline
│   ├── Jenkinsfile-modular       # 模組化版本 (無副檔名)
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── scripts/
│   │   ├── deploy.sh
│   │   ├── env-setup.sh
│   │   ├── health-check.sh
│   │   └── cleanup.sh
│   └── JenkinsDemo/
│       └── ...
├── python-example/
│   ├── Jenkinsfile
│   ├── app.py
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── requirements.txt
│   └── nginx.conf
└── README-shared-library.md
```

## 🚀 **使用方法**

### **1. 在 Jenkins 中設定共享函式庫**

1. 進入 Jenkins 管理 > 系統配置
2. 找到 "Global Pipeline Libraries"
3. 新增函式庫：
   - **Name**: `jenkins-shared-library`
   - **Source Code Management**: Git
   - **Project Repository**: 你的 Git 倉庫 URL
   - **Default Version**: `main` 或 `master`

### **2. 使用模組化 Jenkinsfile**

#### **選項 A：使用模組化版本**
```bash
# 將原本的 Jenkinsfile 備份
mv net-example/Jenkinsfile net-example/Jenkinsfile-original

# 使用模組化版本
mv net-example/Jenkinsfile-modular net-example/Jenkinsfile
```

#### **選項 B：保持兩個版本**
- `Jenkinsfile` - 完整版本
- `Jenkinsfile-modular` - 模組化版本

### **3. 測試共享函式庫**

```groovy
@Library('jenkins-shared-library') _

// 測試 .NET 建置
buildDotNet(
    solutionPath: 'JenkinsDemo/JenkinsDemo.sln',
    buildConfig: 'Release'
)

// 測試 Docker 部署
deployDocker(
    imageName: 'jenkins-demo',
    port: 8081
)
```

## ⚠️ **重要注意事項**

### **1. 檔案命名**
- Jenkinsfile **不要**加 `.groovy` 副檔名
- 共享函式庫檔案 **要**加 `.groovy` 副檔名

### **2. 路徑引用**
- 共享函式庫中的路徑要相對於專案根目錄
- 例如：`scripts/deploy.sh` 而不是 `net-example/scripts/deploy.sh`

### **3. 函式庫載入**
- 使用 `@Library('jenkins-shared-library') _` 載入
- 函式名稱要與檔案名稱一致

## 🔧 **故障排除**

### **問題 1：找不到共享函式庫**
```
ERROR: Could not find any definition of libraries [jenkins-shared-library]
```

**解決方案：**
1. 確認 Jenkins 中已正確設定共享函式庫
2. 確認函式庫名稱拼寫正確
3. 確認 Git 倉庫可訪問

### **問題 2：找不到函式**
```
ERROR: No such DSL method 'buildDotNet' found
```

**解決方案：**
1. 確認 `vars/buildDotNet.groovy` 檔案存在
2. 確認檔案中有 `def call(Map config = [:])` 方法
3. 確認函式庫已正確載入

### **問題 3：路徑錯誤**
```
ERROR: No such file or directory
```

**解決方案：**
1. 檢查相對路徑是否正確
2. 確認檔案確實存在
3. 檢查工作目錄設定

## 📝 **最佳實踐**

1. **版本控制**：將共享函式庫放在獨立的 Git 倉庫
2. **測試**：為共享函式庫編寫測試
3. **文件**：詳細記錄每個函式的參數和用法
4. **回退**：保留原始 Jenkinsfile 作為備份
5. **漸進式**：逐步遷移到模組化版本 