FROM adoptopenjdk/openjdk11:jdk-11.0.5_10-alpine as builder
# Set environment variables.
ENV HOME /root

ADD docker /app/
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# build the project
RUN chmod +x ./mvnw
RUN ./mvnw install -DskipTests
#ENTRYPOINT ["/bin/sh"]


FROM ubuntu:22.04
MAINTAINER EGOR BABCINETCHI
# Update OS and install required packages
RUN apt-get update \
    &&  apt-get install -y \
        openjdk-11-jre-headless \
    &&  apt-get clean

COPY --from=builder /app/target/pr-lab1-consumer-*.jar pr-lab1-consumer.jar

ENTRYPOINT exec java $JAVA_OPTS -jar ./pr-lab1-consumer.jar
