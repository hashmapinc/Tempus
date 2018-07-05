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
                // available as an env variable, but will be masked if you try to print it out any which way
                // note: single quotes prevent Groovy interpolation; expansion is by Bourne Shell, which is what you want
                sh 'echo $PASSWORD'
                // also available as a Groovy variable
                echo USERNAME
                // or inside double quotes for string interpolation
                echo "username is $USERNAME"
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
