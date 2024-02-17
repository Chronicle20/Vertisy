FROM maven:3.9.6-amazoncorretto-21-debian AS jar

# Build in a separated location which won't have permissions issues.
WORKDIR /opt/dir
# Any changes to the pom will affect the entire build, so it should be copied first.
COPY pom.xml ./pom.xml
# Grab all the dependencies listed in the pom early, since it prevents changes to source code from requiring a complete re-download.
# Skip compiling tests since we don't want all the dependecies to be downloaded.
RUN mvn -f ./pom.xml clean dependency:go-offline -Dmaven.test.skip -T 1C
# Source code changes may not change dependencies, so it can go last.
# Skip compiling tests since we don't want all the dependecies to be downloaded for plugins.
COPY src ./src
RUN mvn -f ./pom.xml clean package -Dmaven.test.skip -T 1C

FROM amazoncorretto:21.0.1-alpine3.18

RUN apk add --no-cache bash

COPY --from=ghcr.io/ufoscout/docker-compose-wait:latest /wait /wait

# Host the server in a location that won't have permissions issues.
WORKDIR /opt/dir

COPY /scripts ./scripts/
COPY /wz ./wz/
COPY configuration.ini ./
COPY run.sh ./
COPY recvops-90.properties ./
COPY recvops-92.properties ./
COPY sendops-90.properties ./
COPY sendops-92.properties ./
COPY vertisykey.jks ./
COPY vertisykey3.jks ./

# Copy the JAR we build earlier.
COPY --from=jar /opt/dir/target/Vertisy.jar ./dist/Vertisy.jar

EXPOSE 9494 7575 7576 7577 7578
CMD /wait && /opt/dir/run.sh
