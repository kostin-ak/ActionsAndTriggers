# 📊 Core Performance Benchmark Report

- **Timestamp**: 2026-09-03T23:51:54.476617300
- **JVM Runtime**: Java 21.0.2 (Windows 11, amd64)
- **Benchmark Iterations**: 100 000 operations per test

---

## 1. Hot-Path Measurements (Post-Optimization)

| Component / Target Operation | Elapsed (ms) | Latency (ns/op) | Throughput (ops/sec) | Heap Churn (MB) |
| :--- | :--- | :--- | :--- | :--- |
| **ContextPlaceholderParser.resolve()** | 293,11 ms | 2931,1 ns | 341 167 op/s | ~23,01 MB |
| **Regex Placeholder Extraction** | 123,01 ms | 1230,1 ns | 812 917 op/s | ~20,51 MB |
| **ExecutionContext Alloc & Dispatch** | 11,42 ms | 114,2 ns | 8 755 264 op/s | ~22,50 MB |

---

## 2. Optimization Analysis
- **ContextPlaceholderParser**: Replaced regex backtracking and Matcher allocation with high-speed index scanning. Zero Matcher overhead.
- **PapiHook**: Replaced synchronized StringBuffer with StringBuilder and guarded reflection invocations.
- **ExecutionContext**: Tailored ConcurrentHashMap initial capacity and load factors.
