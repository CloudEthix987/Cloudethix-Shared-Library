def call(String imageName, String tag) {

    sh """
        podman build -t ${imageName}:${tag} .
    """
}
