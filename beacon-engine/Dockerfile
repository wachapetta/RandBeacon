FROM eclipse-temurin:17.0.8.1_1-jre-alpine
EXPOSE 8081

RUN addgroup beacon; adduser  --ingroup beacon --disabled-password beacon
USER beacon

WORKDIR /beacon
COPY ./target/beacon-engine.jar .

ADD docker-files/beacon.cer docker-files/beacon.cer
ADD docker-files/beacon-priv.pem docker-files/beacon-priv.pem
ENTRYPOINT ["java","-jar","beacon-engine.jar"]