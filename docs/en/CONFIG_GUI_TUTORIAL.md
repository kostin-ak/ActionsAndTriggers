# 🎨 Comprehensive Guide: Assembling GUIs via YAML

## 1. Introduction & Concept
With **ActionsAndTriggers (AAT)**, custom menus and complex machinery interfaces require zero Java code. Every interface—from a world navigator to an animated industrial synthesizer with multi-tier upgrades—is configured via declarative YAML files located in `plugins/ActionsAndTriggers/guis/*.yml`.

### Key Architectural Advantages:
- **Visual Pattern Masks**: No manual slot math (0..53). You draw the interface layout using standard `9xN` characters.
- **Guaranteed Item & Resource Safety**: Real player items placed in input or output slots will never vanish. Upon inventory close, damage, disconnect, or server reload, all items are safely returned to player inventories or dropped at their feet if full.
- **Seamless Resourcepack Alignment**: Full support for MiniMessage font glyph shifts (`<shift:-8><glyph:custom_gui><shift:-164>`) and physically transparent slots (`transparent_slot`) to blend with Oraxen and ItemsAdder custom container textures.

---

## 2. Anatomy of a GUI Configuration File

Every file in `guis/` (e.g. `guis/custom_bench.yml`) adheres to this structure:

```yaml
id: custom_bench        # Unique identifier used by core:open_gui
title: "<gradient:#FFA07A:#FF6347>❖ Engineer Workbench ❖</gradient>" # MiniMessage header
rows: 3                 # Height: 1 to 6 rows

allow_combat: false     # Prohibit opening while tagged in combat
close_on_damage: true   # Immediately close when player receives damage

mask:
  pattern:
    - "#########"       # Row 1 (slots 0..8)
    - "#I#+#C#=#"       # Row 2 (slots 9..17)
    - "####X####"       # Row 3 (slots 18..26)
  components:
    "#":
      type: slot_cover
      material: "minecraft:gray_stained_glass_pane"
      name: " "
    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Close Window</red>"
      on_click:
        - action: "core:close_gui"
```

---

## 3. Deep Dive into Widget Types

### 3.1. Decorative Panels & Fillers

#### `slot_cover` (Physical Filler Item)
Places a physical item (typically stained glass), strips lore, and cancels clicks.
```yaml
"#":
  type: slot_cover
  material: "minecraft:black_stained_glass_pane"
  name: " "
```

#### `transparent_slot` (Invisible Slot for Backgrounds)
Physically empty slot (`Material.AIR`), letting custom chest art shine through unobstructed while completely blocking clicks and drags.
```yaml
".":
  type: transparent_slot
```

---

### 3.2. Interactive Buttons

#### `button` (Standard Action Button)
Executes an `on_click` action pipeline when clicked.
```yaml
"B":
  type: button
  material: "oraxen:astral_atlas"    # Accepts oraxen:id, itemsadder:id, and minecraft:id
  name: "<gold>Teleport to Sanctuary</gold>"
  lore:
    - "<gray>Click to return to</gray>"
    - "<yellow>the safe zone.</yellow>"
  on_click:
    - action: "core:command"
      command: "spawn"
      as_console: false
    - action: "core:sound"
      sound: "entity.enderman.teleport"
      volume: 1.0
      pitch: 1.2
```

#### `cycle_button` (Multi-State Toggle)
Cycles through states sequentially on click, storing state in `session_key`.
```yaml
"M":
  type: cycle_button
  session_key: "machine_speed"
  states:
    - id: "eco"
      material: "minecraft:lime_dye"
      name: "<green>Mode: ECO (-50% energy)</green>"
    - id: "normal"
      material: "minecraft:yellow_dye"
      name: "<yellow>Mode: STANDARD</yellow>"
    - id: "turbo"
      material: "minecraft:red_dye"
      name: "<red>Mode: TURBO (+100% speed)</red>"
```

---

### 3.3. Industrial Machine Slots (Item Safety)

#### `input_slot` (Material & Ingot Input)
Restricts insertion to items in `allowed_items`. When empty, renders an interactive ghost placeholder tagged with PDC `actionstriggers:gui_placeholder`.
```yaml
"W":
  type: input_slot
  allowed_items:
    - "minecraft:water_bucket"
  placeholder_material: "minecraft:bucket"
  placeholder_name: "<aqua>💧 Insert Water Bucket</aqua>"
  placeholder_lore:
    - "<gray>Required to cool the machine.</gray>"
```

