# ⚡ AAT Registry: Triggers, Filters & Actions

This document is the **authoritative reference** for the components registered by `ActionsTriggers#onEnable`, `DefaultActionParsers` and `DefaultFilterParsers`. Everything listed here exists in the current code. Anything not listed here is not registered, even if an older revision of the documentation mentioned it.

> Behavior reference: `aat-core/src/main/java/kostin/ak/actionstriggers/core/CoreTriggerKeys.java`, `core/defaults/actions/DefaultActionParsers.java`, `core/defaults/filters/DefaultFilterParsers.java`.

---

## 1. Triggers

All triggers are registered in `ActionsTriggers#onEnable`. A trigger ID is the exact string that must be used in the `trigger:` key of a YAML script.

| Trigger ID | Bukkit event / mechanism | Context keys provided | Notes |
| :--- | :--- | :--- | :--- |
| `core:interval` | Core heartbeat (`runTaskTimer`, 20 ticks) | `tick` (`Long`) | Fires once per second. Does not allocate a context when no script subscribes. |
| `core:player_interact` | `PlayerInteractEvent` | `player`, `action`, `button`, `item_in_hand_id`, `has_block`, `block`, `block_material`, `clicked_block_id`, `location` | Listens with `ignoreCancelled = false`. Fires for both hands; the plugin does not filter `EquipmentSlot`, so an off-hand interaction can produce a second dispatch. |
| `core:player_world_change` | `PlayerChangedWorldEvent` | `player`, `from_world`, `to_world`, `world`, `world_name`, `location` | `from_world`/`to_world` are world names (`String`). |
| `core:player_join` | `PlayerJoinEvent` | `player`, `location` | No join message in the context. |
| `core:player_quit` | `PlayerQuitEvent` | `player`, `location` | No quit message in the context. |
| `core:block_break` | `BlockBreakEvent` | `player`, `block`, `block_material`, `location`, `item_in_hand`, `block_id`, `item_in_hand_id` | `ignoreCancelled = true`. |
| `core:block_place` | `BlockPlaceEvent` | `player`, `block`, `block_material`, `location`, `block_id`, `item_in_hand_id` | The block key holds the **placed** block. |
| `core:block_damage` | `BlockDamageEvent` | `player`, `block`, `block_material`, `location`, `item_in_hand`, `block_id`, `item_in_hand_id` | Fires when digging starts. |
| `core:player_chat` | `AsyncChatEvent` | `player`, `location`, `message` | The event is asynchronous: do not touch the world or the inventory from the actions of this trigger. |
| `core:player_damage` | `EntityDamageEvent` (players only) | `player`, `damage`, `location`, `damage_cause`, `damager` | `damager` is set only for `EntityDamageByEntityEvent`. Fires for every damage cause, including fall/fire. |
| `core:player_death` | `PlayerDeathEvent` | `player`, `location`, `damage_source`, `item_drops` | There is **no** `killer`, `death_message` or `entity_type` key. |
| `core:entity_death` | `EntityDeathEvent` | `entity`, `location`, `player`, `killer`, `item_in_hand` | `player` is wired to `getKiller()`: it is the killer, not the victim. May be `null`. |
| `core:player_consume` | `PlayerItemConsumeEvent` | `player`, `item`, `location`, `item_id` | — |
| `core:drop_item` | `PlayerDropItemEvent` | `player`, `location`, `item`, `item_id` | No `item_amount` key. |
| `core:swap_items` | `PlayerSwapHandItemsEvent` | `player`, `location`, `item_in_hand`, `main_hand_item_id`, `off_hand_item_id` | `item_in_hand` holds the main-hand item. |
| `core:player_sneak` | `PlayerToggleSneakEvent` | `player`, `location` | Fires **only on entering the sneak state** (`if (e.isSneaking())`). No `is_sneaking` key. |
| `core:player_flight` | `PlayerToggleFlightEvent` | `player`, `location`, `is_flying` | — |
| `core:player_jump` | `com.destroystokyo.paper.event.player.PlayerJumpEvent` | `player`, `location` | `location` is the origin (`getFrom()`). |
| `core:player_level_change` | `PlayerLevelChangeEvent` | `player`, `level` | `level` is the **new** level. There is no `old_level`. |
| `core:item_craft` | `CraftItemEvent` | `player`, `item`, `location`, `item_id` | `item` is the recipe result. There is no `recipe_result_amount`. |
| `core:advancement_done` | `PlayerAdvancementDoneEvent` | `player`, `location`, `advancement_key` | Key format is `namespace:path`, e.g. `minecraft:story/shiny_gear`. |

