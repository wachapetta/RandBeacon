FROM eclipse-temurin:17.0.8.1_1-jre-alpine
EXPOSE 8080

WORKDIR /beacon
COPY ./target/beacon-interface.jar .

ADD docker-files/beacon.cer docker-files/beacon.cer
ADD docker-files/beacon-priv.pem docker-files/beacon-priv.pem
ENTRYPOINT ["java","-jar","beacon-interface.jar"]