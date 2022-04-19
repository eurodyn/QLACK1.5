pipeline{
	agent {
        docker {
            image 'eddevopsd2/maven-java-npm-docker:mvn3.6.3-jdk8-npm6.14.4-docker'
            args '-v /root/.m2/QLACK1.5:/root/.m2'
        }
    }
     options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages{
    	stage('Build') {
            steps {
                sh 'mvn -f Qlack2/pom.xml clean install'
            }
        }
        stage('Sonar Analysis') {
            steps {
                sh 'update-alternatives --set java /usr/lib/jvm/java-11-openjdk-amd64/bin/java'
                sh 'mvn -f Qlack2/pom.xml sonar:sonar -Dsonar.projectName=QLACK1.5 -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_KEY_QLACK1_5}'
            }
        }
    }
    post {
         changed {
                emailext subject: '$DEFAULT_SUBJECT',
                            body: '$DEFAULT_CONTENT',
                            to: 'qlack@eurodyn.com'
         }

    }
}