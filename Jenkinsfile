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
                    def PMD = 'https://production-drafter-ons-alpha.publishmydata.com'
                    def credentials = 'onspmd'
                    def drafts = drafter.listDraftsets baseUrl: PMD, credentials: credentials, include: 'owned'
                    def jobDraft = drafts.find  { it['display-name'] == env.JOB_NAME }
                    if (jobDraft) {
                        drafter.deleteDraftset baseUrl: PMD, credentials: credentials, id: jobDraft.id
                    }
                    def newJobDraft = drafter.createDraftset baseUrl: PMD, credentials: credentials, label: env.JOB_NAME
                    echo "Finish me"
                }
            }
        }
        stage('Test Draftset') {
            steps {
                echo 'Placeholder for acceptance tests from e.g. GDP-205'
            }
        }
        stage('Publish') {
            steps {
                echo "Placeholder"
            }
        }
    }
    post {
        always {
            archiveArtifacts 'out/**'
            archiveArtifacts 'metadata/*'
        }
    }
}
