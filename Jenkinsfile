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
            withCredentials([usernamePassword(credentialsId: 'docker_hub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                sh 'sudo docker login -u $USERNAME -p $PASSWORD'
              }
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
