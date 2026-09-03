# ActionsAndTriggers (AAT)

<div align="center">

[![Java](https://img.shields.io/badge/Java-21+-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4%20--%2026.2-brightgreen.svg?style=flat-square)](https://papermc.io/)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%2F%20Purpur-blue.svg?style=flat-square)](https://purpurmc.org/)
[![License](https://img.shields.io/badge/License-PolyForm%20Noncommercial-yellow.svg?style=flat-square)](LICENSE)

**A modular event scripting, task scheduling, and custom GUI engine for Minecraft servers.**

[🇷🇺 Читать на русском языке](README_RU.md) • [📖 Documentation](docs/README.md) • [Triggers & Actions](docs/en/TRIGGERS_AND_ACTIONS.md) • [GUI Engine](docs/en/GUI_ENGINE.md)

</div>

---

## Overview

**ActionsAndTriggers (AAT)** brings together custom menus, conditional event triggers, task schedulers, and item management into one structured system. Instead of installing separate plugins for commands, menus, cooldowns, and mechanics, AAT handles them through a unified YAML configuration format and an open Java API.

### Key Capabilities
- **Event-Driven Triggers**: Execute actions on player interactions, block breaks, damage, advancements, crafting, and server timers.
- **Task Scheduling & Cooldowns**: Built-in chains with delays (`core:delay`), loops with counters (`core:repeat`), identifiable scheduled tasks (`core:schedule`), and player cooldowns.
- **Mask-Based GUI Menus**: Configure menus visually using character masks. Supports input/output slots, progress indicators, fluid bars, tabs, and multi-page lists.
- **Item Safety**: Input items are tracked per-player. Closing menus, taking damage, or restarting the server returns items directly to the player or drops them safely on the ground.
- **Soft Integrations**: Native support for **Oraxen**, **ItemsAdder**, **PlaceholderAPI**, and **LuckPerms** when present.
- **Localization**: All messages and interfaces are externalized into YAML language bundles (`messages_en.yml`, `messages_ru.yml`).

---

## 📋 Requirements & Supported Versions

| Component | Requirement | Notes |
| :--- | :--- | :--- |
| **Minecraft Version** | **1.21.4 — 26.2** | Verified on Paper and Purpur releases |
| **Server Software** | **Paper**, **Purpur** | Required for modern Adventure Component and async event pipelines |
| **Java Runtime** | **Java 21+** | Java 25+ recommended for 26.2 servers |
| **Optional Plugins** | **PlaceholderAPI**, **LuckPerms**, **Oraxen**, **ItemsAdder** | Loaded automatically if present |

---

## 🛠️ Quick Start

### 1. Installation
1. Download `ActionsAndTriggers.jar` and place it in the `plugins/` directory.
2. Ensure the server is running on **Paper** or **Purpur** with **Java 21+**.
3. Start the server to generate default configurations and language files.

### 2. Event Trigger Example (`plugins/ActionsAndTriggers/triggers/navigation.yml`)
```yaml
triggers:
  # Open the Astral Atlas on right-click when outside of combat
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

  # Reject opening during combat
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
        text: "<red>Cannot open during combat! Wait {player.combat_remaining}s.</red>"
```

### 3. Machine GUI Example (`plugins/ActionsAndTriggers/guis/ice_fabricator.yml`)
```yaml
id: ice_fabricator_gui
title: "<gradient:#74B9FF:#0984E3>Cryogenic Ice Fabricator</gradient>"
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
      placeholder_name: "<aqua>Water Bucket Slot</aqua>"
    "C":
      type: input_slot
      allowed_items: ["oraxen:frost_crystal", "minecraft:amethyst_shard"]
      placeholder_material: "minecraft:amethyst_shard"
      placeholder_name: "<gradient:#E0C3FC:#8EC5FC>Catalyst Slot</gradient>"
    ">":
      type: progress_bar
      idle_material: "minecraft:spectral_arrow"
      idle_name: "<aqua>Start Freezing</aqua>"
      running_material: "minecraft:clock"
      running_name: "<gradient:#74B9FF:#0984E3>Freezing: {percent}%</gradient>"
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
      placeholder_name: "<gradient:#FFA07A:#FF6347>Upgrade Slot</gradient>"
    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Close</red>"
      on_click:
        - action: "core:close_gui"
```

---

## 💻 Developer API (Java / Kotlin)

External plugins can interact with AAT directly through the lightweight `:aat-api` module:

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

## 📚 Documentation & Examples

- [Examples Catalog](docs/examples/README.md) — 6 complete scenarios covering navigators, weapons, machines, timers, and permissions.
- [Architecture Guide](docs/en/ARCHITECTURE.md) — Plugin structure and lifecycle overview.
- [GUI Engine Guide](docs/en/GUI_ENGINE.md) — Widget types, masks, and safe inventory handling.
- [GUI Creation Tutorial](docs/en/CONFIG_GUI_TUTORIAL.md) — Step-by-step YAML menu tutorial.
- [Triggers, Filters & Actions](docs/en/TRIGGERS_AND_ACTIONS.md) — Full list of built-in components and parameters.
- [Trigger Scripting Tutorial](docs/en/CONFIG_SCRIPTS_TUTORIAL.md) — Guide to writing custom mechanics in YAML.
- [Developer API Guide](docs/en/API_GUIDE.md) — Details on registering custom triggers and actions.

---

## 🔨 Building

The project uses Gradle with two modules:
- **`:aat-api`**: Standalone public interfaces and builders without server implementation code.
- **`:aat-core`**: Full plugin code, command handlers, and third-party hooks.

```bash
# Build standalone public API jar
./gradlew apiJar
# Output: build/libs/ActionsAndTriggers-1.0-SNAPSHOT-api.jar

# Build runtime plugin jar (shaded)
./gradlew shadowJar
# Output: build/libs/ActionsAndTriggers-1.0-SNAPSHOT-all.jar
```

---

## ⚖️ License & Terms

1. **Community Edition (Non-Commercial)**:
   - Licensed under the **[PolyForm Noncommercial License 1.0.0](LICENSE)**.
   - Free for non-commercial servers, private testing, and educational review.
   - Anyone may compile the code from source free of charge (`./gradlew build`).
   - Redistribution of pre-compiled binaries by third parties is not permitted.
   - External plugins compiling against `:aat-api` may use any license.

2. **Commercial Use**:
   - Deployment on monetized servers or commercial networks requires a **Commercial License** from the copyright holder (**Alex Kostin**).
   - Inquiries: `kostin.ak@mail.ru`.

3. **Contributions**:
   - Pull requests and contributions are subject to full copyright assignment to **Alex Kostin** as detailed in [CONTRIBUTING.md](CONTRIBUTING.md) and Section 3.2 of the [LICENSE](LICENSE).
