package kostin.ak.actionstriggers.benchmark;

import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.ContextPlaceholderParser;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Calibrated micro-benchmark measuring latency, throughput, and heap allocations on hot paths.
 */
@DisplayName("Core Hot-Path Performance Benchmark")
public class BaselineBenchmarkTest {

    private static final int WARMUP_ITERATIONS = 10_000;
    private static final int MEASURE_ITERATIONS = 100_000;

    private static final ContextKey<Double> KEY_DAMAGE = ContextKey.of("damage", Double.class);
    private static final ContextKey<String> KEY_STAGE = ContextKey.of("stage", String.class);
    private static final ContextKey<String> KEY_TEMP = ContextKey.of("temp", String.class);
    private static final ContextKey<String> KEY_1 = ContextKey.of("key1", String.class);
    private static final ContextKey<String> KEY_2 = ContextKey.of("key2", String.class);

    @Test
    @DisplayName("Execute calibrated micro-benchmarks and write comparison report")
    public void runBaselineBenchmarks() throws IOException {
        System.out.println("=== AAT CORE BENCHMARK SUITE ===");

        BenchmarkResult parserResult = benchmarkContextParser();
        BenchmarkResult regexScanResult = benchmarkRegexExtraction();
        BenchmarkResult contextAllocResult = benchmarkContextAllocation();

        String report = String.format("""
                # 📊 Core Performance Benchmark Report
                
                - **Timestamp**: %s
                - **JVM Runtime**: Java %s (%s, %s)
                - **Benchmark Iterations**: %,d operations per test
                
                ---
                
                ## 1. Hot-Path Measurements (Post-Optimization)
                
                | Component / Target Operation | Elapsed (ms) | Latency (ns/op) | Throughput (ops/sec) | Heap Churn (MB) |
                | :--- | :--- | :--- | :--- | :--- |
                | **ContextPlaceholderParser.resolve()** | %.2f ms | %.1f ns | %,d op/s | ~%.2f MB |
                | **Regex Placeholder Extraction** | %.2f ms | %.1f ns | %,d op/s | ~%.2f MB |
                | **ExecutionContext Alloc & Dispatch** | %.2f ms | %.1f ns | %,d op/s | ~%.2f MB |
                
                ---
                
                ## 2. Optimization Analysis
                - **ContextPlaceholderParser**: Replaced regex backtracking and Matcher allocation with high-speed index scanning. Zero Matcher overhead.
                - **PapiHook**: Replaced synchronized StringBuffer with StringBuilder and guarded reflection invocations.
                - **ExecutionContext**: Tailored ConcurrentHashMap initial capacity and load factors.
                """,
                java.time.LocalDateTime.now(),
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                MEASURE_ITERATIONS,
                parserResult.elapsedMillis, parserResult.nsPerOp, parserResult.throughput, parserResult.allocatedMb,
                regexScanResult.elapsedMillis, regexScanResult.nsPerOp, regexScanResult.throughput, regexScanResult.allocatedMb,
                contextAllocResult.elapsedMillis, contextAllocResult.nsPerOp, contextAllocResult.throughput, contextAllocResult.allocatedMb
        );

        System.out.println(report);

        Path docsPath = Path.of("docs");
        if (!Files.exists(docsPath)) {
            Files.createDirectories(docsPath);
        }
        File reportFile = new File("docs/BENCHMARK_REPORT.md");
        try (FileWriter writer = new FileWriter(reportFile)) {
            writer.write(report);
        }
    }

    private BenchmarkResult benchmarkContextParser() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.set(CoreKeys.WORLD, "world_nether");
        ctx.set(KEY_DAMAGE, 15.5);
        ctx.set(KEY_STAGE, "CRYO_SYNTHESIS");
        ctx.set(KEY_TEMP, "-273C");

        String template = "Player in world {world} took {damage} damage! Stage: {stage}, Temp: {temp}.";

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            ContextPlaceholderParser.resolve(template, ctx);
        }

        System.gc();
        long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long start = System.nanoTime();

        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            ContextPlaceholderParser.resolve(template, ctx);
        }

        long elapsedNanos = System.nanoTime() - start;
        long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double allocatedMb = Math.max(0, (memAfter - memBefore) / (1024.0 * 1024.0));

        return new BenchmarkResult(elapsedNanos, MEASURE_ITERATIONS, allocatedMb);
    }

    private BenchmarkResult benchmarkRegexExtraction() {
        String template = "<gradient:#74B9FF:#0984E3>Progress: {percent}%</gradient> | Stage: {stage} | Status: {status}";

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            java.util.regex.Pattern.compile("\\{([^{}]+)\\}").matcher(template).find();
        }

        System.gc();
        long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long start = System.nanoTime();

        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\{([^{}]+)\\}");
            java.util.regex.Matcher m = p.matcher(template);
            while (m.find()) {
                m.group(1);
            }
        }

        long elapsedNanos = System.nanoTime() - start;
        long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double allocatedMb = Math.max(0, (memAfter - memBefore) / (1024.0 * 1024.0));

        return new BenchmarkResult(elapsedNanos, MEASURE_ITERATIONS, allocatedMb);
    }

    private BenchmarkResult benchmarkContextAllocation() {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            ExecutionContext ctx = new ExecutionContext();
            ctx.set(KEY_1, "val1");
            ctx.set(KEY_2, "val2");
            ctx.get(KEY_1);
        }

        System.gc();
        long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long start = System.nanoTime();

        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            ExecutionContext ctx = new ExecutionContext();
            ctx.set(KEY_1, "val1");
            ctx.set(KEY_2, "val2");
            ctx.get(KEY_1);
        }

        long elapsedNanos = System.nanoTime() - start;
        long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double allocatedMb = Math.max(0, (memAfter - memBefore) / (1024.0 * 1024.0));

        return new BenchmarkResult(elapsedNanos, MEASURE_ITERATIONS, allocatedMb);
    }

    private static class BenchmarkResult {
        final double elapsedMillis;
        final double nsPerOp;
        final long throughput;
        final double allocatedMb;

        BenchmarkResult(long elapsedNanos, int iterations, double allocatedMb) {
            this.elapsedMillis = elapsedNanos / 1_000_000.0;
            this.nsPerOp = (double) elapsedNanos / iterations;
            this.throughput = (long) (iterations / (elapsedNanos / 1_000_000_000.0));
            this.allocatedMb = allocatedMb;
        }
    }
}
