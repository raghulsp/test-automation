pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    parameters {
        choice(
            name: 'TEST_SUITE',
            choices: ['api-smoke', 'api-regression', 'api', 'ui-smoke', 'ui-regression', 'ui'],
            description: 'Maven profile / TestNG suite to execute'
        )
        string(name: 'API_ENV', defaultValue: 'qa', description: 'API environment: dev, qa, or staging')
        string(name: 'THREAD_COUNT', defaultValue: '1', description: 'TestNG test-level parallel thread count')
        string(name: 'DATA_PROVIDER_THREAD_COUNT', defaultValue: '4', description: 'Cucumber scenario data provider thread count')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Run supported browsers in headless mode')
    }

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                bat """
                    mvn -B clean verify ^
                      -P%TEST_SUITE% ^
                      -Dapi.env=%API_ENV% ^
                      -Dheadless=%HEADLESS% ^
                      -Dthread.count=%THREAD_COUNT% ^
                      -Ddata.provider.thread.count=%DATA_PROVIDER_THREAD_COUNT%
                """
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'target/cucumber-reports/**, target/masterthought-reports/**, target/surefire-reports/**'
        }
    }
}
