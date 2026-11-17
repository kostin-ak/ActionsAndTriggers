# 🎨 Guide: Assembling GUIs in YAML

## 1. Concept

Menus and machine panels are declared in `plugins/ActionsAndTriggers/guis/*.yml`; no Java is required for the types supported by `YamlGuiLoader`.

- **Masks**: draw the layout with 9×N characters and map each character to a widget. The loader also accepts a flat `items` (or `widgets`) section with explicit `slot`/`x`/`y`.
- **Material IDs**: `minecraft:`, `oraxen:` and `itemsadder:` are resolved through `ItemRegistry`.
- **Title glyphs**: MiniMessage, including `<shift:...>` and `<glyph:...>`. When Oraxen is absent the loader strips `<glyph:...>` and `<shift:...>` tags from the title.
- **Item safety**: real items in `input_slot`/`output_slot` are returned to the player when the inventory closes. There is no AAT handler for server shutdown or plugin disable, so this guarantee does not extend to restarts.

## 2. File anatomy

```yaml
id: custom_bench
title: "<gradient:#FFA07A:#FF6347>❖ Engineer Workbench ❖</gradient>"
rows: 3

on_open:
  - action: "core:sound"
    sound: "block.anvil.use"

mask:
  pattern:
    - "#########"
    - "#I#+#C#=#"
    - "####X####"
  components:
    "#":
      type: slot_cover
      material: "minecraft:gray_stained_glass_pane"
    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Close</red>"
      on_click:
        - action: "core:close_gui"
```

`rows` is clamped to 1..6. `on_close` is parsed but never executed by the current listener; `target_block` is stored but not read by any runtime code. `allow_combat` and `close_on_damage` are ignored — see section 6.

## 3. Widget types available in YAML

### 3.1. Decoration

`slot_cover` (aliases `cover`, `blank`) places a physical item and blocks clicks. Fields: `material`/`item`, `fallback`. `transparent_slot` (alias `invisible_slot`) leaves the slot physically empty and still blocks clicks; `transparent: true` has the same effect on any cover.

`filler` places one item into many slots at once: `material`/`item`, `name`, `slots` (list) or `x`/`y`.

### 3.2. Buttons

```yaml
"B":
  type: button
  material: "oraxen:astral_atlas"
  name: "<gold>Teleport to Sanctuary</gold>"
  lore:
    - "<gray>Click to return</gray>"
  amount: 1
  custom_model_data: 0
  on_click:            # "actions" is accepted as an alias
    - action: "core:command"
      command: "spawn"
      as_console: false
```

An unknown `type` is treated as `button`, which makes typos in widget types hard to notice.

`cycle_button` rotates through `states`; each state has `id`, `material`/`item`, `name`, `lore`, `actions`. The current id is stored in `session_key` or `persistent_key` (PDC of the bound tile block).

`toggle` is a boolean switch with `session_key`/`persistent_key`, `default`, `on_state` and `off_state` (each with `material`, `name`, `lore`) and `on_change` actions.

### 3.3. Machine slots

```yaml
"W":
  type: input_slot
  allowed_items:
    - "minecraft:water_bucket"
  placeholder_material: "minecraft:bucket"
  placeholder_name: "<aqua>Insert Water Bucket</aqua>"
  placeholder_lore:
    - "<gray>Required for cooling.</gray>"
```

`input_slot` accepts only `allowed_items` (an empty list accepts anything) and calls `on_insert`/`on_extract`. Placeholders carry the PDC tag `actionstriggers:gui_placeholder` and cannot be taken.

`output_slot` is take-only, supports the same placeholder fields and an `on_take` hook. Inserting items and taking the placeholder are blocked.

Shift-click from the player inventory is always cancelled, so items cannot be shift-inserted into a machine slot; they must be placed with a normal click.

### 3.4. Indicators

`progress_bar` shows an idle and a running state. It supports `{percent}`, `{bar}`, `{stage}`, `{temp}`, `{status}` and PAPI in its texts. The process itself is started from Java (the built-in machine actions call `startProcess`); `on_click` runs your action list, which is where `core:cryo_freeze` is normally used.

`fluid_tank` draws a vertical column, filling bottom-up. It reads a **numeric** value from the GUI session under `level_key`; the title is rendered verbatim (no `{level}`/`{max}` substitution). YAML cannot put a number into the session, so a tank declared only in YAML will show 0 unless a plugin writes that session value.

`paged_list` has no item source in YAML; it is always empty unless populated from Java.

`tab_container` is not supported by the loader: `type: tab_container` becomes a button. Tabs must be assembled from Java.

## 4. Widget fields reference

| Type | Fields |
| :--- | :--- |
| `slot_cover`, `cover`, `blank`, `transparent_slot`, `invisible_slot` | `material`, `item`, `fallback`, `transparent` |
| `filler` | `material`, `item`, `name`, `slots` |
| `button` (and unknown types) | `material`, `item`, `name`, `lore`, `amount`, `custom_model_data`, `actions`, `on_click` |
| `input_slot` | `allowed_items`, `placeholder_material`, `placeholder_name`, `placeholder_lore`, `on_insert`, `on_extract` |
| `output_slot` | `placeholder_material`, `placeholder_name`, `placeholder_lore`, `on_take` |
| `progress_bar` | `idle_material`, `idle_name`, `idle_lore`, `running_material`, `running_name`, `running_lore`, `bar_length`, `filled_color`, `empty_color`, `on_click` |
| `toggle` | `persistent_key`, `session_key`, `default`, `on_state`, `off_state`, `on_change` |
| `cycle_button` | `persistent_key`, `session_key`, `states` |
| `fluid_tank` | `height`, `level_key`, `max_level`, `filled_material`, `empty_material`, `title` |
| `paged_list` | `width`, `height`, `prev_slot`, `next_slot`, `session_key` |

Every widget also accepts `slot` or `x`/`y`, and `condition`/`conditions` (a filter tree evaluated with the player and the bound block in context).

## 5. Complete working example: cryogenic fabricator

```yaml
id: ice_fabricator_gui
title: "<shift:-8><glyph:ice_fabricator_bg><shift:-164><gradient:#74B9FF:#0984E3>❄ Cryogenic Fabricator ❄</gradient>"
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
      placeholder_name: "<gradient:#FFA07A:#FF6347>⚡ Upgrade Module (T1-T3)</gradient>"

    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Close</red>"
      on_click:
        - action: "core:close_gui"
```

Slot numbers passed to `core:cryo_freeze` are absolute inventory slots. In this mask, water is at 10, catalyst at 12, output at 16 and the upgrade slot at 24.

## 6. Limits you should know before designing a menu

- **No GUI combat flags.** `allow_combat`/`close_on_damage` are not read. Gate the opening trigger with `core:not_in_combat` and the combat action instead. Only the hard-coded `astral_atlas` GUI is force-closed on damage.
- **No `on_close` execution.** Put cleanup into the action that closes the menu, or into the player-quit trigger.
- **No item recovery on shutdown.** Ask players to close machines before a restart, or add your own shutdown handler.
- **Mask cells are single-slot.** A multi-cell widget used inside a mask is rendered once per cell and only its first item is placed. Use `items`/`widgets` with `x`/`y` for tanks and lists.
- **Slots not covered by a mask are still protected** from taking and placing because the listener cancels clicks on slots with no visible widget. Covering them is a visual choice, not a security requirement.
- **`persistent_key` requires a bound block.** PDC is available only when `core:open_gui` was executed with a block in context and that block is a tile entity.