# 📊 Core Performance Benchmark Report

- **Timestamp**: 2026-09-03T23:39:39.181746700
- **JVM Runtime**: Java 21.0.2 (Windows 11, amd64)
- **Benchmark Iterations**: 100 000 operations per test

---

## 1. Hot-Path Measurements (Post-Optimization)

| Component / Target Operation | Elapsed (ms) | Latency (ns/op) | Throughput (ops/sec) | Heap Churn (MB) |
| :--- | :--- | :--- | :--- | :--- |
| **ContextPlaceholderParser.resolve()** | 287,73 ms | 2877,3 ns | 347 542 op/s | ~24,01 MB |
| **Regex Placeholder Extraction** | 112,53 ms | 1125,3 ns | 888 658 op/s | ~47,01 MB |
| **ExecutionContext Alloc & Dispatch** | 12,07 ms | 120,7 ns | 8 283 014 op/s | ~22,50 MB |

---

## 2. Optimization Analysis
- **ContextPlaceholderParser**: Replaced regex backtracking and Matcher allocation with high-speed index scanning. Zero Matcher overhead.
- **PapiHook**: Replaced synchronized StringBuffer with StringBuilder and guarded reflection invocations.
- **ExecutionContext**: Tailored ConcurrentHashMap initial capacity and load factors.
