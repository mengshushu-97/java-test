FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace
RUN mkdir -p /root/.m2 \
    && printf '%s\n' \
      '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">' \
      '  <mirrors>' \
      '    <mirror>' \
      '      <id>aliyunmaven</id>' \
      '      <mirrorOf>*</mirrorOf>' \
      '      <name>Aliyun Maven</name>' \
      '      <url>https://maven.aliyun.com/repository/public</url>' \
      '    </mirror>' \
      '  </mirrors>' \
      '</settings>' \
      > /root/.m2/settings.xml
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre

ARG OTEL_JAVA_AGENT_VERSION=2.16.0

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /otel \
    && curl -fsSL -o /otel/opentelemetry-javaagent.jar \
       "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar"

WORKDIR /app
COPY --from=build /workspace/target/java-test-*.jar /app/app.jar
COPY scripts/start.sh /app/scripts/start.sh
RUN chmod +x /app/scripts/start.sh

EXPOSE 8080

CMD ["/app/scripts/start.sh"]
