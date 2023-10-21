FROM maven:3.9-eclipse-temurin-17-alpine
EXPOSE 8091

WORKDIR /beacon-input
COPY . .

WORKDIR /beacon-input/target/beacon-libs
RUN mvn clean install package -DskipTests

WORKDIR /beacon-input
RUN mvn clean install package -DskipTests

WORKDIR /beacon-input/target
ENTRYPOINT ["java","-jar","beacon-input.jar"]