FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src

RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV NODE_ID=NodeA
ENV PORT=8081
ENV DATA_DIR=/app/data

EXPOSE 8081

ENTRYPOINT ["sh","-c","java -Dnode.id=${NODE_ID} -Dserver.port=${PORT} -Ddata.dir=${DATA_DIR} -jar app.jar"]