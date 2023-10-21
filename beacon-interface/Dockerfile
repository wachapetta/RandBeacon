FROM maven:3.9-eclipse-temurin-17-alpine
EXPOSE 8080

WORKDIR /beacon-interface
COPY . .

WORKDIR /beacon-interface/target/beacon-libs
RUN mvn clean install package -DskipTests

WORKDIR /beacon-interface
RUN mvn clean install package -DskipTests

WORKDIR /beacon-interface/target
ADD docker-files/beacon.cer docker-files/beacon.cer
ADD docker-files/beacon-priv.pem docker-files/beacon-priv.pem
ENTRYPOINT ["java","-jar","beacon-interface.jar"]