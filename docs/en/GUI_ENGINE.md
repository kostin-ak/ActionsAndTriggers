# 🖥️ AAT Widget-Oriented GUI Engine

## 1. Architectural concept

The GUI engine is a composite widget tree rendered into a Bukkit inventory. Instead of hard-coding slot numbers, layouts are described with a 9×N character mask.

- **Grid**: widgets use `x` (0..8) and `y` (0..rows-1); an absolute slot is `y * 9 + x`. A widget may also be placed with a single `slot` field.
- **Mask**: `mask.pattern` is a list of 9-character strings; `mask.components` maps each character to a widget definition. `MaskWidget` unrolls the mask into a `slot → widget` map used for clicks and for item return on close.
- **Rendering**: on open (and on `GuiContext#refreshGui`), every widget writes items into a `Map<Integer, ItemStack>`; the registry then writes that matrix into the inventory. Only `on_open` actions are executed automatically; `on_close` actions are parsed but never invoked by `GuiListener`.
- **Material resolution**: every `material`/`item` field accepts `minecraft:<id>`, `oraxen:<id>` or `itemsadder:<id>` and is resolved through `ItemRegistry`. Unknown IDs fall back to a vanilla placeholder material.

## 2. YAML structure (`guis/*.yml`)

```yaml
id: custom_bench          # used by core:open_gui
title: "<gradient:#FFA07A:#FF6347>Engineer Workbench</gradient>"
rows: 3                   # clamped to 1..6
# target_block: "some_block_id"   # parsed and stored, but no runtime code reads it

on_open:                  # optional: executed right after the inventory is opened
  - action: "core:sound"
    sound: "block.anvil.use"
on_close:                 # optional: PARSED but NOT executed (see section 6)
  - action: "core:message"
    text: "closed"

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

# Alternative to a mask: a flat list of widgets with explicit coordinates.
items:                    # "widgets" is accepted as an alias
  close_button:
    type: button
    slot: 22
    material: "minecraft:barrier"
    name: "<red>Close</red>"
    on_click:
      - action: "core:close_gui"
```

`allow_combat` and `close_on_damage` exist as fields on `GuiDefinition`, but `YamlGuiLoader` never reads them and no runtime check uses them. Do not rely on them; see `POTENTIAL_ISSUES.md`.

## 3. Widgets configurable from YAML

| `type` value | Fields read by `YamlGuiLoader` | Behaviour |
| :--- | :--- | :--- |
| `slot_cover`, `cover`, `blank`, `transparent_slot`, `invisible_slot` | `material`/`item`, `fallback`, `transparent` | Decorative, non-clickable. `transparent_slot` renders nothing at all (the slot stays empty). Without `material`/`item` the widget falls back to a gray glass pane; the optional `gui.slot_cover.*` entries in `config.yml` are consulted next. |
| `filler` | `material`/`item`, `name`, `slots`, `x`/`y` | Places the same item into the listed slots (or one slot at `x`/`y`). |
| `button`, any unknown `type` | `material`/`item`, `name`, `lore`, `amount`, `custom_model_data`, `actions`/`on_click` | Standard button. An unrecognised `type` silently becomes a button. |
| `input_slot` | `allowed_items`, `placeholder_material`, `placeholder_name`, `placeholder_lore`, `on_insert`, `on_extract` | Accepts only listed items (empty list = any item). Renders a placeholder tagged with PDC `actionstriggers:gui_placeholder`, which cannot be taken. |
| `output_slot` | `placeholder_material`, `placeholder_name`, `placeholder_lore`, `on_take` | Take-only. Insertion and taking the placeholder are blocked. |
| `progress_bar` | `idle_material`/`material`, `idle_name`/`name`, `idle_lore`/`lore`, `running_material`, `running_name`, `running_lore`, `bar_length`, `filled_color`, `empty_color`, `on_click` | Two-state indicator. The running state is driven by `ProgressBarWidget#startProcess`, which is called by the built-in machine actions. Supports `{percent}`, `{bar}`, `{stage}`, `{temp}`, `{status}` and PAPI. |
| `toggle` | `persistent_key`, `session_key`, `default`, `on_state` (`material`, `name`, `lore`), `off_state` (`material`, `name`, `lore`), `on_change` | Boolean switch stored in block PDC (`persistent_key`, requires a bound tile block) or in the GUI session (`session_key`). |
| `cycle_button` | `persistent_key`, `session_key`, `states` (`id`, `material`/`item`, `name`, `lore`, `actions`) | Rotates through states on click. The state is stored as a **String** id. |
| `fluid_tank` | `height`, `level_key`, `max_level`, `filled_material`, `empty_material`, `title` | Vertical gauge filled bottom-up. Reads a **numeric** value from the GUI session only; `title` is rendered verbatim (no `{level}` substitution). |
| `paged_list` | `width`, `height`, `prev_slot`, `next_slot`, `session_key` | Pagination frame. `YamlGuiLoader` has no way to supply items, so a YAML-declared paged list is always empty. |

