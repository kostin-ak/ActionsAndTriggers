# 📊 Core Performance Benchmark Report

- **Timestamp**: 2026-09-03T23:29:17.423004500
- **JVM Runtime**: Java 21.0.2 (Windows 11, amd64)
- **Benchmark Iterations**: 100 000 operations per test

---

## 1. Hot-Path Measurements (Post-Optimization)

| Component / Target Operation | Elapsed (ms) | Latency (ns/op) | Throughput (ops/sec) | Heap Churn (MB) |
| :--- | :--- | :--- | :--- | :--- |
| **ContextPlaceholderParser.resolve()** | 288,91 ms | 2889,1 ns | 346 124 op/s | ~3,52 MB |
| **Regex Placeholder Extraction** | 123,13 ms | 1231,3 ns | 812 120 op/s | ~19,52 MB |
| **ExecutionContext Alloc & Dispatch** | 12,72 ms | 127,2 ns | 7 863 427 op/s | ~22,50 MB |

---

## 2. Optimization Analysis
- **ContextPlaceholderParser**: Replaced regex backtracking and Matcher allocation with high-speed index scanning. Zero Matcher overhead.
- **PapiHook**: Replaced synchronized StringBuffer with StringBuilder and guarded reflection invocations.
- **ExecutionContext**: Tailored ConcurrentHashMap initial capacity and load factors.
