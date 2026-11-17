# ⚡ Guide: Scripting Triggers and Actions in YAML

## 1. Introduction

Gameplay logic (weapon abilities, interactive items, mob reactions, periodic generators, combat restrictions) lives in `plugins/ActionsAndTriggers/triggers/*.yml`. Files are read by `YamlTriggerLoader` at startup and on `/aat reload`.

Pipeline:

```
Bukkit event / heartbeat ──> ExecutionContext ──> conditions (AND) ──> actions (sequential)
```

An unknown trigger, action or filter ID is **not** reported at runtime for triggers: the subscription is stored and never dispatched. Unknown action or filter IDs fail during load and are logged with the file name.

## 2. Anatomy of a script file

```yaml
triggers:
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "oraxen:lightning_wand"
      - type: "core:not_on_cooldown"
        key: "wand_strike"
    actions:
      - action: "core:set_cooldown"
        key: "wand_strike"
        duration: 5
      - action: "core:sound"
        sound: "entity.lightning_bolt.thunder"
      - action: "core:command"
        command: "summon lightning_bolt {location.x} {location.y} {location.z}"
        as_console: true
```

The list of conditions is combined with logical AND. Use `core:and`, `core:or` and `core:not` for nesting.

## 3. Conditions

Full parameter tables are in `TRIGGERS_AND_ACTIONS.md`. The practical rules:

- **String matching**: `core:match` resolves `{...}` in `template` and compares it to `value` (`ignore_case` defaults to `true`). `core:mismatch` is its negation.
- **Membership**: `core:in` uses `key` and `values`, not `template`/`list`:
  ```yaml
  - type: "core:in"
    key: "world"
    values: ["world", "world_nether"]
  ```
- **Exact equality**: `core:eq` compares the raw context value, so a `String` value and an `Integer` YAML scalar never match. It is rarely what you want for IDs; prefer `core:match`.
- **Numbers**: only `core:gt` and `core:lt` exist, and both read the key as `Double`. `{damage}` works; `{level}` (Integer) does not. There is no `core:numeric`.
- **Chance**: `core:chance` accepts `chance` from `0.0` to `1.0`. There is no `percent` parameter; write `chance: 0.25`.
- **Permissions and groups**: `core:permission`, `core:in_group`, `core:not_in_group` (`group` defaults to `default`; without LuckPerms the group check becomes the `group.<name>` permission).
- **Combat and cooldowns**: `core:in_combat`, `core:not_in_combat`, `core:on_cooldown`, `core:not_on_cooldown` (`key` defaults to `global`). `core:cooldown` is an alias of `core:not_on_cooldown`, not a check for an active cooldown.
- **Items and blocks**: `core:check_item` and `core:check_block` compare full IDs (`oraxen:frost_crystal`, `minecraft:blue_ice`) against `context_key` (defaults `item_in_hand_id` and `block_id`). `core:has_item` counts items in the inventory and takes `material`/`item` plus optional `amount`.
- **Logic**: `core:and`/`core:or` take `conditions`, `core:not` takes a single `condition` map.
- **Other**: `core:always_true`, `core:has`, `core:regex`, `core:looking_at`, `core:looking_at_water`.

## 4. Actions

- **Timers**: `core:delay` (`ticks`/`seconds`), `core:repeat` (`times`, `interval` in ticks, `delay`, exposes `{iteration}`), `core:schedule` (`id`, `delay`), `core:cancel_schedule` (`id`), `core:set_cooldown` (`key`, `duration` in seconds).
- **GUI**: `core:open_gui` (`gui`), `core:close_gui`.
- **Machines**: `core:cryo_freeze`, `core:magma_smelt`, `core:alchemical_distill` — server-specific mechanics that require an open AAT GUI with a `progress_bar`. Their slot numbers are configurable but default to fixed values.
- **Combat**: `core:tag_combat` (`seconds`), `core:untag_combat`.
- **Feedback and control**: `core:command` (`command`, `as_console`), `core:message` (`text`, `type` = `chat`/`actionbar`/`title`, `subtitle`), `core:sound` (`sound`, `volume`, `pitch`), `core:cancel_event`.
- **Player and world**: `core:give_item` (`material`/`item`, `amount`, `if_absent`), `core:damage` (`amount`), `core:kill`, `core:potion_effect` (`effect`, `duration` in ticks, `amplifier`, `particles`), `core:teleport` (`x`, `y`, `z`, `yaw`, `pitch`, `world`), `core:push` (`x`, `y`, `z`, `add`), `core:spawn_entity` (`entity`, `x`, `y`, `z`), `core:grant_advancement` (`advancement`).
- **Visuals**: `core:particle` (`particle`, `count`, `dx`/`dy`/`dz` as offsets, `spread_x`/`spread_y`/`spread_z`, `speed`), `core:firework` (`power` only).
- **LuckPerms**: `core:add_permission`, `core:remove_permission`, `core:set_group`.

