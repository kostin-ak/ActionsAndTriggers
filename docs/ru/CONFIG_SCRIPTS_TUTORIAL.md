# ⚡ Полное руководство: Скриптинг Триггеров и Действий через YAML

## 1. Введение
В **ActionsAndTriggers (AAT)** любая игровая логика (способности оружия, интерактивные предметы, реакция на смерть мобов, периодические генераторы ресурсов, защита от открытия меню в бою) описывается в файлах `plugins/ActionsAndTriggers/triggers/*.yml`.

### Конвейер обработки (Pipeline):
```
Bukkit Event / Heartbeat ──> ExecutionContext ──> Filter Pipeline ──(Все true?)──> Action Pipeline
```

---

## 2. Анатомия файла скрипта (`triggers/*.yml`)

```yaml
triggers:
  - trigger: "core:player_interact"  # Идентификатор триггера события
    conditions:                      # Список условий (выполняются как логическое "И")
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "oraxen:lightning_wand"
      - type: "core:not_on_cooldown"
        key: "wand_strike"
    actions:                         # Список действий (выполняются по порядку)
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

## 3. Подробный справочник условий (Filters & Conditions)

### 3.1. Логические операторы
- **`core:and`**: Вложенный список условий. Истинно, если все условия истинны.
  ```yaml
  - type: "core:and"
    conditions:
      - type: "core:permission"
        permission: "vip.access"
      - type: "core:not_in_combat"
  ```
- **`core:or`**: Вложенный список условий. Истинно, если хотя бы одно истинно.
- **`core:not`**: Инвертирует вложенное условие.
  ```yaml
  - type: "core:not"
    condition:
      type: "core:in_combat"
  ```
- **`core:chance`**: Проверка вероятности (от 0.0 до 1.0 или `percent: 1..100`).
  ```yaml
  - type: "core:chance"
    percent: 25   # 25% шанс срабатывания
  ```

### 3.2. Боевой режим и Кулдауны
- **`core:in_combat`**: Игрок получал или наносил урон за последние $N$ секунд.
- **`core:not_in_combat`**: Игрок в безопасности.
- **`core:on_cooldown`**: У игрока активна перезарядка способности `key: "name"`.
- **`core:not_on_cooldown`**: Перезарядка завершилась.

### 3.3. Права и Группы LuckPerms (Мягкая интеграция)
- **`core:permission`**: Проверка права у игрока (`permission: "custom.perm"`).
- **`core:in_group`**: Проверка группы LuckPerms (`group: "admin"`).
- **`core:not_in_group`**: Проверка отсутствия группы.

### 3.4. Сравнения строк и чисел
- **`core:match`**: Сравнение плейсхолдера (`template: "{item_in_hand_id}", value: "oraxen:astral_atlas"`).
- **`core:in`**: Проверка вхождения в список (`template: "{world}", list: ["world", "world_nether"]`).
- **`core:numeric`**: Числовое сравнение (`template: "{player.health}", op: "<=", value: 6.0`). Доступные операторы: `==`, `!=`, `>`, `>=`, `<`, `<=`.

---

## 4. Подробный справочник действий (Actions)

### 4.1. Таймеры, Задержки и Циклы
- **`core:delay`**: Задержка перед следующими действиями с изоляцией контекста.
  ```yaml
  - action: "core:delay"
    seconds: 3
    actions:
      - action: "core:message"
        text: "<green>Зарядка завершена!</green>"
  ```
- **`core:repeat`**: Циклическое повторение $N$ раз с шагом в тиках.
  ```yaml
  - action: "core:repeat"
    times: 5
    interval: 10   # каждые 0.5 секунды
    actions:
      - action: "core:sound"
        sound: "block.note_block.harp"
      - action: "core:message"
        type: "actionbar"
        text: "<yellow>Пульсация {iteration}/5</yellow>"
  ```
- **`core:schedule`** и **`core:cancel_schedule`**: Запуск и отмена именованных задач по ID.
- **`core:set_cooldown`**: Установка перезарядки (`key: "ability", duration: 10`).

### 4.2. Права и Группы LuckPerms
- **`core:add_permission`**: Выдача пермишена (`permission: "essentials.fly"`).
- **`core:remove_permission`**: Снятие пермишена.
- **`core:set_group`**: Смена основной группы (`group: "vip"`).

### 4.3. Базовые действия
- **`core:open_gui`**: Открытие интерфейса (`gui: "ice_fabricator_gui"`).
- **`core:close_gui`**: Закрытие меню.
- **`core:message`**: Сообщение (`type: chat/actionbar/title`, `text: "..."`, `subtitle: "..."`).
- **`core:sound`**: Звуковой эффект (`sound: "...", volume: 1.0, pitch: 1.0`).
- **`core:command`**: Консольная или пользовательская команда (`command: "...", as_console: true`).
- **`core:particle`**: Частицы (`particle: "FLAME", count: 20, speed: 0.1`).
- **`core:teleport`**: Телепортация (`world: "lobby", x: 0.5, y: 100, z: 0.5`).
- **`core:cancel_event`**: Отмена базового события Bukkit (например, предотвратить поломку блока или клик).

---

## 5. Практические примеры

### Пример 1: Посох Телепортации с проверкой боя и кулдауна
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
        text: "<purple>⚡ Скачок в пространстве!</purple>"
```

### Пример 2: Автоматический генератор (тикер `core:interval`)
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
