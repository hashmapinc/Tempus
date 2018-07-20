pipeline {
  agent {
    docker {
      image 'hashmapinc/tempusbuild:latest'
      args '-u root -v /var/run/docker.sock:/var/run/docker.sock -v /home/ubuntu/.m2:/root/.m2 -v /home/ubuntu/.npm:/root/.npm -v /home/ubuntu/.gradle:/root/.gradle'
    }

  }
  stages {
    stage('Initialize') {
      steps {
        sh 'echo $USER'
        sh '''echo PATH = ${PATH}
echo M2_HOME = ${M2_HOME}
mvn clean
mvn validate'''
        slackSend(message: 'Started Build', color: 'Green', channel: 'Tempus', botUser: true)
      }
    }
    stage('Build') {
      steps {
        sh 'mvn -Dmaven.test.failure.ignore=true -DskipITs install'
      }
    }
    stage('Integration Tests') {
      steps {
        sh 'mvn failsafe:integration-test'
        sh 'mvn failsafe:verify'
      }
    }
    stage('Report and Archive') {
      steps {
        junit '**/target/surefire-reports/**/*.xml,**/target/failsafe-reports/**/*.xml'
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
    stage('Success Message') {
      steps {
        slackSend(message: 'Build Completed', channel: 'Tempus', color: 'Green')
      }
    }
  }
  post {
    always {
      sh 'chmod -R 777 .'

    }

  }
}