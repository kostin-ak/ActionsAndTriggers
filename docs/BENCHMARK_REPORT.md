# 📊 Core Performance Benchmark Report

- **Timestamp**: 2026-09-03T23:35:12.398924700
- **JVM Runtime**: Java 21.0.2 (Windows 11, amd64)
- **Benchmark Iterations**: 100 000 operations per test

---

## 1. Hot-Path Measurements (Post-Optimization)

| Component / Target Operation | Elapsed (ms) | Latency (ns/op) | Throughput (ops/sec) | Heap Churn (MB) |
| :--- | :--- | :--- | :--- | :--- |
| **ContextPlaceholderParser.resolve()** | 300,08 ms | 3000,8 ns | 333 246 op/s | ~30,01 MB |
| **Regex Placeholder Extraction** | 121,38 ms | 1213,8 ns | 823 880 op/s | ~97,01 MB |
| **ExecutionContext Alloc & Dispatch** | 13,79 ms | 137,9 ns | 7 252 683 op/s | ~22,50 MB |

---

## 2. Optimization Analysis
- **ContextPlaceholderParser**: Replaced regex backtracking and Matcher allocation with high-speed index scanning. Zero Matcher overhead.
- **PapiHook**: Replaced synchronized StringBuffer with StringBuilder and guarded reflection invocations.
- **ExecutionContext**: Tailored ConcurrentHashMap initial capacity and load factors.
