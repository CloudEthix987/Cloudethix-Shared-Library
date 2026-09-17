def call(String imageName, String tag) {

    sh """
        docker push ${imageName}:${tag}
    """
}