### Deprecated or non-existent trigger IDs

These strings appear in older docs and examples but are **not** registered: `core:async_chat`, `core:player_toggle_sneak`, `core:player_toggle_flight`, `core:craft_item`, `core:player_advancement_done`, `core:player_swap_hand_items`, `core:player_drop_item`, `core:block_click`, `core:player_leave`.

Subscribing to an unknown ID does not throw: `TriggerRegistry` simply stores a subscription that nothing ever dispatches. Such a script fails silently.

---

## 2. Filters (conditions)

A list of conditions is combined with logical **AND**. Nested lists require the logical filters below.

| Filter ID | Parameters (as read by the code) | Semantics |
| :--- | :--- | :--- |
| `core:always_true` | — | Always passes. |
| `core:in_combat` | — | Player is currently combat-tagged (`CombatTracker`). |
| `core:not_in_combat` | — | Player is not combat-tagged. |
| `core:on_cooldown` | `key` (default `global`) | Cooldown is active for the executing player. |
| `core:not_on_cooldown` | `key` (default `global`) | Cooldown is absent or expired. |
| `core:cooldown` | `key` | Alias of `core:not_on_cooldown` (kept for backwards compatibility). |
| `core:permission` | `permission` | `Player#hasPermission`. |
| `core:in_group` | `group` (default `default`) | LuckPerms group membership; falls back to `group.<name>` permission when LuckPerms is absent. |
| `core:not_in_group` | `group` | Negation of `core:in_group`. |
| `core:chance` | `chance` (0.0–1.0, default `1.0`) | Random gate. There is **no** `percent` parameter. |
| `core:looking_at` | `material` (Bukkit `Material`, default `STONE`), `distance` (default `5`) | Player's target block matches the material. |
| `core:looking_at_water` | — | Target block within 5 blocks is water (fluid collision mode `ALWAYS`). |
| `core:has` | `key` | Context contains the key with the requested type. |
| `core:eq` | `key`, `value` | Exact equality against the raw context value. Type must match (an `Integer` value will not equal a `String`). |
| `core:in` | `key`, `values` (a YAML list) | Context value is contained in the list. Note the parameter names: `key`/`values`, not `template`/`list`. |
| `core:gt` | `key`, `value` | Numeric `>`; reads the key as `Double`, so `Integer` keys (e.g. `level`) always fail. |
| `core:lt` | `key`, `value` | Numeric `<`; same `Double` limitation. |
| `core:regex` | `key`, `pattern` | `String#matches` (the whole string must match). |
| `core:match` | `template`, `value`, `ignore_case` (default `true`) | Resolves `{placeholders}` in `template` and compares it with `value`. |
| `core:mismatch` | same as `core:match` | Negation of `core:match`. |
| `core:and` | `conditions` (list) | All nested conditions pass. |
| `core:or` | `conditions` (list) | At least one nested condition passes. |
| `core:not` | `condition` (single map) | Inverts a nested condition. |
| `core:check_item` | `context_key` (default `item_in_hand_id`), `material` (string or list) | Case-insensitive comparison against full item IDs. |
| `core:check_block` | `context_key` (default `block_id`), `material` (string or list) | Case-insensitive comparison against full block IDs. |
| `core:has_item` | `material` or `item` (string or list), `amount` (default `1`) | Counts matching items in the player inventory. |
| `core:has_not_item` | same as `core:has_item` | Negation of `core:has_item`. |

