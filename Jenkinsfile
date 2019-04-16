@Library('SonarSource@2.1.1') _


pipeline {
  agent none
  parameters {
    string(name: 'GIT_SHA1', description: 'Git SHA1 (provided by travisci hook job)')
    string(name: 'CI_BUILD_NAME', defaultValue: 'sonar-go', description: 'Build Name (provided by travisci hook job)')
    string(name: 'CI_BUILD_NUMBER', description: 'Build Number (provided by travisci hook job)')
    string(name: 'GITHUB_BRANCH', defaultValue: 'master', description: 'Git branch (provided by travisci hook job)')
    string(name: 'GITHUB_REPOSITORY_OWNER', defaultValue: 'SonarSource', description: 'Github repository owner(provided by travisci hook job)')
  }
  stages {
    stage('Notify') {
      steps {
        sendAllNotificationQaStarted()
      }
    }
    stage('QA') {
      parallel {
        stage('ruling-lts') {
          agent {
            label 'linux'
          }
          steps {
            runRuling "LATEST_RELEASE[6.7]"
          }
        }
        stage('ruling-latest') {
          agent {
            label 'linux'
          }
          steps {
            runRuling "LATEST_RELEASE"
          }
        }
        stage('plugin-lts') {
          agent {
            label 'linux'
          }
          steps {
            runPlugin "LATEST_RELEASE[6.7]"
          }
        }
        stage('ci-windows') {
          agent {
            label 'windows'
          }
          steps {
            sh "./gradlew --no-daemon --console plain build"
          }
        }
        stage('plugin-lts-windows') {
          agent {
            label 'windows'
          }
          steps {
            runPlugin "LATEST_RELEASE[6.7]"
          }
        }
        stage('plugin-latest') {
          agent {
            label 'linux'
          }
          steps {
            runPlugin "LATEST_RELEASE"
          }
        }
        stage('plugin-dogfood') {
          agent {
            label 'linux'
          }
          steps {
            runPlugin "DOGFOOD"
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

def runRuling(String sqRuntimeVersion) {
  withQAEnv {
    sh "ruling=true ./gradlew -DbuildNumber=${params.CI_BUILD_NUMBER} -Dsonar.runtimeVersion=${sqRuntimeVersion} " +
        "-Dorchestrator.artifactory.apiKey=${env.ARTIFACTORY_API_KEY}  --console plain --no-daemon --info :its:ruling:check"
  }
}

def runPlugin(String sqRuntimeVersion) {
  withQAEnv {
    sh "./gradlew -DbuildNumber=${params.CI_BUILD_NUMBER} -Dsonar.runtimeVersion=${sqRuntimeVersion} " +
        "-Dorchestrator.artifactory.apiKey=${env.ARTIFACTORY_API_KEY}  --console plain --no-daemon --info integrationTest"
  }
}

def withQAEnv(def body) {
  checkout scm
  withCredentials([string(credentialsId: 'ARTIFACTORY_PRIVATE_API_KEY', variable: 'ARTIFACTORY_API_KEY'),
                   usernamePassword(credentialsId: 'ARTIFACTORY_PRIVATE_USER', passwordVariable: 'ARTIFACTORY_PRIVATE_PASSWORD', usernameVariable: 'ARTIFACTORY_PRIVATE_USERNAME')]) {
    def jdk = tool name: 'Java 11', type: 'jdk'
    withEnv(["JAVA_HOME=${jdk}"]) {
      wrap([$class: 'Xvfb']) {
        body.call()
      }
    }
  }
}
