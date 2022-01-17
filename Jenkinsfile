pipeline{
	agent {
        docker {
            image 'imousmoutis/maven3-jdk8:1.0.1'
            args '-v /root/.m2/Qlack1.5:/root/.m2'
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
        stage('Sonar Analysis') {
            steps {
                withSonarQubeEnv('sonar'){
                    sh 'mvn -f Qlack2/pom.xml sonar:sonar -Dsonar.projectName=QLACK1.5 -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_KEY_QLACK1_5}'
                }
            }
        }
        stage('Produce bom.xml'){
            steps{
                sh 'mvn -f Qlack2/pom.xml org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom'
            }
        }
        stage('Dependency-Track Analysis'){
            steps{
                sh '''
                    cat > payload.json <<__HERE__
                    {
                        "project": "26777f12-1128-4601-a026-93c7c2ea5948",
                        "bom": "$(cat Qlack2/target/bom.xml |base64 -w 0 -)"
                    }
                    __HERE__
                   '''

                sh '''
                    curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                   '''
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