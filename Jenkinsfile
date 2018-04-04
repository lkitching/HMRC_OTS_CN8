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
    stage('table2qb') {
      steps {
        sh "java -jar lib/table2qb-0.1.1-SNAPSHOT-standalone.jar build.clj"
      }
    }
    stage('CSV2RDF') {
      agent {
        docker {
          image 'cloudfluff/rdf-tabular'
          reuseNode true
        }
      }
      steps {
        script {
          for (table in ["components", "countries", "component-specifications", "dataset",
                         "data-structure-definition", "observations", "used-codes-codelists",
                         "used-codes-codes"]) {
            sh "rdf serialize --input-format tabular --output-format ntriples out/${table}.json > out/${table}.nt"
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