#### `output_slot` (Product Extraction)
Extraction-only slot. Placing items inside is strictly disallowed.
```yaml
"O":
  type: output_slot
```

---

### 3.4. Gauges & Dynamic Indicators

#### `progress_bar` (Animated Process Bar)
Animates continuous operations at 10 FPS with support for `{percent}`, `{stage}`, and `{temp}` placeholders.
```yaml
">":
  type: progress_bar
  idle_material: "minecraft:spectral_arrow"
  idle_name: "<aqua>⚡ Click to Start Freezing</aqua>"
  running_material: "minecraft:clock"
  running_name: "<gradient:#74B9FF:#0984E3>⚙ Freezing: {percent}%</gradient>"
  bar_length: 10
  filled_color: "#74B9FF"
  empty_color: "#636E72"
  on_click:
    - action: "core:cryo_freeze"
      water_slot: 10
      crystal_slot: 12
      output_slot: 16
      upgrade_slot: 24
```

#### `fluid_tank` (Vertical Fluid Level Gauge)
Renders liquid levels as a vertical column filled from bottom to top.
```yaml
"T":
  type: fluid_tank
  level_key: "coolant_level"
  max_level: 100
  filled_material: "minecraft:water_bucket"
  empty_material: "minecraft:bucket"
  title: "<aqua>Coolant Tank: {level}/{max}</aqua>"
```

---

## 4. Complete Real-World Example: Advanced Cryogenic Fabricator

Here is the production-ready configuration for an ice synthesizer featuring Tier 1-3 accelerator modules:

```yaml
id: ice_fabricator_gui
title: "<shift:-8><glyph:ice_fabricator_bg><shift:-164><gradient:#74B9FF:#0984E3>❄ Cryogenic Fabricator ❄</gradient>"
rows: 3
allow_combat: false
close_on_damage: true

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
      allowed_items:
        - "minecraft:water_bucket"
      placeholder_material: "minecraft:bucket"
      placeholder_name: "<aqua>💧 Water Bucket Input</aqua>"

    "C":
      type: input_slot
      allowed_items:
        - "oraxen:frost_crystal"
        - "minecraft:amethyst_shard"
      placeholder_material: "minecraft:amethyst_shard"
      placeholder_name: "<gradient:#E0C3FC:#8EC5FC>❄ Frost Crystal</gradient>"

    ">":
      type: progress_bar
      idle_material: "minecraft:spectral_arrow"
      idle_name: "<aqua>⚡ Start Process</aqua>"
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

    "M":
      type: cycle_button
      session_key: "fabricator_mode"
      states:
        - id: "blue_ice"
          material: "minecraft:blue_ice"
          name: "<gradient:#74B9FF:#0984E3>Mode: Blue Ice</gradient>"
        - id: "packed_ice"
          material: "minecraft:packed_ice"
          name: "<gradient:#A0E7E5:#B4F8C8>Mode: Packed Ice</gradient>"

    "S":
      type: button
      material: "oraxen:frost_crystal"
      name: "<gradient:#E0C3FC:#8EC5FC>Cryostat: ACTIVE</gradient>"

    "U":
      type: input_slot
      allowed_items:
        - "oraxen:cryo_accelerator_t1"
        - "oraxen:cryo_accelerator_t2"
        - "oraxen:cryo_accelerator_t3"
        - "minecraft:copper_ingot"
        - "minecraft:iron_ingot"
        - "minecraft:gold_ingot"
        - "minecraft:emerald"
        - "minecraft:diamond"
        - "minecraft:netherite_ingot"
      placeholder_material: "minecraft:redstone_torch"
      placeholder_name: "<gradient:#FFA07A:#FF6347>⚡ Upgrade Module (T1-T3)</gradient>"
      placeholder_lore:
        - "<gray>T1 (-25%): Copper / Iron</gray>"
        - "<gray>T2 (-50%): Gold / Emerald</gray>"
        - "<gray>T3 (-75%): Diamond / Netherite</gray>"

    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Close</red>"
      on_click:
        - action: "core:close_gui"
```
