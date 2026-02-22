FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests


FROM quay.io/wildfly/wildfly:latest

ARG POSTGRES_JDBC_VERSION=42.7.4
RUN curl -L -o /tmp/postgresql.jar \
    https://jdbc.postgresql.org/download/postgresql-${POSTGRES_JDBC_VERSION}.jar

COPY configure-datasource.cli /tmp/configure-datasource.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/configure-datasource.cli

COPY --from=builder /app/target/*.war /opt/jboss/wildfly/standalone/deployments/

EXPOSE 8080

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