`core:numeric` is **not** implemented. Use `core:gt`/`core:lt`, or `core:match` with a formatted template.

---

## 3. Actions

Actions execute sequentially. `core:cancel_event` sets the context flag; `YamlTriggerLoader` then stops the remaining actions of that script, and `BukkitEventTrigger` cancels the underlying event if it is `Cancellable`.

### Flow control and cooldowns

| Action ID | Parameters | Notes |
| :--- | :--- | :--- |
| `core:delay` | `ticks` or `seconds` (or `delay`, in ticks), `actions` | Runs the nested actions on a cloned context. Default 20 ticks. |
| `core:repeat` | `times` (default `1`), `interval` (ticks, default `20`) or `interval_seconds`, `delay` (ticks), `actions` | Exposes `{iteration}` (1-based) in the nested context. `times <= 0` means an endless loop. |
| `core:schedule` | `id` (default `task_<millis>`), `delay` (ticks) or `seconds`, `actions` | Re-using an `id` cancels the previous task with that id. |
| `core:cancel_schedule` | `id` | Cancels a named task. |
| `core:set_cooldown` | `key` (default `global`), `duration` or `seconds` (default `5`) | Per-player cooldown, stored in memory only. |

### GUI and machines

| Action ID | Parameters | Notes |
| :--- | :--- | :--- |
| `core:open_gui` | `gui` (or `id`) | Opens a GUI from `guis/*.yml`. Passes `context.block` as the bound block. Hard-coded special case: `astral_atlas` is refused while the player is combat-tagged. |
| `core:close_gui` | — | Closes the player's inventory on the next tick. |
| `core:cryo_freeze` | `water_slot` (10), `crystal_slot` (12), `output_slot` (16), `upgrade_slot` (24) | Server-specific ice machine. Requires an open AAT GUI; if it contains a `progress_bar` the result is animated and delayed, otherwise it completes immediately. Consumes water + catalyst immediately and reads the mode from session key `fabricator_mode`. |
| `core:magma_smelt` | `orax_slot` (10), `onyx_slot` (12), `fuel_slot` (14), `output_slot` (16), `duration` (100) | Server-specific alloy machine. |
| `core:alchemical_distill` | `ingredient_slot` (10), `water_slot` (12), `output_slot` (16), `duration` (80) | Server-specific brewing machine. |

### Combat

| Action ID | Parameters | Notes |
| :--- | :--- | :--- |
| `core:tag_combat` | `seconds` (default `15`) | Applies a combat tag. |
| `core:untag_combat` | — | Removes the combat tag. |

### Player interaction and visuals

| Action ID | Parameters | Notes |
| :--- | :--- | :--- |
| `core:command` | `command`, `as_console` (default `true`) | `as_console` is evaluated when the script is parsed, not per execution. |
| `core:message` | `text`, `type` (`chat`/`actionbar`/`title`), `subtitle` | `type` is also fixed at parse time. MiniMessage is supported. |
| `core:sound` | `sound`, `volume` (1.0), `pitch` (1.0) | Sound key or legacy Bukkit name. |
| `core:give_item` | `material` (or `item`), `amount` (1), `if_absent` (or `unique`) | `if_absent` is evaluated at parse time; the item is skipped if the inventory already holds that full ID. |
| `core:damage` | `amount` (1.0) | `Player#damage`. |
| `core:kill` | — | `setHealth(0)`. |
| `core:potion_effect` | `effect` (default `speed`), `duration` (**ticks**, default 200), `amplifier` (0), `particles` (true) | `duration: 3` means 3 ticks, not 3 seconds. |
| `core:particle` | `particle` (enum name, default `FLAME`), `count` (10), `dx`/`dy`/`dz` (offsets, `dy` defaults to 1.0), `spread_x`/`spread_y`/`spread_z` (0.5), `speed` (0.0) | `dx/dy/dz` are positional offsets here. |
| `core:firework` | `power` (1) | The effect is hard-coded: red/yellow with orange fade, `BALL_LARGE`, trail. `type` and `colors` are ignored. |
| `core:teleport` | `x`, `y`, `z`, `yaw`, `pitch`, `world` | Missing coordinates default to the player's current position; an unknown world falls back to the current one. |
| `core:push` | `x` (0.0), `y` (0.5), `z` (0.0), `add` (false) | `add: true` adds to the current velocity, otherwise it is replaced. `dx/dy/dz` are **not** read here. |
| `core:spawn_entity` | `entity` (Bukkit `EntityType`, default `ZOMBIE`), `x`, `y`, `z` | `LIGHTNING_BOLT` is spawned via `strikeLightning`. |
| `core:grant_advancement` | `advancement` (namespaced key) | Awards all remaining criteria. |
| `core:cancel_event` | — | Cancels the parent Bukkit event and stops the rest of the action chain. |

