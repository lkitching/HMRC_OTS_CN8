pipeline {
    agent {
        label 'master'
    }
    stages {
        stage('Clean') {
            steps {
                sh 'rm -rf out'
            }
        }
        stage('Prepare source') {
            agent {
                docker {
                    image 'cloudfluff/databaker'
                    reuseNode true
                }
            }
            steps {
                sh 'jupyter-nbconvert --to python --stdout Prepare_sources.ipynb | python'
            }
        }
        stage('Upload draftset') {
            steps {
                script {
                    def csvs = []
                    for (def file : findFiles(glob: 'out/*.csv')) {
                        csvs.add("out/${file.name}")
                    }
                    uploadDraftset('HMRC Overseas Trade Statistics', csvs)
                }
            }
        }
        stage('Test Draftset') {
            steps {
                script {
                    configFileProvider([configFile(fileId: 'pmd', variable: 'configfile')]) {
                        def config = readJSON(text: readFile(file: configfile))
                        String PMD = config['pmd_api']
                        String credentials = config['credentials']
                        def drafts = drafter.listDraftsets(PMD, credentials, 'owned')
                        def jobDraft = drafts.find  { it['display-name'] == env.JOB_NAME }
                        if (jobDraft) {
                            withCredentials([usernameColonPassword(credentialsId: 'ons', variable: 'USERPASS')]) {
                                sh "java -cp lib/sparql.jar uk.org.floop.sparqlTestRunner.Run -i -s https://production-drafter-ons-alpha.publishmydata.com/v1/sparql/live -u \'${USERPASS}\'"
                            }
                        } else {
                            error "Expecting a draftset for this job."
                        }
                    }
                }
            }
        }
        stage('Publish') {
            steps {
                script {
                    publishDraftset()
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts 'out/**'
            junit 'reports/**/*.xml'
        }
    }
}
