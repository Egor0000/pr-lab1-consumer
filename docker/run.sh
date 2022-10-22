#!/usr/bin/env bash

export LAB1_CONSUMER_HOME=/app/pr-lab1-consumer
export LAB1_CONSUMER_VERSION=pr-lab1-consumer:latest
export LAB1_CONSUMER_PROFILE=default
export PORT=8085
export PRODUCER_PORT=8075
export APP_NAME=consumer-dev
export ADDRESS=localhost
export ORDERING_ADDRESS=localhost
export ORDERING_PORT=8002
export RESTAURANT_ID=0

function noArgumentSupplied() {

    echo ""
    echo "========================================================================="
    echo ""

    exit 1
}

args=("$@")

echo "========================================================================="
echo ""
echo "  LAB1_CONSUMER Docker Environment"
echo ""
echo "  Number of arguments: $#"

if [[ $# -eq 0 ]] ; then
    echo '  No arguments supplied'
    noArgumentSupplied
    exit 1
fi

if [[ -z ${args[0]} ]] ; then
    echo '  no example instance name supplied'
    noArgumentSupplied
    exit 1
fi

export LAB1_CONSUMER_INSTANCE_NAME=${args[0]}

echo "  LAB1_CONSUMER instance name: ${LAB1_CONSUMER_INSTANCE_NAME}"
echo "  LAB1_CONSUMER profile: ${LAB1_CONSUMER_PROFILE}"
echo "  Restaurant ID: ${RESTAURANT_ID}"
echo ""
echo "========================================================================="
echo ""

# -e JAVA_OPTS="${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:8787" \

docker run -it --name ${LAB1_CONSUMER_INSTANCE_NAME} --net host --log-driver none \
-e SPRING_PROFILES_ACTIVE=${LAB1_CONSUMER_PROFILE} \
-e ADDRESS=${ADDRESS} \
-e PORT=${PORT} \
-e ORDERING_ADDRESS=${ORDERING_ADDRESS} \
-e ORDERING_PORT=${ORDERING_PORT} \
-e PRODUCER_PORT=${PRODUCER_PORT} \
-e RESTAURANT_ID=${RESTAURANT_ID} \
-e APP_NAME=${APP_NAME} \
-e JAVA_OPTS="${JAVA_OPTS}" \
--rm ${LAB1_CONSUMER_VERSION}