FROM maven:3.9-eclipse-temurin-17-alpine
EXPOSE 8091

VOLUME /root/.m2


WORKDIR /beacon-input
COPY . .

WORKDIR /beacon-input/target/beacon-libs
RUN mvn clean install -DskipTests


WORKDIR /beacon-input
RUN mvn clean install -DskipTests
CMD mvn spring-boot:run -DskipTests