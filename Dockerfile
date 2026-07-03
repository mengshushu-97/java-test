FROM maven:3.9-eclipse-temurin-17 AS build

ARG OTEL_JAVA_AGENT_VERSION=2.16.0

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
RUN mvn -q -DskipTests dependency:go-offline \
    && mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy \
      -Dartifact=io.opentelemetry.javaagent:opentelemetry-javaagent:${OTEL_JAVA_AGENT_VERSION}:jar \
      -DoutputDirectory=/otel \
      -Dmdep.stripVersion=true

COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /otel/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
COPY --from=build /workspace/target/java-test-*.jar /app/app.jar
COPY scripts/start.sh /app/scripts/start.sh
RUN chmod +x /app/scripts/start.sh

EXPOSE 8080

CMD ["/app/scripts/start.sh"]
