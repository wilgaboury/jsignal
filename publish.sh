#! /bin/bash

SCRIPT_DIR=$(realpath $(dirname "$0"))
cd ${SCRIPT_DIR}

./gradlew clean publish

source .env

TOKEN=$(echo "${MAVEN_CENTRAL_USERNAME}:${MAVEN_CENTRAL_PASSWORD}" | base64)

MODULES=("rx" "ui" "std")
for MODULE in "${MODULES[@]}"
do
  cd ${SCRIPT_DIR}/${MODULE}/build/repo
  zip -r ${MODULE}.zip org
  curl --request POST \
    --verbose \
    --header "Authorization: Bearer ${TOKEN}" \
    --form bundle=@${MODULE}.zip \
    https://central.sonatype.com/api/v1/publisher/upload
done