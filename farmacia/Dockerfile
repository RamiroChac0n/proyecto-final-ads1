FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests


FROM quay.io/wildfly/wildfly:34.0.0.Final-jdk21

ARG POSTGRES_JDBC_VERSION=42.7.4
RUN curl -L -o /tmp/postgresql.jar \
    https://jdbc.postgresql.org/download/postgresql-${POSTGRES_JDBC_VERSION}.jar

COPY configure-datasource.cli /tmp/configure-datasource.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/configure-datasource.cli

COPY --from=builder /app/target/*.war /opt/jboss/wildfly/standalone/deployments/

EXPOSE 8080

RUN cat > /opt/jboss/wildfly/bin/docker-entrypoint.sh <<'SCRIPT'
#!/bin/bash
_CLI=$(mktemp)
cat > "${_CLI}" <<'EOF'
embed-server --server-config=standalone.xml
/subsystem=datasources/data-source=PostgresDS:write-attribute(name=connection-url,value="jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}")
/subsystem=datasources/data-source=PostgresDS:write-attribute(name=user-name,value="${DB_USER}")
/subsystem=datasources/data-source=PostgresDS:write-attribute(name=password,value="${DB_PASS}")
stop-embedded-server
EOF
/opt/jboss/wildfly/bin/jboss-cli.sh --file="${_CLI}"
rm -f "${_CLI}"
exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0
SCRIPT
RUN chmod +x /opt/jboss/wildfly/bin/docker-entrypoint.sh

CMD ["/opt/jboss/wildfly/bin/docker-entrypoint.sh"]
