# ⚡ Comprehensive Guide: Scripting Triggers & Actions via YAML

## 1. Introduction
With **ActionsAndTriggers (AAT)**, all gameplay logic (weapon abilities, interactive quest items, mob drop mechanics, periodic resource generators, combat interrupters) is defined via YAML files located in `plugins/ActionsAndTriggers/triggers/*.yml`.

### Execution Pipeline:
```
Bukkit Event / Heartbeat ──> ExecutionContext ──> Filter Pipeline ──(All match?)──> Action Pipeline
```

---

## 2. Anatomy of a Script File (`triggers/*.yml`)

```yaml
triggers:
  - trigger: "core:player_interact"  # Trigger event identifier
    conditions:                      # Condition list (evaluated as logical AND)
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "oraxen:lightning_wand"
      - type: "core:not_on_cooldown"
        key: "wand_strike"
    actions:                         # Action list (executed sequentially)
      - action: "core:set_cooldown"
        key: "wand_strike"
        duration: 5
      - action: "core:sound"
        sound: "entity.lightning_bolt.thunder"
      - action: "core:command"
        command: "summon lightning_bolt {location.x} {location.y} {location.z}"
        as_console: true
```

---

## 3. Complete Reference: Filters & Conditions

### 3.1. Logical Operators
- **`core:and`**: Nested list of conditions. Evaluates to true only if all pass.
  ```yaml
  - type: "core:and"
    conditions:
      - type: "core:permission"
        permission: "vip.access"
      - type: "core:not_in_combat"
  ```
- **`core:or`**: Evaluates to true if at least one nested condition passes.
- **`core:not`**: Inverts the child condition.
  ```yaml
  - type: "core:not"
    condition:
      type: "core:in_combat"
  ```
- **`core:chance`**: Probability gate (from 0.0 to 1.0, or `percent: 1..100`).
  ```yaml
  - type: "core:chance"
    percent: 25   # 25% chance
  ```

### 3.2. Combat Tracker & Cooldowns
- **`core:in_combat`**: True if player took or inflicted damage within $N$ seconds.
- **`core:not_in_combat`**: True if player is safe.
- **`core:on_cooldown`**: Player currently has an active cooldown for `key: "name"`.
- **`core:not_on_cooldown`**: Cooldown has elapsed or was never applied.

### 3.3. LuckPerms Permissions & Groups (Soft-Dependency)
- **`core:permission`**: Checks permission node (`permission: "custom.perm"`).
- **`core:in_group`**: Checks LuckPerms group inheritance (`group: "admin"`).
- **`core:not_in_group`**: Checks lack of group membership.

### 3.4. String & Numeric Comparisons
- **`core:match`**: Pattern matching (`template: "{item_in_hand_id}", value: "oraxen:astral_atlas"`).
- **`core:in`**: Collection membership (`template: "{world}", list: ["world", "world_nether"]`).
- **`core:numeric`**: Numeric comparisons (`template: "{player.health}", op: "<=", value: 6.0`). Operators: `==`, `!=`, `>`, `>=`, `<`, `<=`.

---

## 4. Complete Reference: Core Actions

### 4.1. Timers, Delays & Loops
- **`core:delay`**: Executes nested actions after a specified delay with isolated context.
  ```yaml
  - action: "core:delay"
    seconds: 3
    actions:
      - action: "core:message"
        text: "<green>Recharge complete!</green>"
  ```
- **`core:repeat`**: Repeating loop $N$ times with tick interval.
  ```yaml
  - action: "core:repeat"
    times: 5
    interval: 10   # every 0.5s
    actions:
      - action: "core:sound"
        sound: "block.note_block.harp"
      - action: "core:message"
        type: "actionbar"
        text: "<yellow>Pulse {iteration}/5</yellow>"
  ```
- **`core:schedule`** & **`core:cancel_schedule`**: Identifiable scheduled task execution and cancellation.
- **`core:set_cooldown`**: Applies player ability cooldown (`key: "ability", duration: 10`).

### 4.2. LuckPerms Permission Management
- **`core:add_permission`**: Grants permission (`permission: "essentials.fly"`).
- **`core:remove_permission`**: Revokes permission.
- **`core:set_group`**: Sets primary group (`group: "vip"`).

### 4.3. In-Game Actions
- **`core:open_gui`**: Opens container interface (`gui: "ice_fabricator_gui"`).
- **`core:close_gui`**: Closes container.
- **`core:message`**: Sends formatted feedback (`type: chat/actionbar/title`, `text: "..."`, `subtitle: "..."`).
- **`core:sound`**: Plays audio effects (`sound: "...", volume: 1.0, pitch: 1.0`).
- **`core:command`**: Dispatches command (`command: "...", as_console: true`).
- **`core:particle`**: Spawns visual particles (`particle: "FLAME", count: 20, speed: 0.1`).
- **`core:teleport`**: Teleports player (`world: "lobby", x: 0.5, y: 100, z: 0.5`).
- **`core:cancel_event`**: Cancels the underlying Bukkit event.

---

## 5. Practical Production Examples

### Example 1: Blink Staff with Combat & Cooldown Protection
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
        dy: 0.5
      - action: "core:message"
        type: "actionbar"
        text: "<purple>⚡ Blinked forward!</purple>"
```

### Example 2: Ambient Heartbeat Generator (`core:interval`)
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
