FROM eclipse-temurin:17.0.8.1_1-jre-alpine
EXPOSE 8080

RUN addgroup beacon; adduser  --ingroup beacon --disabled-password beacon
USER beacon

WORKDIR /beacon
COPY ./target/beacon-interface.jar .

ADD docker-files/beacon.cer docker-files/beacon.cer
ADD docker-files/beacon-priv.pem docker-files/beacon-priv.pem

COPY docker-files/beacon.cer certificate/04c5dc3b40d25294c55f9bc2496fd4fe9340c1308cd073900014e6c214933c7f7737227fc5e4527298b9e95a67ad92e0310b37a77557a10518ced0ce1743e132

ENV BEACON_X509_FOLDER=/beacon/certificate

ENTRYPOINT ["java","-jar","beacon-interface.jar"]