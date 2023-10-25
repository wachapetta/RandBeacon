FROM eclipse-temurin:17.0.8.1_1-jre-alpine
EXPOSE 8091

WORKDIR /beacon
COPY ./target/beacon-input.jar  .

ENTRYPOINT ["java","-jar","beacon-input.jar"]