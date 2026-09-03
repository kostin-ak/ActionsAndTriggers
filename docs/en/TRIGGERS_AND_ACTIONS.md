# ⚡ ActionsAndTriggers Registry: Triggers, Filters & Actions

This document provides a **comprehensive** catalog of all core components registered within `ActionsTriggers.java`, `DefaultActionParsers.java`, and `DefaultFilterParsers.java`.

---

## 🎯 1. Core Triggers (`triggers: [ - trigger: "..." ]`)

The framework registers **21 core triggers**:

| Trigger ID | Event Class / Mechanism | Context Keys `{key}` | Description |
| :--- | :--- | :--- | :--- |
| `core:interval` | Core Heartbeat Timer | `{tick}` | Fires every second (20 ticks) for automated machine cycles and background tasks. |
| `core:player_interact` | `PlayerInteractEvent` | `{player}`, `{action}`, `{button}`, `{has_block}`, `{block_type}`, `{item_in_hand_id}`, `{location}` | Player clicks air or block with left/right mouse button. |
| `core:player_world_change` | `PlayerChangedWorldEvent` | `{player}`, `{from_world}`, `{to_world}`, `{world}`, `{location}` | Player transitions between worlds. |
| `core:player_join` | `PlayerJoinEvent` | `{player}`, `{join_message}`, `{world}`, `{location}` | Player joins the server. |
| `core:player_quit` | `PlayerQuitEvent` | `{player}`, `{quit_message}` | Player disconnects from the server. |
| `core:block_break` | `BlockBreakEvent` | `{player}`, `{block_type}`, `{item_in_hand_id}`, `{location}` | Player breaks a block. |
| `core:block_place` | `BlockPlaceEvent` | `{player}`, `{block_type}`, `{block_placed}`, `{item_in_hand_id}`, `{location}` | Player places a block. |
| `core:block_damage` | `BlockDamageEvent` | `{player}`, `{block_type}`, `{item_in_hand_id}`, `{location}` | Player starts digging or hits a block. |
| `core:async_chat` | `AsyncChatEvent` | `{player}`, `{message}`, `{world}` | Player posts a message in chat. |
| `core:player_damage` | `EntityDamageEvent` | `{player}`, `{damage}`, `{cause}`, `{damager}` | Player takes damage (activates Combat Tag). |
| `core:player_death` | `PlayerDeathEvent` | `{player}`, `{death_message}`, `{killer}`, `{location}` | Player dies. |
| `core:entity_death` | `EntityDeathEvent` | `{entity}`, `{entity_type}`, `{killer}`, `{location}` | An entity or mob dies. |
| `core:player_consume` | `PlayerItemConsumeEvent` | `{player}`, `{item_id}` | Player consumes food or drinks a potion. |
| `core:player_drop_item` | `PlayerDropItemEvent` | `{player}`, `{item_id}`, `{item_amount}`, `{location}` | Player drops an item (Q key). |
| `core:player_swap_hand_items` | `PlayerSwapHandItemsEvent`| `{player}`, `{main_hand_item}`, `{off_hand_item}` | Player swaps hand items (F key). |
| `core:player_toggle_sneak` | `PlayerToggleSneakEvent` | `{player}`, `{is_sneaking}` | Player presses or releases Shift. |
| `core:player_toggle_flight`| `PlayerToggleFlightEvent` | `{player}`, `{is_flying}` | Player enables or disables flight. |
| `core:player_jump` | `PlayerJumpEvent` | `{player}`, `{location}` | Player jumps. |
| `core:player_level_change` | `PlayerLevelChangeEvent` | `{player}`, `{old_level}`, `{new_level}` | Player experience level changes. |
| `core:craft_item` | `CraftItemEvent` | `{player}`, `{recipe_result}`, `{recipe_result_amount}` | Player crafts an item on a workbench. |
| `core:player_advancement_done` | `PlayerAdvancementDoneEvent`| `{player}`, `{advancement}` | Player earns an advancement. |

---

## 🔍 2. Core Filters & Conditions (`conditions: [ - type: "..." ]`)

### Logical Operators & Basic Checks:
1. **`core:always_true`**: Always returns `true`.
2. **`core:and`**: Logical AND. Accepts `conditions: [...]`. True only if all child conditions pass.
3. **`core:or`**: Logical OR. True if at least one child condition passes.
4. **`core:not`**: Logical NOT. Inverts the output of the nested `condition: {...}`.
5. **`core:chance`**: Probability check. Parameters: `chance: 0.15` (0.0 to 1.0) or `percent: 15`.
6. **`core:permission`**: Checks player permissions. Parameter: `permission: "some.node"`.

