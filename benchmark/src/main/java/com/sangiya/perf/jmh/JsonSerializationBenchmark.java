package com.sangiya.perf.jmh;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Measures ObjectMapper serialization/deserialization throughput.
 * A single shared ObjectMapper instance significantly outperforms creating one per request.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class JsonSerializationBenchmark {

    private ObjectMapper mapper;
    private OrderPayload payload;
    private String jsonPayload;

    @Setup
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        payload = new OrderPayload("cust-001", "prod-XYZ", 5, new BigDecimal("12.50"));
        jsonPayload = mapper.writeValueAsString(payload);
    }

    @Benchmark
    public String serialize() throws Exception {
        return mapper.writeValueAsString(payload);
    }

    @Benchmark
    public OrderPayload deserialize() throws Exception {
        return mapper.readValue(jsonPayload, OrderPayload.class);
    }

    public record OrderPayload(String customerId, String productId, int quantity, BigDecimal unitPrice) {}

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JsonSerializationBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
