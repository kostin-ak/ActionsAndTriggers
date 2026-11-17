# ⚡ Руководство: Скриптинг триггеров и действий в YAML

## 1. Введение

Игровая логика (способности оружия, интерактивные предметы, реакции на мобов, периодические генераторы, ограничения в бою) описывается в `plugins/ActionsAndTriggers/triggers/*.yml`. Файлы читает `YamlTriggerLoader` при старте и по `/aat reload`.

Пайплайн:

```
Событие Bukkit / heartbeat ──> ExecutionContext ──> условия (И) ──> действия (по порядку)
```

Неизвестный ID триггера **не** сообщается в рантайме: подписка сохраняется и никогда не вызывается. Неизвестные ID действий и фильтров падают при загрузке и логируются с именем файла.

## 2. Анатомия файла скрипта

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

Список условий объединяется логическим И. Для вложенности используйте `core:and`, `core:or` и `core:not`.

## 3. Условия

Полные таблицы параметров — в `TRIGGERS_AND_ACTIONS.md`. Практические правила:

- **Сравнение строк**: `core:match` раскрывает `{...}` в `template` и сравнивает с `value` (`ignore_case` по умолчанию `true`). `core:mismatch` — отрицание.
- **Вхождение в список**: у `core:in` параметры `key` и `values`, а не `template`/`list`:
  ```yaml
  - type: "core:in"
    key: "world"
    values: ["world", "world_nether"]
  ```
- **Точное равенство**: `core:eq` сравнивает сырое значение контекста, поэтому `String` и целое число из YAML никогда не совпадут. Для ID это почти всегда не то, что нужно; предпочитайте `core:match`.
- **Числа**: есть только `core:gt` и `core:lt`, оба читают ключ как `Double`. `{damage}` подходит, `{level}` (Integer) — нет. Фильтра `core:numeric` не существует.
- **Шанс**: `core:chance` принимает `chance` от `0.0` до `1.0`. Параметра `percent` нет; пишите `chance: 0.25`.
- **Права и группы**: `core:permission`, `core:in_group`, `core:not_in_group` (`group` по умолчанию `default`; без LuckPerms проверяется право `group.<name>`).
- **Бой и кулдауны**: `core:in_combat`, `core:not_in_combat`, `core:on_cooldown`, `core:not_on_cooldown` (`key` по умолчанию `global`). `core:cooldown` — алиас `core:not_on_cooldown`, а не проверка активного кулдауна.
- **Предметы и блоки**: `core:check_item` и `core:check_block` сравнивают полные ID (`oraxen:frost_crystal`, `minecraft:blue_ice`) с `context_key` (по умолчанию `item_in_hand_id` и `block_id`). `core:has_item` считает предметы в инвентаре и принимает `material`/`item` и необязательный `amount`.
- **Логика**: `core:and`/`core:or` принимают `conditions`, `core:not` — одну мапу `condition`.
- **Остальное**: `core:always_true`, `core:has`, `core:regex`, `core:looking_at`, `core:looking_at_water`.

## 4. Действия

- **Таймеры**: `core:delay` (`ticks`/`seconds`), `core:repeat` (`times`, `interval` в тиках, `delay`, даёт `{iteration}`), `core:schedule` (`id`, `delay`), `core:cancel_schedule` (`id`), `core:set_cooldown` (`key`, `duration` в секундах).
- **GUI**: `core:open_gui` (`gui`), `core:close_gui`.
- **Станки**: `core:cryo_freeze`, `core:magma_smelt`, `core:alchemical_distill` — серверные механики, требующие открытого AAT GUI с `progress_bar`. Номера слотов настраиваются, но по умолчанию фиксированы.
- **Бой**: `core:tag_combat` (`seconds`), `core:untag_combat`.
- **Отклик и управление**: `core:command` (`command`, `as_console`), `core:message` (`text`, `type` = `chat`/`actionbar`/`title`, `subtitle`), `core:sound` (`sound`, `volume`, `pitch`), `core:cancel_event`.
- **Игрок и мир**: `core:give_item` (`material`/`item`, `amount`, `if_absent`), `core:damage` (`amount`), `core:kill`, `core:potion_effect` (`effect`, `duration` в тиках, `amplifier`, `particles`), `core:teleport` (`x`, `y`, `z`, `yaw`, `pitch`, `world`), `core:push` (`x`, `y`, `z`, `add`), `core:spawn_entity` (`entity`, `x`, `y`, `z`), `core:grant_advancement` (`advancement`).
- **Визуал**: `core:particle` (`particle`, `count`, `dx`/`dy`/`dz` как смещения, `spread_x`/`spread_y`/`spread_z`, `speed`), `core:firework` (только `power`).
- **LuckPerms**: `core:add_permission`, `core:remove_permission`, `core:set_group`.

Два параметра читаются один раз при парсинге скрипта, а не при каждом выполнении: `core:command#as_console` и `core:message#type`. Они не могут зависеть от контекста.

## 5. Практические примеры

### 5.1. Посох телепортации с проверками боя и кулдауна

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
        text: "<purple>⚡ Скачок выполнен!</purple>"
```

`core:push` читает `x`/`y`/`z`, а не `dx`/`dy`/`dz`.

### 5.2. Сообщение о перезарядке

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
        text: "<gray>Перезарядка...</gray>"
```

### 5.3. Генератор на фоновом таймере

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

В контексте таймера нет игрока. Любое действие, которому нужен `{player}` (`core:message`, `core:particle`, `core:push`, …), вернёт `false` и ничего не сделает.

### 5.4. Взаимодействие с блоком по полному ID

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

`core:player_interact` даёт `{clicked_block_id}` (а не `{block_id}`) и `{block_material}`. Вариант с материалом даёт `BLUE_ICE`, вариант с id — `minecraft:blue_ice`; для `core:check_block` понадобился бы `context_key: "clicked_block_id"`, потому что по умолчанию он читает `block_id`. Держитесь одного стиля.

## 6. Поведение плейсхолдеров, которое нужно знать

- Неизвестные токены удаляются, а не сохраняются. Опечатка вроде `{block_type}` превращается в пустую строку, и условие может молча никогда не проходить.
- `{player}` — имя игрока; поддерживаются `{player.name}`, `{player.uuid}`, `{player.health}`, `{player.combat_remaining}`, `{player.group}`, `{player.prefix}`.
- `{location.x}`/`y`/`z` и `{location.world}` работают для ключей локации; `{item.type}`/`{item.amount}` — для предметов.
- Синтаксис PAPI `%placeholder%` работает в текстах действий при установленном PlaceholderAPI. Скобочный синтаксис `{placeholder}` пробуется через PAPI только если у AAT нет контекстного ключа с таким именем; нераскрытые скобки остаются как есть.
- `core:repeat` задаёт `{iteration}`; `core:interval` задаёт `{tick}`.

## 7. Перезагрузка

`/aat reload` заново читает `triggers/` и `guis/`, перезагружает языковой файл и сначала отписывает все скрипты AAT. Действия, фильтры и провайдеры регистрируются один раз при старте и не пересканируются, поэтому новое кастомное действие требует перезапуска.
