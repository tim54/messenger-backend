FROM eclipse-temurin:21-jdk-alpine
COPY target/messenger-app.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]