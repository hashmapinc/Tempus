pipeline {
  agent {
    node {
      label 'tempus'
    }

  }
  stages {
    stage('Initialize') {
      steps {
        sh '''echo PATH = ${PATH}
echo M2_HOME = ${M2_HOME}
mvn clean'''
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
    stage('Publish Image') {
      when {
        branch 'dev'
      }
      steps {
        sh '''cp $WORKSPACE/application/target/tempus.deb $WORKSPACE/docker/tb/tempus.deb
cp $WORKSPACE/application/target/tempus.deb $WORKSPACE/docker/cassandra-setup/tempus.deb'''
        withCredentials(bindings: [usernamePassword(credentialsId: 'docker_hub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          sh 'sudo docker login -u $USERNAME -p $PASSWORD'
        }

        sh '''sudo docker build $WORKSPACE/docker/tb/ -t hashmapinc/tempus:dev
sudo docker build $WORKSPACE/docker/cassandra-setup/ -t hashmapinc/cassandra-setup:dev'''
        sh '''sudo docker push hashmapinc/tempus:dev
sudo docker push hashmapinc/cassandra-setup:dev
'''
      }
    }
  }
}
