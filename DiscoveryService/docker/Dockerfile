FROM openjdk:18-jdk-alpine
EXPOSE 8761
ARG JAR_FILE=DiscoveryService-1.0-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]