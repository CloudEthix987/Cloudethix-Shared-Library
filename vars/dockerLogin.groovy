def call() {

    withCredentials([
        usernamePassword(
            credentialsId: 'dockerhub-credentials',
            usernameVariable: 'payalkharat',
            passwordVariable: 'dckr_pat_q8X6bU_es_n8AqC2ivAOXsaLAjw'
        )
    ]) {

        sh '''
            echo "$DOCKER_PASSWORD" | podman login docker.io \
                -u "$DOCKER_USER" \
                --password-stdin
        '''
    }
}
