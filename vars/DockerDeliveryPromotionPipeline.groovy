def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    pipeline {

        agent any

        environment {

            // Docker Hub registry
            registryURL = 'https://registry.hub.docker.com'

            // Docker Hub repositories
            dev_registry   = 'payalkharat/cloudethix-sample-nginx-dev'
            qa_registry    = 'payalkharat/cloudethix-sample-nginx-qa'
            stage_registry = 'payalkharat/cloudethix-sample-nginx-stage'
            prod_registry  = 'payalkharat/cloudethix-sample-nginx-prod'

            // Jenkins credentials
            dev_dh_creds   = 'dh_cred_dev'
            qa_dh_creds    = 'dh_cred_qa'
            stage_dh_creds = 'dh_cred_stage'
            prod_dh_creds  = 'dh_cred_prod'
        }

        parameters {

            choice(
                name: 'account',
                choices: ['dev', 'qa', 'stage', 'prod'],
                description: 'Select the environment.'
            )
        }

        stages {

            /*
             * ============================================================
             * DEV
             * Build image and push it to DEV repository
             * ============================================================
             */

            stage('Building the Docker Image in Dev') {

                when {
                    expression {
                        params.account == 'dev'
                    }
                }

                environment {

                    dev_image =
                        "${env.dev_registry}:${GIT_COMMIT}"
                }

                steps {

                    script {

                        docker.withRegistry(
                            env.registryURL,
                            env.dev_dh_creds
                        ) {

                            def image = docker.build(
                                env.dev_image,
                                '.'
                            )

                            image.push()
                        }
                    }
                }

                post {

                    always {

                        sh """
                            docker rmi ${env.dev_image} || true
                        """
                    }
                }
            }


            /*
             * ============================================================
             * QA
             * Pull DEV image
             * Tag it as QA
             * Push QA image
             * ============================================================
             */

            stage('Push the Docker Image in QA') {

                when {
                    expression {
                        params.account == 'qa'
                    }
                }

                environment {

                    dev_image =
                        "${env.dev_registry}:${GIT_COMMIT}"

                    qa_image =
                        "${env.qa_registry}:${GIT_COMMIT}"
                }

                steps {

                    script {

                        /*
                         * Login to Docker Hub using DEV credentials
                         * and pull DEV image
                         */

                        docker.withRegistry(
                            env.registryURL,
                            env.dev_dh_creds
                        ) {

                            docker.image(
                                env.dev_image
                            ).pull()
                        }


                        /*
                         * Tag DEV image as QA image
                         *
                         * IMPORTANT:
                         * Both image names are exactly the same
                         * format used during pull.
                         */

                        sh """
                            docker tag \
                            ${env.dev_image} \
                            ${env.qa_image}
                        """


                        /*
                         * Login to Docker Hub using QA credentials
                         * and push QA image
                         */

                        docker.withRegistry(
                            env.registryURL,
                            env.qa_dh_creds
                        ) {

                            docker.image(
                                env.qa_image
                            ).push()
                        }
                    }
                }

                post {

                    always {

                        sh """
                            docker rmi ${env.dev_image} || true
                            docker rmi ${env.qa_image} || true
                        """
                    }
                }
            }


            /*
             * ============================================================
             * STAGE
             * Pull QA image
             * Tag it as STAGE
             * Push STAGE image
             * ============================================================
             */

            stage('Push the Docker Image in STAGE') {

                when {
                    expression {
                        params.account == 'stage'
                    }
                }

                environment {

                    qa_image =
                        "${env.qa_registry}:${GIT_COMMIT}"

                    stage_image =
                        "${env.stage_registry}:${GIT_COMMIT}"
                }

                steps {

                    script {

                        /*
                         * Login using QA credentials
                         * and pull QA image
                         */

                        docker.withRegistry(
                            env.registryURL,
                            env.qa_dh_creds
                        ) {

                            docker.image(
                                env.qa_image
                            ).pull()
                        }


                        /*
                         * Tag QA image as STAGE image
                         */

                        sh """
                            docker tag \
                            ${env.qa_image} \
                            ${env.stage_image}
                        """


                        /*
                         * Login using STAGE credentials
                         * and push STAGE image
                         */

                        docker.withRegistry(
                            env.registryURL,
                            env.stage_dh_creds
                        ) {

                            docker.image(
                                env.stage_image
                            ).push()
                        }
                    }
                }

                post {

                    always {

                        sh """
                            docker rmi ${env.qa_image} || true
                            docker rmi ${env.stage_image} || true
                        """
                    }
                }
            }


            /*
             * ============================================================
             * PROD
             * Pull STAGE image
             * Tag it as PROD
             * Push PROD image
             * ============================================================
             */

            stage('Push the Docker Image in PROD') {

                when {
                    expression {
                        params.account == 'prod'
                    }
                }

                environment {

                    stage_image =
                        "${env.stage_registry}:${GIT_COMMIT}"

                    prod_image =
                        "${env.prod_registry}:${GIT_COMMIT}"
                }

                steps {

                    script {

                        /*
                         * Login using STAGE credentials
                         * and pull STAGE image
                         */

                        docker.withRegistry(
                            env.registryURL,
                            env.stage_dh_creds
                        ) {

                            docker.image(
                                env.stage_image
                            ).pull()
                        }


                        /*
                         * Tag STAGE image as PROD image
                         */

                        sh """
                            docker tag \
                            ${env.stage_image} \
                            ${env.prod_image}
                        """


                        /*
                         * Login using PROD credentials
                         * and push PROD image
                         */

                        docker.withRegistry(
                            env.registryURL,
                            env.prod_dh_creds
                        ) {

                            docker.image(
                                env.prod_image
                            ).push()
                        }
                    }
                }

                post {

                    always {

                        sh """
                            docker rmi ${env.stage_image} || true
                            docker rmi ${env.prod_image} || true
                        """
                    }
                }
            }
        }
    }
}
