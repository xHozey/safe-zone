FROM jenkins/jenkins:2.580.2-jdk21

USER root

COPY --from=docker:27-cli /usr/local/bin/docker /usr/local/bin/docker

RUN apt-get update \
	&& apt-get install -y --no-install-recommends \
		ca-certificates \
		curl \
		docker-compose \
	&& apt-get clean \
	&& rm -rf /var/lib/apt/lists/* \
	&& command -v docker \
	&& docker --version

USER jenkins

COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt

RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt
