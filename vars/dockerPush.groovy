def call(String imageName, String tag) {

    sh """
        podman push ${imageName}:${tag}
    """
}
