FROM maven:3.9-eclipse-temurin-17-alpine
EXPOSE 8081

WORKDIR /beacon-engine
COPY . .

WORKDIR /beacon-engine/target/beacon-libs
RUN mvn clean install package -DskipTests

WORKDIR /beacon-engine
RUN mvn clean install package -DskipTests

WORKDIR /beacon-engine/target
ADD docker-files/beacon.cer docker-files/beacon.cer
ADD docker-files/beacon-priv.pem docker-files/beacon-priv.pem
ENTRYPOINT ["java","-jar","beacon-engine.jar"]