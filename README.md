# java-test

Spring Boot 示例服务，JDK 17，输出 JSON 日志到 stdout，并通过 OpenTelemetry Java Agent 接入 Collector。

## 功能

- `GET /health` 健康检查
- `GET /api/data` 返回 Java 服务数据
- `GET /api/agent/data` 供 Agent 回调取数
- `POST /api/node-chain` 供 Node 调用，内部继续调用 Agent，形成 `node -> java -> agent`
- `POST /api/node-simple` 供 Node 调用，形成 `node -> java`
- `POST /api/trigger/java-node-agent` 触发 `java -> node -> agent`
- `POST /api/trigger/java-agent-java` 触发 `java -> agent -> java`
- 每秒输出 heartbeat 日志
- 每分钟自动执行 `java -> node -> agent` 和 `java -> agent -> java`

## 本地启动

```bash
mvn test
mvn spring-boot:run
```

本地如需 trace，下载 OpenTelemetry Java Agent 后设置：

```bash
export JAVA_TOOL_OPTIONS="-javaagent:/path/to/opentelemetry-javaagent.jar"
export OTEL_SERVICE_NAME=java-test
export OTEL_EXPORTER_OTLP_ENDPOINT=http://127.0.0.1:4318
export OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
export OTEL_INSTRUMENTATION_LOGGING_MDC_ENABLED=true
mvn spring-boot:run
```

## Kubernetes

```bash
kubectl apply -f k8s/test
kubectl get pods -n test -l app.kubernetes.io/name=java-test
```

## CI/CD

当前不使用镜像仓库，使用 GitHub self-hosted runner 在 k3s 服务器本机构建镜像并部署。见 [docs/cicd-local-k3s.md](docs/cicd-local-k3s.md)。

生产多副本时，定时任务会在每个副本执行。正式环境建议把定时任务拆成独立 CronJob，或增加 leader election。
