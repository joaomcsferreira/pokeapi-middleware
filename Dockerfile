FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-DEXTERNAL_URL='https://pokeapi.co/api/v2'","-DINTERNAL_URL='http://localhost:8080'" ,"-jar","/app.jar"]
EXPOSE 8080