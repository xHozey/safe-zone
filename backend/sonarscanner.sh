#!/usr/bin/env bash

set -e

export JWT_SECRET="$(grep '^JWT_SECRET=' .env | cut -d= -f2-)"
export SERVER_SSL_KEY_STORE_PASSWORD="$(grep '^SERVER_SSL_KEY_STORE_PASSWORD=' .env | cut -d= -f2-)"
export SONAR_TOKEN="$(grep '^SONAR_TOKEN=' .env | cut -d= -f2-)"

for service in gateway userService productService mediaService server; do
  echo "Scanning $service..."
  cd "$service"
  ./mvnw clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
    -Dsonar.projectKey="buy-01-$service" \
    -Dsonar.projectName="buy-01-$service" \
    -Dsonar.host.url=http://localhost:9090 \
    -Dsonar.token="$SONAR_TOKEN"
  cd ..
done