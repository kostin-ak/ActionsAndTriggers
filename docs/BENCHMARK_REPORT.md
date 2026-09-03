# 📊 Core Performance Benchmark Report

- **Timestamp**: 2026-09-04T01:25:50.204893700
- **JVM Runtime**: Java 21.0.2 (Windows 11, amd64)
- **Benchmark Iterations**: 100 000 operations per test

---

## 1. Hot-Path Measurements (Post-Optimization)

| Component / Target Operation | Elapsed (ms) | Latency (ns/op) | Throughput (ops/sec) | Heap Churn (MB) |
| :--- | :--- | :--- | :--- | :--- |
| **ContextPlaceholderParser.resolve()** | 79,41 ms | 794,1 ns | 1 259 211 op/s | ~12,58 MB |
| **Regex Placeholder Extraction** | 90,13 ms | 901,3 ns | 1 109 455 op/s | ~55,00 MB |
| **ExecutionContext Alloc & Dispatch** | 8,65 ms | 86,5 ns | 11 564 170 op/s | ~22,50 MB |

---

## 2. Optimization Analysis
- **ContextPlaceholderParser**: Replaced regex backtracking and Matcher allocation with high-speed index scanning. Zero Matcher overhead.
- **PapiHook**: Replaced synchronized StringBuffer with StringBuilder and guarded reflection invocations.
- **ExecutionContext**: Tailored ConcurrentHashMap initial capacity and load factors.
