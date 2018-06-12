podTemplate(label: 'maven', containers: [
  containerTemplate(name: 'maven', image: 'maven:3.3.9-jdk-8-alpine', ttyEnabled: true, command: 'cat')
  ]) {

  node('maven') {
    stage('Build a Maven project') {
      git 'https://github.com/hashmapinc/Tempus.git'
      container('maven') {
          sh 'mvn clean install'
      }
    }
  }
}
