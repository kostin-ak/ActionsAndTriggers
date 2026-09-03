# ⚡ ActionsAndTriggers (AAT)

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Paper](https://img.shields.io/badge/Paper-1.21.4+-blue.svg?style=flat-square)](https://papermc.io/)
[![Tests](https://img.shields.io/badge/Tests-34%2F34%20Passing-brightgreen.svg?style=flat-square)](docs/en/BENCHMARK_REPORT.md)
[![Performance](https://img.shields.io/badge/Throughput-767k%20ops%2Fsec-purple.svg?style=flat-square)](docs/en/BENCHMARK_REPORT.md)

**An ultra-high-performance, event-driven scripting, scheduling, and composite GUI engine for Paper & Purpur Minecraft servers.**

[🇷🇺 Читать на русском языке](README_RU.md) • [📖 Documentation Portal](docs/README.md) • [⚡ Triggers & Actions](docs/en/TRIGGERS_AND_ACTIONS.md) • [🖥️ GUI Engine](docs/en/GUI_ENGINE.md)

</div>

---

## 🌟 Why ActionsAndTriggers?

Modern Minecraft servers are often overloaded with dozens of disjointed plugins: one for menus, one for conditional commands, one for custom item abilities, and another for timers. 

**ActionsAndTriggers (AAT)** unifies these workflows into a single, cohesive, enterprise-grade architecture:
- 🚀 **Zero-Allocation Hot Paths**: Handcrafted single-pass index-of parsing provides **3.12x faster placeholder resolution** and cuts heap churn by **95.6%** compared to regex engines.
- 🎨 **Widget-Oriented Composite GUI Engine**: Visual YAML masks, animated progress bars, vertical fluid tanks, multi-page lists, tab containers, and seamless custom font glyph background support.
- 🛡️ **Guaranteed Item & Resource Safety**: Slots automatically unroll from masks. Closing menus, receiving combat damage, or server reloads will **never** eat items—inputs and outputs safely return to player inventories or drop at their feet.
- ⏱️ **Full Task Scheduling & Cooldowns**: Built-in delayed chains (`core:delay`), repeating loops with `{iteration}` counters (`core:repeat`), identifiable scheduled tasks (`core:schedule`), and player cooldowns.
- 🌐 **Clean Third-Party Integrations**: Transparent support for **Oraxen**, **ItemsAdder**, and **PlaceholderAPI** without hard dependencies or startup crashes.
- 🌍 **Native i18n Localization**: Zero hardcoded strings. Centralized multilingual dictionary with auto-extracting language bundles (`messages_en.yml`, `messages_ru.yml`).

---

## 📊 Performance Benchmarks (Java 21)

Benchmarked over **100,000 iterations** using a calibrated, isolated test harness:

| Operation / Hot Path | Baseline (Legacy Regex) | AAT Optimized | Performance Gain |
| :--- | :--- | :--- | :--- |
| **`ContextPlaceholderParser.resolve()`** | 4,074.8 ns/op | **1,303.5 ns/op** | **⚡ 3.12x faster (+212.6%)** |
| **`Regex Extraction & Matcher Churn`** | 57.47 MB | **2.51 MB** | **⚡ 95.6% memory reduction** |
| **`ExecutionContext Dispatch`** | 140.2 ns/op | **93.1 ns/op** | **⚡ 1.51x faster (+50.6%)** |

> Complete benchmark details and profiling logs are documented in [BENCHMARK_REPORT.md](docs/en/BENCHMARK_REPORT.md).

---

## 🛠️ Quick Start

### 1. Installation
1. Place `ActionsAndTriggers.jar` into your server's `plugins/` directory.
2. Ensure you are running **Paper** or **Purpur** on **Java 21+**.
3. Start the server. The plugin will automatically unpack configurations and language files.

### 2. Declarative Trigger Example (`plugins/ActionsAndTriggers/triggers/navigation.yml`)
```yaml
triggers:
  # Open Astral Atlas on right-click outside of combat
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "oraxen:astral_atlas"
        ignore_case: true
      - type: "core:not_in_combat"
    actions:
      - action: "core:open_gui"
        gui: "astral_atlas"

  # Deny opening while tagged in combat
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "oraxen:astral_atlas"
        ignore_case: true
      - type: "core:in_combat"
    actions:
      - action: "core:sound"
        sound: "entity.villager.no"
      - action: "core:message"
        type: "actionbar"
        text: "<red><bold>Astral connection severed! Wait {player.combat_remaining}s.</bold></red>"
```

### 3. Composite Machine GUI Example (`plugins/ActionsAndTriggers/guis/ice_fabricator.yml`)
```yaml
id: ice_fabricator_gui
title: "<gradient:#74B9FF:#0984E3>❄ Cryogenic Ice Fabricator</gradient>"
rows: 3

mask:
  pattern:
    - "#########"
    - "#W#C#>#O#"
    - "##M#S#U#X"
  components:
    "#":
      type: transparent_slot
    "W":
      type: input_slot
      allowed_items: ["minecraft:water_bucket"]
      placeholder_material: "minecraft:bucket"
      placeholder_name: "<aqua>💧 Water Bucket Slot</aqua>"
    "C":
      type: input_slot
      allowed_items: ["oraxen:frost_crystal", "minecraft:amethyst_shard"]
      placeholder_material: "minecraft:amethyst_shard"
      placeholder_name: "<gradient:#E0C3FC:#8EC5FC>❄ Catalyst Slot</gradient>"
    ">":
      type: progress_bar
      idle_material: "minecraft:spectral_arrow"
      idle_name: "<aqua>⚡ Start Cryo-Freezing</aqua>"
      running_material: "minecraft:clock"
      running_name: "<gradient:#74B9FF:#0984E3>❄ Freezing: {percent}%</gradient>"
      on_click:
        - action: "core:cryo_freeze"
          water_slot: 10
          crystal_slot: 12
          output_slot: 16
          upgrade_slot: 24
    "O":
      type: output_slot
    "U":
      type: input_slot
      allowed_items: ["oraxen:cryo_accelerator_t1", "minecraft:netherite_ingot"]
      placeholder_material: "minecraft:redstone_torch"
      placeholder_name: "<gradient:#FFA07A:#FF6347>⚡ Upgrade Slot</gradient>"
    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Close</red>"
      on_click:
        - action: "core:close_gui"
```

---

## 💻 Developer Fluent API (Java / Kotlin)

Integrate directly into your plugins with a clean builder API:

```java
AATGui.builder("navigator")
    .title(MiniMessage.miniMessage().deserialize("<gradient:#70E1F5:#FFD194>World Navigator</gradient>"))
    .rows(3)
    .mask(Widgets.mask()
        .pattern(
            "#########",
            "#.S.L.C.#",
            "####X####"
        )
        .filler('#', Material.GRAY_STAINED_GLASS_PANE)
        .button('S', Material.SPRUCE_SAPLING, b -> b
            .name(Component.text("Wilderness", NamedTextColor.GREEN))
            .onClick(ctx -> ctx.getPlayer().performCommand("mv tp world"))
        )
        .button('X', Material.BARRIER, b -> b
            .name(Component.text("Close", NamedTextColor.RED))
            .onClick(ctx -> ctx.getPlayer().closeInventory())
        )
    )
    .open(player);
```

---

## 📚 Documentation & Ready-to-Use Examples

- [📦 Production Examples Catalog](docs/examples/README.md) — 6 complete working scenarios from navigators to cryo fabricators.
- [🏛️ Architecture & Lifecycle Guide](docs/en/ARCHITECTURE.md)
- [🖥️ GUI Composite Widget Engine](docs/en/GUI_ENGINE.md)
- [🎨 GUI Creation via YAML Tutorial](docs/en/CONFIG_GUI_TUTORIAL.md)
- [⚡ Full Catalog of Triggers, Filters & Actions](docs/en/TRIGGERS_AND_ACTIONS.md)
- [📜 Scripting Triggers via YAML Tutorial](docs/en/CONFIG_SCRIPTS_TUTORIAL.md)
- [💻 Java & Kotlin Developer API Guide](docs/en/API_GUIDE.md)
- [📊 Benchmark & Optimization Metrics](docs/en/BENCHMARK_REPORT.md)

---

## 🔨 Building & Modular Structure

The project is architected as a professional multi-module Gradle project:
- **`:aat-api`**: Lightweight standalone public API, interfaces, context models, and fluent builders. Has ZERO internal core dependencies.
- **`:aat-core`**: Full plugin implementation, shaded commands, YAML parser engine, and soft-dependencies (`LuckPerms`, `Oraxen`, `ItemsAdder`).

```bash
# 1. Compile lightweight public API jar (for external plugin developers)
./gradlew apiJar
# Output: build/libs/ActionsAndTriggers-1.0-SNAPSHOT-api.jar (117 KB)

# 2. Compile full server runtime jar (with shaded Lamp and engine internals)
./gradlew shadowJar
# Output: build/libs/ActionsAndTriggers-1.0-SNAPSHOT-all.jar (569 KB)
```

---

## ⚖️ Dual-Licensing, Intellectual Property & Enterprise

This project operates under a professional **Dual-Licensing** business model:

1. **Community Edition (Non-Commercial)**:
   - Licensed under the **[PolyForm Noncommercial License 1.0.0](LICENSE)**.
   - Free for personal testing, private educational review, and non-monetized community servers.
   - **Free Source Compilation**: Anyone is welcome to clone the repository and compile the plugin from source for free via Gradle (`./gradlew build`).
   - **Pre-Compiled Releases**: Official ready-to-use release JAR files (on SpigotMC, BuiltByBit, Polymart, etc.) may be offered as paid distribution packages by the author. Redistribution of pre-compiled binaries by third parties is prohibited.
   - **Forking & Attribution**: Forks and modifications are permitted for non-commercial use, provided strict attribution to the original author (**Alex Kostin**) is maintained.
   - **Addon Linking Exception**: Third-party plugins compiling against the `:aat-api` module may be distributed under any license of their choice.

2. **Commercial & Enterprise Edition**:
   - Any deployment on commercial Minecraft networks (with web stores, monetization, or paid perks), hosting bundles, or proprietary forks requires an official **Commercial License** issued directly by the copyright holder (**Alex Kostin**).
   - **Enterprise Support & Custom Modules**: Custom triggers, actions, and priority SLA agreements are available directly from the author.
   - **Inquiries**: Contact `kostin.ak@mail.ru`.

3. **Contributions**:
   - All contributions (pull requests, patches, improvements) are subject to full copyright assignment to **Alex Kostin** as specified in [CONTRIBUTING.md](CONTRIBUTING.md) and Section 3.2 of the [LICENSE](LICENSE). Submitting code does not grant co-ownership or profit sharing.
