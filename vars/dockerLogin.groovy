

def call() {
    withCredentials([
        usernamePassword(
            credentialsId: 'Docker-hub-id',
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASSWORD'
        )
    ]) {
        sh '''
            echo "$DOCKER_PASSWORD" | docker login docker.io \
                -u "$DOCKER_USER" \
                --password-stdin
        '''
    }
}
