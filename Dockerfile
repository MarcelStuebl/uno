FROM eclipse-temurin:21-jre
WORKDIR /app

# Fat-JAR ins Image
COPY target/server-all.jar /app/server.jar

EXPOSE 59362
ENTRYPOINT ["java","-jar","/app/server.jar"]

