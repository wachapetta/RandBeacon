FROM eclipse-temurin:17.0.8.1_1-jre-alpine
EXPOSE 8091

RUN addgroup beacon; adduser  --ingroup beacon --disabled-password beacon
USER beacon

WORKDIR /beacon
COPY ./target/beacon-input.jar  .

ENTRYPOINT ["java","-jar","beacon-input.jar"]