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
 * Калиброванный микро-бенчмарк для фиксации базовой производительности горячих путей AAT.
 */
public class BaselineBenchmarkTest {

    private static final int WARMUP_ITERATIONS = 10_000;
    private static final int MEASURE_ITERATIONS = 100_000;

    private static final ContextKey<Double> KEY_DAMAGE = ContextKey.of("damage", Double.class);
    private static final ContextKey<String> KEY_STAGE = ContextKey.of("stage", String.class);
    private static final ContextKey<String> KEY_TEMP = ContextKey.of("temp", String.class);
    private static final ContextKey<String> KEY_1 = ContextKey.of("key1", String.class);
    private static final ContextKey<String> KEY_2 = ContextKey.of("key2", String.class);

    @Test
    @DisplayName("Бенчмарк горячих путей ядра AAT (Исходная базовая линия)")
    public void runBaselineBenchmarks() throws IOException {
        System.out.println("=== НАЧАЛО БЕНЧМАРКА AAT (БАЗОВАЯ ЛИНИЯ ДО ОПТИМИЗАЦИИ) ===");

        // 1. Тест парсинга контекстных плейсхолдеров
        BenchmarkResult parserResult = benchmarkContextParser();

        // 2. Тест поиска и регулярных выражений
        BenchmarkResult regexScanResult = benchmarkRegexExtraction();

        // 3. Тест создания ExecutionContext и доступа к ключам
        BenchmarkResult contextAllocResult = benchmarkContextAllocation();

        // Формирование отчета
        String report = String.format("""
                # 📊 Отчет о Производительности Ядра AAT (Benchmark Report)
                
                **Дата замера**: %s
                **Среда исполнения**: Java %s (%s, %s)
                **Статус**: Исходная базовая линия (ДО оптимизации)
                
                ---
                
                ## 1. Замеры производительности горячих путей (100,000 итераций)
                
                | Компонент / Тест | Общее время (мс) | Задержка (нс/оп) | Пропускная способность | Аллокация памяти |
                | :--- | :--- | :--- | :--- | :--- |
                | **ContextPlaceholderParser.resolve()** | %.2f ms | %.1f ns | %,d op/s | ~%.2f MB |
                | **Regex Placeholder Extraction** | %.2f ms | %.1f ns | %,d op/s | ~%.2f MB |
                | **ExecutionContext Alloc & Dispatch** | %.2f ms | %.1f ns | %,d op/s | ~%.2f MB |
                
                ---
                
                ## 2. Архитектурные узкие места (Bottlenecks)
                1. **ContextPlaceholderParser**: создание новых `Matcher`, повторные аллокации `StringBuilder` и вызовы `Matcher.quoteReplacement` на каждый прогон строки.
                2. **MiniMessage**: повторная десериализация неизменяемых строк шаблонов на каждом тике/клике.
                3. **ExecutionContext**: аллокация новой `ConcurrentHashMap` и автобоксинг при каждом Bukkit-событии.
                """,
                java.time.LocalDateTime.now(),
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                parserResult.elapsedMillis, parserResult.nsPerOp, parserResult.throughput, parserResult.allocatedMb,
                regexScanResult.elapsedMillis, regexScanResult.nsPerOp, regexScanResult.throughput, regexScanResult.allocatedMb,
                contextAllocResult.elapsedMillis, contextAllocResult.nsPerOp, contextAllocResult.throughput, contextAllocResult.allocatedMb
        );

        System.out.println(report);

        // Сохраняем в docs/BENCHMARK_REPORT.md
        Path docsPath = Path.of("docs");
        if (!Files.exists(docsPath)) {
            Files.createDirectories(docsPath);
        }
        File reportFile = new File("docs/BENCHMARK_REPORT.md");
        try (FileWriter writer = new FileWriter(reportFile)) {
            writer.write(report);
        }
        System.out.println("Отчет успешно сохранен в: " + reportFile.getAbsolutePath());
    }

    private BenchmarkResult benchmarkContextParser() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.set(CoreKeys.WORLD, "world_nether");
        ctx.set(KEY_DAMAGE, 15.5);
        ctx.set(KEY_STAGE, "CRYO_SYNTHESIS");
        ctx.set(KEY_TEMP, "-273C");

        String template = "Игрок в мире {world} получил урон {damage}! Стадия: {stage}, Температура: {temp}.";

        // Прогрев JIT
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
        String template = "<gradient:#74B9FF:#0984E3>Прогресс: {percent}%</gradient> | Этап: {stage} | Статус: {status}";

        // Прогрев JIT
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
        // Прогрев JIT
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
