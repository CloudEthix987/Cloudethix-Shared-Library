

def call() {
    withCredentials([
        string(
            credentialsId: 'Docker-hub-id',
            variable: 'dckr_pat_q8X6bU_es_n8AqC2ivAOXsaLAjw'
        )
    ]) {
        sh '''
            echo "$DOCKER_PASSWORD" | docker login docker.io \
                -u "payalkharat" \
                --password-stdin
        '''
    }
}