Common fields for any widget: `x`, `y` or `slot`, and `condition`/`conditions` (a filter tree evaluated against the player and the bound block).

## 4. Widgets available only from Java

`TabContainerWidget` is fully implemented but there is no `tab_container` branch in `YamlGuiLoader`. A YAML entry with `type: tab_container` falls through to `button`. Tabs, like `PagedListWidget` item sources, must be assembled in code:

```java
TabContainerWidget tabs = new TabContainerWidget();
tabs.addTab("general", 0, iconA, List.of(widgetA));
tabs.addTab("upgrades", 1, iconB, List.of(widgetB));
```

## 5. Session state and block data

- Each open inventory gets an `AATGuiHolder` with a `sessionState` map and a `slotWidgets` map.
- `GuiContext` exposes typed session helpers (`getState`/`setState`) and block PDC helpers (`getPdcString/Int/Boolean` and setters).
- PDC helpers work only when the GUI was opened with a bound block and that block's state is a `TileState` (chest, furnace, barrel, …). Otherwise they are silent no-ops.
- `GuiContext#refreshGui` asks `GuiRegistry` to re-render the whole inventory; widgets such as `CycleButtonWidget` and `ToggleWidget` also repaint their own slot immediately after a click.

## 6. Clicks, drags and item safety

`GuiListener` handles `InventoryClickEvent`, `InventoryDragEvent` and `InventoryCloseEvent` at `HIGHEST` priority.

- Clicks in slots that no visible widget occupies are cancelled.
- Drags touching the top inventory are cancelled.
- Shift-clicks from the player inventory are always cancelled, so items cannot be shift-inserted into an input slot.
- On close, items in `input_slot` and `output_slot` that are not placeholders are moved to the player inventory; leftovers are dropped with `dropItemNaturally`.
- Item return is implemented **only** for `InventoryCloseEvent`. There is no handler for plugin disable, server shutdown, or `close_on_damage`; items left in a machine GUI during a restart are not recovered by AAT.
- `on_close` actions in a GUI definition are parsed but never executed.

## 7. Text and placeholders

- `ButtonWidget` replaces `{player}`, the well-known progress keys and **all session keys** (`{<sessionKey>}`), then runs PlaceholderAPI.
- `CycleButtonWidget`, `InputSlotWidget`, `OutputSlotWidget` and `ProgressBarWidget` run PlaceholderAPI but do not substitute session keys (beyond each widget's own template variables).
- `ToggleWidget` and `FluidTankWidget` do not run PlaceholderAPI at all.
- Progress-bar templates support `{percent}`, `{bar}`, `{stage}`, `{temp}`, `{status}`; the default stages are the built-in cryo stages unless custom stages were added from Java.

## 8. Known limitations

The GUI engine currently supports fewer YAML features than older documentation claimed. In short: no YAML tabs, no YAML item source for paged lists, no working numeric fluid tank from YAML, no GUI combat flags, no `on_close` execution, no item recovery on shutdown. Details, impact and suggested fixes are collected in `docs/en/POTENTIAL_ISSUES.md`.
