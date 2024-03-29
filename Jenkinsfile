pipeline{
    agent {
        kubernetes {
            yaml '''
              apiVersion: v1
              kind: Pod
              metadata:
                name: qlack15
                namespace: jenkins
              spec:
                affinity:
                        podAntiAffinity:
                          preferredDuringSchedulingIgnoredDuringExecution:
                          - weight: 50
                            podAffinityTerm:
                              labelSelector:
                                matchExpressions:
                                - key: jenkins/jenkins-jenkins-agent
                                  operator: In
                                  values:
                                  - "true"
                              topologyKey: kubernetes.io/hostname
                securityContext:
                    runAsUser: 0
                    runAsGroup: 0
                containers:
                - name: qlack15-builder
                  image: eddevopsd2/maven-java-npm-docker:mvn3.6.3-jdk8-npm6.14.4-docker
                  volumeMounts:
                  - name: maven
                    mountPath: /root/.m2/
                    subPath: qlack1.5
                  tty: true
                  securityContext:
                    privileged: true
                    runAsUser: 0
                    fsGroup: 0
                imagePullSecrets:
                - name: regcred
                volumes:
                - name: maven
                  persistentVolumeClaim:
                    claimName: maven-nfs-pvc
            '''
            workspaceVolume persistentVolumeClaimWorkspaceVolume(claimName: 'workspace-nfs-pvc', readOnly: false)
        }
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages{
    	stage('Build') {
            steps {
                container (name: 'qlack15-builder'){
                    sh 'mvn -f Qlack2/pom.xml clean install'
                    sh 'mvn -f Qlack2/pom.xml clean install -Dliquibase.driver.groupId=mysql -Dliquibase.driver.artifactId=mysql-connector-java -Dliquibase.driver.version=5.1.29 -Dcontainer=jboss'
                }
            }
        }
        stage('Sonar Analysis') {
            steps {
                container (name: 'qlack15-builder'){
                    withSonarQubeEnv('sonar'){
                        sh 'update-alternatives --set java /usr/lib/jvm/java-11-openjdk-amd64/bin/java'
                        sh 'mvn -f Qlack2/pom.xml sonar:sonar -Dsonar.projectName=QLACK1.5 -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.token=${SONAR_GLOBAL_KEY} -Dsonar.working.directory="/tmp"'
                    }
                }
            }
        }
        stage('Produce bom.xml'){
            steps{
                container (name: 'qlack15-builder'){
                    sh 'mvn -f Qlack2/pom.xml org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom'
                }
            }
        }
        stage('Dependency-Track Analysis'){
            steps{
                container (name: 'qlack15-builder'){
                    sh '''
                        cat > payload.json <<__HERE__
                        {
                            "project": "a6c6ff21-585c-4d8c-9fef-b90b5c998026",
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
    }
    post {
         changed {
                emailext subject: '$DEFAULT_SUBJECT',
                            body: '$DEFAULT_CONTENT',
                            to: 'qlack@eurodyn.com'
         }
    }
}