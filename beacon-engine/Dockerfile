FROM maven:3.9-eclipse-temurin-17-alpine
EXPOSE 8081

VOLUME /root/.m2

WORKDIR /beacon-engine
COPY . .

WORKDIR /beacon-engine/target/beacon-libs
RUN mvn clean install -DskipTests

WORKDIR /beacon-engine
RUN mvn clean install -DskipTests
CMD mvn spring-boot:run -DskipTests