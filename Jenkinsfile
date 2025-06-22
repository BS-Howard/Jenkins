pipeline {
    agent any
    parameters {
        choice(name: 'PROJECT', choices: ['net', 'python'], description: '選擇要跑哪個專案')
    }
    stages {
        stage('Trigger Child Pipeline') {
            steps {
                script {
                    if (params.PROJECT == 'net') {
                        build job: 'net-pipeline', wait: true
                    } else if (params.PROJECT == 'python') {
                        build job: 'python-pipeline', wait: true
                    } else {
                        error "未知的參數: ${params.PROJECT}"
                    }
                }
            }
        }
    }
    post {
        success {
            echo "子 Pipeline ${params.PROJECT} 執行成功！"
        }
        failure {
            echo "子 Pipeline ${params.PROJECT} 執行失敗！"
        }
    }
}