### Combat Tracker & Cooldowns:
7. **`core:in_combat`**: True if the player is currently tagged in combat.
8. **`core:not_in_combat`**: True if the player is out of combat.
9. **`core:on_cooldown`**: True if the player has an active cooldown for `key: "ability_name"`.
10. **`core:not_on_cooldown`**: True if the cooldown has elapsed or was never set.

### String, Numeric & Context Comparisons:
11. **`core:eq`**: Exact equality check (`key: "world", value: "lobby"`).
12. **`core:match`**: Pattern matching with placeholder resolution (`template: "{item_in_hand_id}", value: "oraxen:astral_atlas"`).
13. **`core:in`**: Checks membership in a collection (`template: "{world}", list: ["world", "world_nether"]`).
14. **`core:numeric`**: Numeric comparisons (`template: "{player.health}", op: "<=", value: 5.0`). Operators: `==`, `!=`, `>`, `>=`, `<`, `<=`.

---

## 🚀 3. Core Actions (`actions: [ - action: "..." ]`)

### Timers, Schedulers & Cooldowns:
1. **`core:delay`**: Executes nested actions after a specified delay with isolated context.
   - Parameters: `ticks: 40` or `seconds: 2`, `actions: [...]`.
2. **`core:repeat`**: Periodically loops actions for a specified number of times.
   - Parameters: `times: 5`, `interval: 20`, `delay: 0`, `actions: [...]`.
   - Exposes `{iteration}` in nested context.
3. **`core:schedule`**: Schedules an identifiable task that can be cancelled later.
   - Parameters: `id: "task_id"`, `delay: 100`, `actions: [...]`.
4. **`core:cancel_schedule`**: Cancels a scheduled task by `id: "task_id"`.
5. **`core:set_cooldown`**: Sets a personal cooldown for the interacting player.
   - Parameters: `key: "freeze_spell"`, `duration: 10` (seconds).

### GUI & Machines:
6. **`core:open_gui`**: Opens a GUI menu (`gui: "ice_fabricator_gui"`).
7. **`core:close_gui`**: Closes the player's open container.
8. **`core:cryo_freeze`**: Complete cryogenic ice fabrication process.
   - `water_slot`: Water bucket input slot.
   - `crystal_slot`: Catalyst input slot.
   - `output_slot`: Ice extraction slot.
   - `upgrade_slot`: Accelerator slot (Tier 1 = -25%, Tier 2 = -50%, Tier 3 = -75% duration).
   - Instantly consumes inputs, animates progress bar, and dispenses ice (or drops to ground if full).

### Combat Controls:
9. **`core:tag_combat`**: Manually places player in combat (`seconds: 15`).
10. **`core:untag_combat`**: Clears combat status from player immediately.

### In-Game Actions:
11. **`core:command`**: Executes a command (`command: "say Hello"`, `as_console: true/false`).
12. **`core:message`**: Sends formatted feedback (`text`, `type: chat/actionbar/title`, `subtitle`).
13. **`core:sound`**: Plays audio effects (`sound`, `volume`, `pitch`).
14. **`core:give_item`**: Gives an item (`material`, `amount`, `if_absent: true/false`).
15. **`core:damage`**: Applies physical damage (`amount`).
16. **`core:kill`**: Immediately kills the player.
17. **`core:potion_effect`**: Applies potion status (`effect`, `duration`, `amplifier`, `particles`).
18. **`core:particle`**: Spawns visual particles (`particle`, `count`, `dx`, `dy`, `dz`, `speed`).
19. **`core:firework`**: Spawns fireworks (`power`, `type`, `colors`).
20. **`core:teleport`**: Teleports player (`world`, `x`, `y`, `z`, `yaw`, `pitch`).
21. **`core:push`**: Applies physical velocity vector (`dx`, `dy`, `dz`).
22. **`core:spawn_entity`**: Spawns an entity (`entity`, `x`, `y`, `z`).
23. **`core:grant_advancement`**: Unlocks an advancement (`advancement`).
24. **`core:cancel_event`**: Cancels the underlying Bukkit event.

---

## 🌐 4. PlaceholderAPI & Context Variables

All messages, titles, lore lines, and conditions resolve placeholders:
- Standard PAPI format: `%player_name%`, `%vault_eco_balance%`, `%server_online%`
- Bracket format: `{player_name}`, `{vault_eco_balance}`, `{statistic_time_played}`
- Built-in context variables:
  * `{player}`: Player username.
  * `{player.combat_remaining}`: Remaining combat tag duration in seconds.
  * `{iteration}`: Current iteration counter in `core:repeat`.
  * `{tick}`: Heartbeat tick counter in `core:interval`.
  * `{progress_status}`: Cryostat operational status.
  * `{progress_temp}`: Cryogenic reactor temperature.
  * `{progress_stage}`: Current fabrication phase.
  * `{progress_percent}`: Completion percentage (0–100).
