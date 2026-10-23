FROM eclipse-temurin:25-jre
COPY target/Money-Manager.jar M-Manager.jar
CMD ["java", "-jar", "M-Manager.jar"]