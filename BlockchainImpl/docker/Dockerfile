FROM openjdk:18-jdk-alpine
EXPOSE 30303
ARG JAR_FILE=BlockchainImpl-1.0-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]