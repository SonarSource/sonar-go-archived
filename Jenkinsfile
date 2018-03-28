@Library('SonarSource@1.6') _

pipeline {
    agent {
        label 'linux'
    }
    parameters {
        string(name: 'GIT_SHA1', description: 'Git SHA1 (provided by travisci hook job)')
        string(name: 'CI_BUILD_NAME', defaultValue: 'sonar-go', description: 'Build Name (provided by travisci hook job)')
        string(name: 'CI_BUILD_NUMBER', description: 'Build Number (provided by travisci hook job)')
        string(name: 'GITHUB_BRANCH', defaultValue: 'master', description: 'Git branch (provided by travisci hook job)')
        string(name: 'GITHUB_REPOSITORY_OWNER', defaultValue: 'SonarSource', description: 'Github repository owner(provided by travisci hook job)')
    }
    environment {
        SONARSOURCE_QA = 'true'
    }
    stages {
        stage('Notify') {
            steps {
                sendAllNotificationQaStarted()
            }
        }
        stage('QA') {
            parallel {
                stage('ruling') {
                    steps {
                        withQAEnv {
                            sh "ruling=true ./gradlew -DbuildNumber=${params.CI_BUILD_NUMBER} -Dsonar.runtimeVersion=LTS -Dorchestrator.artifactory.repositories=sonarsource-qa " +
                                    "-Dorchestrator.artifactory" +
                                    ".apiKey=${env.ARTIFACTORY_PRIVATE_API_KEY}  --console plain --no-daemon --info :its:ruling:check"
                        }
                    }
                }
                stage('plugin') {
                    steps {
                        withQAEnv {
                            sh "./gradlew -DbuildNumber=${params.CI_BUILD_NUMBER} -Dsonar.runtimeVersion=LTS -Dorchestrator.artifactory.repositories=sonarsource-qa " +
                                    "-Dorchestrator.artifactory" +
                                    ".apiKey=${env.ARTIFACTORY_PRIVATE_API_KEY}  --console plain --no-daemon --info integrationTest"
                        }
                    }
                }
            }
            post {
                always {
                    sendAllNotificationQaResult()
                }
            }
        }
        stage('Promote') {
            steps {
                repoxPromoteBuild()
            }
            post {
                always {
                    sendAllNotificationPromote()
                }
            }
        }
    }
}

def withQAEnv(def body) {
    checkout scm
    withCredentials([string(credentialsId: 'ARTIFACTORY_PRIVATE_API_KEY', variable: 'ARTIFACTORY_PRIVATE_API_KEY'),
                     usernamePassword(credentialsId: 'ARTIFACTORY_PRIVATE_USER', passwordVariable: 'ARTIFACTORY_PRIVATE_PASSWORD', usernameVariable: 'ARTIFACTORY_PRIVATE_USERNAME')]) {
        wrap([$class: 'Xvfb']) {
            body.call()
        }
    }
}
