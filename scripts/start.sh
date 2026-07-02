#!/usr/bin/env sh
set -eu

export SERVICE_NAME="${SERVICE_NAME:-java-test}"
export OTEL_SERVICE_NAME="${OTEL_SERVICE_NAME:-$SERVICE_NAME}"
export OTEL_EXPORTER_OTLP_PROTOCOL="${OTEL_EXPORTER_OTLP_PROTOCOL:-http/protobuf}"
export OTEL_INSTRUMENTATION_LOGGING_MDC_ENABLED="${OTEL_INSTRUMENTATION_LOGGING_MDC_ENABLED:-true}"

if [ -f /otel/opentelemetry-javaagent.jar ]; then
  export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -javaagent:/otel/opentelemetry-javaagent.jar"
fi

exec java ${JAVA_OPTS:-} -jar /app/app.jar
