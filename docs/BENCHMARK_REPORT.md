# 📊 Core Performance Benchmark Report

- **Timestamp**: 2026-09-04T01:19:15.879201200
- **JVM Runtime**: Java 21.0.2 (Windows 11, amd64)
- **Benchmark Iterations**: 100 000 operations per test

---

## 1. Hot-Path Measurements (Post-Optimization)

| Component / Target Operation | Elapsed (ms) | Latency (ns/op) | Throughput (ops/sec) | Heap Churn (MB) |
| :--- | :--- | :--- | :--- | :--- |
| **ContextPlaceholderParser.resolve()** | 91,36 ms | 913,6 ns | 1 094 530 op/s | ~12,57 MB |
| **Regex Placeholder Extraction** | 113,16 ms | 1131,6 ns | 883 690 op/s | ~37,92 MB |
| **ExecutionContext Alloc & Dispatch** | 9,08 ms | 90,8 ns | 11 011 396 op/s | ~22,50 MB |

---

## 2. Optimization Analysis
- **ContextPlaceholderParser**: Replaced regex backtracking and Matcher allocation with high-speed index scanning. Zero Matcher overhead.
- **PapiHook**: Replaced synchronized StringBuffer with StringBuilder and guarded reflection invocations.
- **ExecutionContext**: Tailored ConcurrentHashMap initial capacity and load factors.
