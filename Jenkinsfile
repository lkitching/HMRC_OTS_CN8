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
                    def drafts = drafter.listDraftsets(PMD, credentials, 'owned')
                    def jobDraft = drafts.find  { it['display-name'] == env.JOB_NAME }
                    if (jobDraft) {
                        drafter.deleteDraftset(PMD, credentials, jobDraft.id)
                    }
                    def newJobDraft = drafter.createDraftset(PMD, credentials, env.JOB_NAME)
                    String metadataGraph = "http://gss-data.org.uk/graph/hmrc-overseas-trade-statistics/metadata"
                    String encGraph = java.net.URLEncoder.encode(metadataGraph, "UTF-8")
                    drafter.deleteGraph(PMD, credentials, newJobDraft.id,
                                        "http://gss-data.org.uk/graph/hmrc-overseas-trade-statistics/metadata")
                    drafter.addData(PMD, credentials, newJobDraft.id,
                                    readFile("metadata/dataset.trig"), "application/trig")
                    def PIPELINE = 'http://production-grafter-ons-alpha.publishmydata.com/v1/pipelines'
                    runPipeline("${PIPELINE}/ons-table2qb.core/components/import",
                                newJobDraft.id, credentials, [[name: 'components-csv',
                                                                file: [name: 'metadata/components.csv', type: 'text/csv']]])
                    runPipeline("${PIPELINE}/ons-table2qb.core/codelist/import",
                                newJobDraft.id, credentials, [[name: 'codelist-csv',
                                                                file: [name: 'metadata/flow-directions.csv', type: 'text/csv']],
                                                              [name: 'codelist-name', value: 'Flow Directions']])
                    runPipeline("${PIPELINE}/ons-table2qb.core/codelist/import",
                                newJobDraft.id, credentials, [[name: 'codelist-csv',
                                                                file: [name: 'metadata/units.csv', type: 'text/csv']],
                                                              [name: 'codelist-name', value: 'Units']])
                    runPipeline("${PIPELINE}/ons-table2qb.core/codelist/import",
                                newJobDraft.id, credentials, [[name: 'codelist-csv',
                                                                file: [name: 'out/countries.csv', type: 'text/csv']],
                                                              [name: 'codelist-name', value: 'Countries']])
                    runPipeline("${PIPELINE}/ons-table2qb.core/data-cube/import",
                                newJobDraft.id, credentials, [[name: 'observations-csv',
                                                               file: [name: 'out/CN8_Non-EU_cod_2012-2016.csv', type: 'text/csv']],
                                                              [name: 'dataset-name', value: 'HMRC Overseas Trade Statistics']])
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
                script {
                    def PMD = 'https://production-drafter-ons-alpha.publishmydata.com'
                    def credentials = 'onspmd'
                    def drafts = drafter.listDraftsets(PMD, credentials, 'owned')
                    def jobDraft = drafts.find  { it['display-name'] == env.JOB_NAME }
                    if (jobDraft) {
                        drafter.publishDraftset(PMD, credentials, jobDraft.id)
                    } else {
                        error "Expecting a draftset for this job."
                    }
                }
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
