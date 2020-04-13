pipeline{
	agent {
        docker {
            image 'imousmoutis/maven3-jdk8:1.0.1'
            args '-v /root/.m2:/root/.m2'
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
                sh 'mvn -f Qlack2/pom.xml clean install -Dliquibase.driver.groupId=mysql -Dliquibase.driver.artifactId=mysql-connector-java -Dliquibase.driver.version=5.1.29 -Dcontainer=jboss'
            }
        }
        stage('Dependencies Check') {
            steps {
                sh 'mvn -f Qlack2/pom.xml org.owasp:dependency-check-maven:aggregate'
            }
        }
        stage('Sonar Analysis') {
            steps {
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