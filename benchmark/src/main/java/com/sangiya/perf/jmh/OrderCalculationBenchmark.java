package com.sangiya.perf.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

/**
 * JMH microbenchmarks for hot-path arithmetic that runs in every order placement.
 * Run: java -jar benchmark/target/benchmarks.jar
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class OrderCalculationBenchmark {

    private BigDecimal unitPrice;
    private int quantity;

    @Setup
    public void setUp() {
        unitPrice = new BigDecimal("19.99");
        quantity = 7;
    }

    @Benchmark
    public BigDecimal calculateTotal_bigDecimal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    @Benchmark
    public double calculateTotal_primitiveDouble() {
        return Math.round(19.99 * quantity * 100.0) / 100.0;
    }

    @Benchmark
    public long calculateTotal_longCents() {
        // Store cents as long — avoids floating-point entirely
        long unitCents = 1999L;
        return unitCents * quantity;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(OrderCalculationBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