### LuckPerms

| Action ID | Parameters | Notes |
| :--- | :--- | :--- |
| `core:add_permission` | `permission` | No-op when LuckPerms is not installed. |
| `core:remove_permission` | `permission` | No-op without LuckPerms. |
| `core:set_group` | `group` (default `default`) | Sets the primary group and adds an inheritance node. No-op without LuckPerms. |

---

## 4. Context placeholders

Placeholders are resolved by `ContextPlaceholderParser` (linear scan, no regex) in any string that goes through `ActionParameters`, and by `PapiHook` in GUI text.

### Built-in format

- `{key}` resolves the raw context value through a type formatter.
- `{key.property}` resolves a property, e.g. `{player.name}`, `{location.x}`, `{block.type}`.
- Keys are typed. Context values that have no dedicated formatter fall back to `toString()`, so `{block_material}` yields `BLUE_ICE` and `{action}` yields `RIGHT_CLICK_BLOCK`.
- **An unknown token is removed from the string** (it is replaced with nothing), not preserved. A condition built on a misspelled key silently compares against an empty string and fails.

### Supported properties

`player`: `name`, `uuid`, `health`, `combat_remaining`, `combat_seconds`, `combat_time`, `combat`, `group`, `prefix`, `suffix`, `weight`.
`location`: `x`, `y`, `z`, `block_x`, `block_y`, `block_z`, `world`.
`block`: `type`, `x`, `y`, `z`, `world`.
`entity`: `name`, `type`, `uuid`.
`item` (`ItemStack`): `type`, `amount`.

Common keys per trigger are listed in section 1. `{iteration}` is provided by `core:repeat` and `{tick}` by `core:interval`.

### PlaceholderAPI

- Standard PAPI syntax (`%player_name%`) is expanded through `PlaceholderAPI#setPlaceholders` when the plugin is installed.
- Bracket syntax (`{player_name}`) is converted to `%player_name%` and expanded **only if PAPI resolves it**; otherwise the braces are left in place. This means bracket syntax and AAT context syntax share the same `{...}` form: if a context key and a PAPI placeholder have the same name, the context value wins in action parameters.
- For GUI widget text, PAPI is applied by the widget itself (`ButtonWidget`, `InputSlotWidget`, `OutputSlotWidget`, `ProgressBarWidget`, `CycleButtonWidget`). `ToggleWidget` and `FluidTankWidget` do **not** run PAPI.

---

## 5. What is intentionally missing

These are frequently requested but not implemented in the current code; see `docs/en/POTENTIAL_ISSUES.md` for the full list and workarounds:

- Numeric comparison filter (`core:numeric`).
- Percent syntax for `core:chance`.
- Firework colour/type parameters.
- Configurable GUI combat flags (`allow_combat`, `close_on_damage`).
- YAML support for `tab_container`, item sources for `paged_list`, and numeric sources for `fluid_tank`.
- Persistence of cooldowns, combat tags and named tasks across restarts.
