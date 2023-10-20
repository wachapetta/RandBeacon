FROM maven:3.9-eclipse-temurin-17-alpine
EXPOSE 8080

VOLUME /root/.m2

WORKDIR /beacon-interface
COPY . .

WORKDIR /beacon-interface/target/beacon-libs
RUN mvn clean install -DskipTests

WORKDIR /beacon-interface
RUN mvn clean install -DskipTests
CMD mvn spring-boot:run -DskipTests