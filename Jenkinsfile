podTemplate(label: 'maven', containers: [
  containerTemplate(
        name: 'maven', 
        image: 'maven:3.3.9-jdk-8-alpine', 
        ttyEnabled: true, 
        command: 'cat', 
        resourceRequestCpu: '1',
        resourceLimitCpu: '2',
        resourceRequestMemory: '4G',
        resourceLimitMemory: '6G',)
  ]) {

  node('maven') {
    stage('Build a Maven project') {
      git 'https://github.com/jenkinsci/kubernetes-plugin.git'
      container('maven') {
          sh 'mvn -B clean package'
      }
    }
  }
}
