pipeline {
  agent {
      label 'master'
  }
  stages {
    stage('Fetch source') {
      agent {
        docker {
          image 'cloudfluff/databaker'
          reuseNode true
        }
      }
      steps {
        sh 'jupyter-nbconvert --to python --stdout Fetch_sources.ipynb | python'
      }
    }
    stage('table2qb') {
      steps {
        sh "java -jar lib/table2qb-0.1.0-SNAPSHOT-standalone.jar build.clj"
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
        sh 'cp metadata/*.json out/'
        sh 'rdf serialize --input-format tabular --output-format ttl in/CN8_Non-EU_cod_2012.csv > out/CN8_Non-EU_cod_2012.ttl'
      }
    }
    stage('Test') {
      steps {
        sh 'java -cp bin/sparql uk.org.floop.sparqlTestRunner.Run -i -t tests/qb -r reports/TESTS-qb.xml out/CN8_Non-EU_cod_2012.ttl'
      }
    }
  }
  post {
    always {
      archiveArtifacts 'out/*'
    }
  }
}