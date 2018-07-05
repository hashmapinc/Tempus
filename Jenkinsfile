pipeline {
  agent {
    node {
      label 'tempus'
    }

  }
  stages {
    stage('Initialize') {
      parallel {
        stage('Initialize') {
          steps {
            sh '''echo PATH = ${PATH}
echo M2_HOME = ${M2_HOME}
mvn clean'''
          }
        }
        stage('docker login') {
          steps {
            sh '''node(\'<MY_UNIX_SLAVE>\') {
withCredentials([[$class: \'UsernamePasswordMultiBinding\', credentialsId: \'<CREDENTIAL_ID>\',
usernameVariable: \'USERNAME\', passwordVariable: \'PASSWORD\']]) {




sh \'echo uname=$USERNAME pwd=$PASSWORD\'
 }
}'''
            }
          }
        }
      }
      stage('Build') {
        steps {
          sh 'mvn validate'
          sh 'mvn -Dmaven.test.failure.ignore=true install'
        }
      }
      stage('Report and Archive') {
        steps {
          junit '**/target/surefire-reports/**/*.xml'
          archiveArtifacts 'application/target/*.jar,application/target/*.deb,application/target/*.zip,application/target/*.rpm'
        }
      }
    }
  }