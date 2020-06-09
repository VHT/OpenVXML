pipeline {
  agent any
  stages {
    stage('Maven Build') {
      steps {
        sh '''mvn clean verify
mkdir -p ${WORKSPACE}/OpenVXML/repository
cp -r com.vht.openvxml.releng/com.vht.openvxml.update/target/repository/ ${WORKSPACE}/OpenVXML/
cd ${WORKSPACE}
zip -r OpenVXML.zip OpenVXML/'''
      }
    }
  }
}