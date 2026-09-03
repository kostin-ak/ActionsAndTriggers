# 📊 Core Performance Benchmark Report

- **Timestamp**: 2026-09-03T23:47:23.895812900
- **JVM Runtime**: Java 21.0.2 (Windows 11, amd64)
- **Benchmark Iterations**: 100 000 operations per test

---

## 1. Hot-Path Measurements (Post-Optimization)

| Component / Target Operation | Elapsed (ms) | Latency (ns/op) | Throughput (ops/sec) | Heap Churn (MB) |
| :--- | :--- | :--- | :--- | :--- |
| **ContextPlaceholderParser.resolve()** | 253,23 ms | 2532,3 ns | 394 904 op/s | ~31,01 MB |
| **Regex Placeholder Extraction** | 110,95 ms | 1109,5 ns | 901 276 op/s | ~98,51 MB |
| **ExecutionContext Alloc & Dispatch** | 12,34 ms | 123,4 ns | 8 107 012 op/s | ~22,50 MB |

---

## 2. Optimization Analysis
- **ContextPlaceholderParser**: Replaced regex backtracking and Matcher allocation with high-speed index scanning. Zero Matcher overhead.
- **PapiHook**: Replaced synchronized StringBuffer with StringBuilder and guarded reflection invocations.
- **ExecutionContext**: Tailored ConcurrentHashMap initial capacity and load factors.
