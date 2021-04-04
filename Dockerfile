FROM openjdk:8u265
MAINTAINER bison

ARG JAR_FILE

ADD target/${JAR_FILE} /data-layer.jar

EXPOSE 50000

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions","-XX:+UseCGroupMemoryLimitForHeap", "-jar","/data-layer.jar"]