Two parameters are read once when the script is parsed, not on every execution: `core:command#as_console` and `core:message#type`. They cannot depend on the context.

## 5. Practical examples

### 5.1. Blink staff with combat and cooldown guards

```yaml
triggers:
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "oraxen:blink_staff"
      - type: "core:not_in_combat"
      - type: "core:not_on_cooldown"
        key: "blink"
    actions:
      - action: "core:set_cooldown"
        key: "blink"
        duration: 8
      - action: "core:sound"
        sound: "entity.enderman.teleport"
      - action: "core:particle"
        particle: "PORTAL"
        count: 50
      - action: "core:push"
        x: 0.0
        y: 0.5
        z: 0.0
      - action: "core:message"
        type: "actionbar"
        text: "<purple>⚡ Blinked!</purple>"
```

`core:push` reads `x`/`y`/`z`, not `dx`/`dy`/`dz`.

### 5.2. Cooldown feedback

```yaml
triggers:
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "oraxen:blink_staff"
      - type: "core:on_cooldown"
        key: "blink"
    actions:
      - action: "core:message"
        type: "actionbar"
        text: "<gray>Recharging...</gray>"
```

### 5.3. Heartbeat generator

```yaml
triggers:
  - trigger: "core:interval"
    actions:
      - action: "core:particle"
        particle: "END_ROD"
        count: 5
        dx: 0.2
        dy: 0.5
        dz: 0.2
```

The heartbeat has no player in the context. Any action that requires `{player}` (`core:message`, `core:particle`, `core:push`, …) returns `false` and does nothing.

### 5.4. Block interaction with a full block ID

```yaml
triggers:
  - trigger: "core:player_interact"
    conditions:
      - type: "core:has"
        key: "block"
      - type: "core:match"
        template: "{clicked_block_id}"
        value: "minecraft:blue_ice"
      - type: "core:not_in_combat"
    actions:
      - action: "core:open_gui"
        gui: "ice_fabricator_gui"
```

`core:player_interact` provides `{clicked_block_id}` (not `{block_id}`) and `{block_material}`. The material form yields `BLUE_ICE`, the id form yields `minecraft:blue_ice`; `core:check_block` would need `context_key: "clicked_block_id"` because its default is `block_id`. Use one style consistently.

## 6. Placeholder behaviour you must know

- Unknown tokens are erased, not preserved. A typo such as `{block_type}` becomes an empty string, so the condition may silently never pass.
- `{player}` is the player name; `{player.name}`, `{player.uuid}`, `{player.health}`, `{player.combat_remaining}`, `{player.group}`, `{player.prefix}` are supported.
- `{location.x}`/`y`/`z` and `{location.world}` work for location keys; `{item.type}`/`{item.amount}` for item keys.
- PAPI `%placeholder%` syntax works in action text when PlaceholderAPI is installed. Bracket syntax `{placeholder}` is attempted through PAPI only if AAT has no context key with that name, and unresolved brackets stay as-is.
- `core:repeat` sets `{iteration}`; `core:interval` sets `{tick}`.

## 7. Reloading

`/aat reload` re-reads `triggers/` and `guis/`, reloads the language file and unsubscribes all AAT scripts first. Actions, filters and providers are registered once at startup and are not re-scanned, so a newly added custom action requires a restart.
