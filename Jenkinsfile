pipeline {
    agent any
    stages {
        stage('Detect Changed Folder & Trigger Child Pipelines') {
            steps {
                script {
                    // 取得本次變動的檔案清單（跟前一次 commit 差異）
                    def changeFiles = sh(
                        script: "git diff --name-only HEAD~1 HEAD",
                        returnStdout: true
                    ).trim().split('\n')

                    echo "Changed files:\n${changeFiles.join('\n')}"

                    // 定義子專案目錄對應的 Jenkins job 名稱
                    def jobsMap = [
                        'net-example'    : 'net-pipeline',
                        'python-example' : 'python-pipeline',
                        // 以後可以直接在這裡新增更多子專案與 pipeline 名稱
                    ]

                    // 找出被改動的子專案
                    def changedJobs = jobsMap.findAll { folder, jobName ->
                        changeFiles.any { it.startsWith("${folder}/") }
                    }.collect { it.value }

                    if (changedJobs.isEmpty()) {
                        echo "No relevant changes detected, skip triggering any child pipelines."
                    } else {
                        echo "Detected changes in: ${changedJobs.join(', ')}"
                        changedJobs.each { job ->
                            echo "Triggering child pipeline: ${job}"
                            // wait: false 可改成 true，視需求決定是否等待該 pipeline 結束
                            build job: job, wait: false
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            echo "Finished detecting changes and triggering child pipelines."
        }
    }
}