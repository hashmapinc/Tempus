def notifySlack(String buildStatus = 'STARTED',String security = 'ANALYSING' ) {
    buildStatus = buildStatus ?: 'SUCCESS'

    def color

    if (buildStatus == 'STARTED') {
        color = '#D4DADF'
    } else if (buildStatus == 'SUCCESS') {
        color = '#BDFFC3'
    } else if (buildStatus == 'UNSTABLE') {
        color = '#FFFE89'
    } else {
        color = '#FF9FA1'
    }

    def msg = "${buildStatus}:`${env.JOB_NAME}` #${env.BUILD_NUMBER}\nSecurity: `${security}`\nUrl: ${RUN_DISPLAY_URL}\nChanges: ${RUN_CHANGES_DISPLAY_URL}"
    slackSend(color: color, message: msg, channel: 'tempusbuild', botUser: true)
}


pipeline {
    options {
      timeout(time: 2, unit: 'HOURS') 
  }
  agent {
    docker {
      image 'hashmapinc/tempusbuild:1056'
      args '-u root -v /var/run/docker.sock:/var/run/docker.sock'
    }
  }
  stages {
    stage('Code Scan') {
      steps {  
        notifySlack()
        script{        
            securitystatus = "BREACHED"
        }
        sh '''
          git secrets --register-aws
          git-secrets --scan
    '''
        }
    }    
    stage('Initialize') {
      steps {
        script{        
            securitystatus = "SECURE"
        }        
        sh 'echo $USER'
        sh '''echo PATH = ${PATH}
              echo M2_HOME = ${M2_HOME}
              mvn clean
              mvn validate'''
        slackSend(message: 'Build Started for Branch: '+env.BRANCH_NAME+' for: '+env.CHANGE_AUTHOR+' on: '+env.BUILD_TAG, color: 'Green', channel: 'tempusnotifications', botUser: true)
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
      when {
        branch 'dev'
      }      
      steps {
        configFileProvider([configFile(fileId: 'global-maven-config', variable: 'MAVEN_SETTINGS_XML')]) {
        sh 'mvn -s $MAVEN_SETTINGS_XML -Dmaven.test.failure.ignore=true -DskipITs -DskipTests deploy'
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
    stage('Publish Master Image') {
      when {
        branch 'master'
      }
      steps {
        withCredentials(bindings: [usernamePassword(credentialsId: 'docker_hub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          sh 'sudo docker login -u $USERNAME -p $PASSWORD'
        }
          sh '''cd ./docker/tb/
                make push'''
      }
    }    
  }
  post {
    always {
      sh 'chmod -R 777 .'
    }
    success {
          notifySlack(currentBuild.result,'SECURE')
      }
      failure {
          notifySlack(currentBuild.result,securitystatus)
      }
  }
}