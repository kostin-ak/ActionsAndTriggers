# 🖥️ AAT Widget-Oriented GUI Engine

## 1. Architectural Concept
The GUI Engine in **ActionsAndTriggers** is built around the **Composite Pattern**. Instead of low-level hardcoding of slot numbers from 0 to 53, the user interface is structured as a **widget tree** combined with declarative **mask templates**.

### Key Features:
- **9xN Coordinate Grid**: Widgets operate on coordinates `x (0..8)` and `y (0..5)`. Absolute inventory slots are computed automatically.
- **Oraxen & ItemsAdder Compatibility**: Material fields accept custom IDs such as `oraxen:item_id` or `itemsadder:item_id`.
- **Custom Font Glyph Offsets**: The window `title` supports font shift tags (`<shift:-8><glyph:my_gui_bg><shift:-164>`) to render seamless custom chest textures.
- **Transparent Slots (`transparent_slot`)**: Physically empty slots (`Material.AIR`) without glass pane textures or glints, letting custom background art shine through while blocking all clicks.
- **Interactive Ghost Placeholders**: Empty slots display sample items with informative tooltips, safely tagged with PDC (`actionstriggers:gui_placeholder`).
- **Data Binding**: Automatic synchronization with session states or world tile entity PDC.
- **Guaranteed Item Safety**: Mask slots unpack recursively. When an inventory closes due to player exit, damage, or server reload, all real player items are guaranteed to return to the player's inventory or drop safely onto the ground if the inventory is full.

---

## 2. YAML Configuration Structure (`guis/*.yml`)

Each YAML file located in `plugins/ActionsAndTriggers/guis/` represents a discrete GUI definition:

```yaml
id: ice_fabricator_gui
title: "<shift:-8><glyph:ice_fabricator_bg><shift:-164><gradient:#74B9FF:#0984E3>❄ Cryogenic Ice Fabricator ❄</gradient>"
rows: 3  # Number of rows (1 to 6)

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
      placeholder_name: "<aqua>💧 Raw Material (Water)</aqua>"

    "C":
      type: input_slot
      allowed_items:
        - "oraxen:frost_crystal"
        - "minecraft:amethyst_shard"
      placeholder_material: "minecraft:amethyst_shard"
      placeholder_name: "<gradient:#E0C3FC:#8EC5FC>❄ Cryo Catalyst</gradient>"

    ">":
      type: progress_bar
      idle_material: "minecraft:spectral_arrow"
      idle_name: "<aqua>⚡ Start Cryo-Freezing</aqua>"
      running_material: "minecraft:clock"
      running_name: "<gradient:#74B9FF:#0984E3>❄ Freezing: {percent}%</gradient>"
      bar_length: 10
      filled_color: "#74B9FF"
      empty_color: "#636E72"
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
        - id: "ice"
          material: "minecraft:ice"
          name: "<aqua>Mode: Normal Ice</aqua>"

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
      placeholder_name: "<gradient:#FFA07A:#FF6347>⚡ Upgrade Module Slot</gradient>"

    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Close Interface</red>"
      on_click:
        - action: "core:close_gui"
```

---

## 3. Widget Catalog

### 3.1. Fillers and Covers
1. **`transparent_slot`** (or `invisible_slot`):
   - Keeps the slot physically empty (`Material.AIR`).
   - Ideal for resource-pack custom container graphics.
   - Blocks all clicks, drags, shift-clicks, and hotbar swaps.
2. **`slot_cover`** (or `cover`, `blank`):
   - Physical decorative pane item (e.g. `minecraft:gray_stained_glass_pane`).
   - Strips lore, name, and attributes while canceling clicks.

### 3.2. Machine & Processing Slots
1. **`input_slot` (Component & Ingot Input)**:
   - Restricts item placement to items listed in `allowed_items`.
   - Displays a ghost placeholder with custom tooltips when empty.
   - Tagged with `actionstriggers:gui_placeholder` to prevent dupes.
2. **`output_slot` (Product Extraction)**:
   - **Take-only**. Item insertion is completely disallowed.
   - Triggers `on_take` action hooks (pickup sounds, particle effects, stats).

### 3.3. Gauges & Dynamic Indicators
1. **`progress_bar` (Animated Progress Bar)**:
   - Smooth 10 FPS multi-stage process animation.
   - Template variables: `{percent}`, `{bar}`, `{stage}`, `{temp}`, `{status}`.
2. **`fluid_tank` (Vertical Fluid Gauge)**:
   - Displays fluid or coolant levels as a vertical column filled from bottom to top.
   - Properties: `level_key` (session or PDC key), `max_level`, `filled_material`, `empty_material`, `title`.

### 3.4. Navigation & View Management
1. **`paged_list` (Paginated List)**:
   - Splits a large collection of items across multiple pages automatically.
   - Configurable navigation buttons (`prev_slot`, `next_slot`).
2. **`tab_container` (Multi-Tab Container)**:
   - Switch between multiple screens or views without reopening or re-rendering whole inventories.
   - Tracks active view in `active_tab`.
3. **`cycle_button` (State Switcher)**:
   - Rotates through defined `states` upon clicking, updating `session_key` or persistent block PDC.

---

## 4. Security & Combat Interruption

1. **Guaranteed Item Preservation**:
   - When closing an inventory, all valid items inside input/output slots are returned to the player.
   - If the player's inventory is full, items are safely dropped at their location (`dropItemNaturally`).
2. **Combat Interruption**:
   - Damage from any source tags players in combat.
   - Any interface marked with `allow_combat: false` or `close_on_damage: true` is immediately closed upon receiving damage.
   - Opening combat-prohibited menus is denied and informs the player of the remaining combat cooldown.
