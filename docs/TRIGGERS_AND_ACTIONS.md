# ⚡ Справочник Триггеров, Фильтров и Действий AAT

## 1. Триггеры (`triggers: [ - trigger: "..." ]`)

| Ключ триггера | Событие Bukkit | Доступные ключи контекста | Описание |
| :--- | :--- | :--- | :--- |
| `core:player_interact` | `PlayerInteractEvent` | `{player}`, `{item_in_hand_id}`, `{action}`, `{button}`, `{block_type}`, `{location}` | Клик ПКМ/ЛКМ по воздуху или блоку. |
| `core:player_world_change` | `PlayerChangedWorldEvent` | `{player}`, `{from_world}`, `{to_world}`, `{world}`, `{location}` | Перемещение игрока между мирами сервера. |
| `core:player_join` | `PlayerJoinEvent` | `{player}`, `{world}`, `{location}` | Вход игрока на сервер. |
| `core:block_break` | `BlockBreakEvent` | `{player}`, `{block_type}`, `{location}`, `{item_in_hand_id}` | Разрушение блока игроком. |
| `core:async_chat` | `AsyncChatEvent` | `{player}`, `{message}`, `{world}` | Отправка сообщения в чат. |

---

## 2. Фильтры / Условия (`conditions: [ - type: "..." ]`)

| Ключ фильтра | Параметры | Описание |
| :--- | :--- | :--- |
| `core:match` | `template`, `value`, `ignore_case` | Проверка совпадения строк/плейсхолдеров (например, `{to_world}` = `creative`). |
| `core:has_item` | `material` (или `item`), `amount` | Проверяет наличие предмета (Vanilla, Oraxen, ItemsAdder) в инвентаре. |
| `core:has_not_item` | `material` (или `item`) | Истинно, если указанного предмета **нет** в инвентаре игрока. |
| `core:in_world` | `world` | Проверяет, находится ли игрок в указанном мире. |
| `core:has_permission` | `permission` | Проверяет наличие права Bukkit / LuckPerms. |
| `core:sneaking` | `value: true/false` | Проверяет, зажат ли у игрока Shift. |

---

## 3. Действия (`actions: [ - action: "..." ]`)

| Ключ действия | Параметры | Описание |
| :--- | :--- | :--- |
| `core:open_gui` | `gui` (ID меню) | Открывает виджето-ориентированный GUI из папки `guis/*.yml`. |
| `core:command` | `command`, `as_console` (true/false) | Выполняет команду от имени консоли или игрока. Поддерживает `{player}`. |
| `core:give_item` | `material` (или `item`), `amount`, `if_absent` | Выдает предмет в инвентарь. При `if_absent: true` выдает только если предмета еще нет. |
| `core:sound` | `sound`, `volume`, `pitch` | Проигрывает звук игроку или в локации. |
| `core:particle` | `particle`, `count`, `dx`, `dy`, `dz`, `speed` | Спавнит визуальные частицы. |
| `core:message` | `text`, `type: chat/actionbar/title`, `subtitle` | Отправляет форматированное сообщение (MiniMessage). |
| `core:teleport` | `world`, `x`, `y`, `z`, `yaw`, `pitch` | Телепортирует игрока по заданным координатам. |
| `core:potion_effect` | `effect`, `duration`, `amplifier`, `particles` | Накладывает эффект зелья на игрока. |
| `core:cancel_event` | — | Отменяет базовое Bukkit-событие (например, отмена разрушения блока). |
