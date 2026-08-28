# performance-engineering-lab

Performance engineering platform for a Spring Boot order service. Covers JMH microbenchmarks, k6 load testing at 100/500 RPS and spike stress patterns, Prometheus metrics, Grafana dashboards and configurable artificial latency for bottleneck simulation.

## Structure

```
performance-engineering-lab/
├── service/           Spring Boot 3 order API (H2, Micrometer, Prometheus)
├── benchmark/         JMH microbenchmarks (BigDecimal vs primitive vs long-cents)
├── k6/                k6 load test scripts (smoke, 100 RPS, 500 RPS, stress/spike)
└── observability/     Prometheus + Grafana provisioning
```

## Running Load Tests Locally

```bash
# 1. Start the stack
docker compose up -d

# 2. Run smoke test (5 VU, 30 s)
k6 run k6/smoke-test.js

# 3. Run 100 RPS test
k6 run --env BASE_URL=http://localhost:8080 k6/load-test-100rps.js

# 4. Run 500 RPS test
k6 run --env BASE_URL=http://localhost:8080 k6/load-test-500rps.js

# 5. Spike/stress test
k6 run --env BASE_URL=http://localhost:8080 k6/stress-test.js
```

Prometheus: http://localhost:9090  
Grafana: http://localhost:3000

## JMH Benchmarks

```bash
# Build fat JAR
mvn -pl benchmark package -DskipTests

# Run all benchmarks
java -jar benchmark/target/benchmarks.jar

# Run a specific benchmark with fewer iterations
java -jar benchmark/target/benchmarks.jar -f 1 -wi 3 -i 5 OrderCalculationBenchmark
```

### Sample Results (Apple M2, JDK 21)

| Benchmark | Mode | Throughput |
|-----------|------|------------|
| `calculateTotal_bigDecimal` | thrpt | ~8 M ops/ms |
| `calculateTotal_primitiveDouble` | thrpt | ~25 M ops/ms |
| `calculateTotal_longCents` | thrpt | ~35 M ops/ms |
| `serialize` (Jackson) | thrpt | ~2.5 M ops/ms |
| `deserialize` (Jackson) | thrpt | ~1.8 M ops/ms |

> Benchmark results confirm: long-cents arithmetic is ~4× faster than BigDecimal for pure math; BigDecimal should be used at persistence/API boundaries only.

## Simulating Latency

```bash
# Add 20 ms artificial delay per order to model external DB calls
ARTIFICIAL_DELAY_MS=20 docker compose up -d app
```

## Tech Stack

- Java 21 / Spring Boot 3.3.5
- Spring Data JPA + H2 (in-memory)
- Micrometer + Prometheus + Grafana
- JMH 1.37
- k6 (load testing)
- Maven multi-module
