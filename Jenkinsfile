pipeline {
  agent {
    docker {
      image 'hashmapinc/tempusbuild:java-11'
      args '-u root -v /var/run/docker.sock:/var/run/docker.sock'
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
        slackSend(message: 'Build Started for Branch: '+env.BRANCH_NAME+' for: '+env.CHANGE_AUTHOR+' on: '+env.BUILD_TAG, color: 'Green', channel: 'tempusbuild', botUser: true)
      }
    }
    stage('Build') {
      steps {
        sh 'mvn -Dmaven.test.failure.ignore=true -DskipITs org.jacoco:jacoco-maven-plugin:prepare-agent install'
      }
    }
    stage('Integration Tests') {
      steps {
        sh 'mvn failsafe:integration-test'
        sh 'mvn failsafe:verify'
      }
    }
    stage('SonarQube analysis') {
        steps {
            withSonarQubeEnv('SonarCloud') {
                sh 'mvn -Dsonar.organization=hashmapinc-github -Dsonar.branch.name=$BRANCH_NAME sonar:sonar'
            }
        }
    }
    stage('Report and Archive') {
      steps {
        junit '**/target/surefire-reports/**/*.xml,**/target/failsafe-reports/**/*.xml'
        archiveArtifacts 'application/target/*.jar,application/target/*.deb,application/target/*.zip,application/target/*.rpm'
      }
    }
    stage('Deploy Artifacts') {
      steps {
        configFileProvider([configFile(fileId: 'global-maven-config', variable: 'MAVEN_SETTINGS_XML')]) {
        sh 'mvn -s $MAVEN_SETTINGS_XML deploy:deploy'
        }
      }      
    }
    stage('Publish Image') {
      when {
        branch 'dev'
      }
      steps {
        sh '''cp $WORKSPACE/application/target/tempus.deb $WORKSPACE/docker/tb/tempus.deb
cp $WORKSPACE/application/target/tempus.deb $WORKSPACE/docker/database-setup/tempus.deb'''
        withCredentials(bindings: [usernamePassword(credentialsId: 'docker_hub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          sh 'sudo docker login -u $USERNAME -p $PASSWORD'
        }

        sh '''sudo docker build $WORKSPACE/docker/tb/ -t hashmapinc/tempus:dev
sudo docker build $WORKSPACE/docker/database-setup/ -t hashmapinc/database-setup:dev'''
        sh '''sudo docker push hashmapinc/tempus:dev
sudo docker push hashmapinc/database-setup:dev
'''
      }
    }
    stage('Success Message') {
      steps {
        slackSend(message: 'Build Completed for Branch: '+env.BRANCH_NAME+' for: '+env.CHANGE_AUTHOR+' on: '+env.BUILD_TAG, channel: 'tempusbuild', color: 'Green')
      }
    }
  }
  post {
    always {
      sh 'chmod -R 777 .'

    }

  }
}