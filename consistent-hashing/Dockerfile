FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src

RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
ENV NODE_ADDRESSES=http://node1:8081,http://node2:8082,http://node3:8083

EXPOSE 8080

ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT} -DnodeAddresses=${NODE_ADDRESSES} -jar app.jar"]