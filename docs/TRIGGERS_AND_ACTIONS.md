# ⚡ Полный реестр Триггеров, Фильтров и Экшенов AAT

Данный документ содержит **исчерпывающий** список всех компонентов ядра ActionsAndTriggers, зарегистрированных в `ActionsTriggers.java`, `DefaultActionParsers.java` и `DefaultFilterParsers.java`.

---

## 🎯 1. Все Триггеры ядра (`triggers: [ - trigger: "..." ]`)

Всего в ядре зарегистрировано **20 триггеров** событий Bukkit:

| ID Триггера | Класс события Bukkit | Ключи контекста в шаблонах `{key}` | Описание |
| :--- | :--- | :--- | :--- |
| `core:player_interact` | `PlayerInteractEvent` | `{player}`, `{action}`, `{button}`, `{has_block}`, `{block_type}`, `{item_in_hand_id}`, `{location}` | Клик ПКМ / ЛКМ по воздуху или блоку. |
| `core:player_world_change` | `PlayerChangedWorldEvent` | `{player}`, `{from_world}`, `{to_world}`, `{world}`, `{location}` | Переход игрока между мирами сервера. |
| `core:player_join` | `PlayerJoinEvent` | `{player}`, `{join_message}`, `{world}`, `{location}` | Вход игрока на сервер. |
| `core:player_quit` | `PlayerQuitEvent` | `{player}`, `{quit_message}` | Выход игрока с сервера. |
| `core:block_break` | `BlockBreakEvent` | `{player}`, `{block_type}`, `{item_in_hand_id}`, `{location}` | Разрушение блока игроком. |
| `core:block_place` | `BlockPlaceEvent` | `{player}`, `{block_type}`, `{block_placed}`, `{item_in_hand_id}`, `{location}` | Установка блока игроком. |
| `core:block_damage` | `BlockDamageEvent` | `{player}`, `{block_type}`, `{item_in_hand_id}`, `{location}` | Начало копания/удар по блоку. |
| `core:async_chat` | `AsyncChatEvent` | `{player}`, `{message}`, `{world}` | Отправка сообщения в чат. |
| `core:player_damage` | `EntityDamageEvent` | `{player}`, `{damage}`, `{cause}`, `{damager}` | Получение урона игроком. |
| `core:player_death` | `PlayerDeathEvent` | `{player}`, `{death_message}`, `{killer}`, `{location}` | Смерть игрока. |
| `core:entity_death` | `EntityDeathEvent` | `{entity}`, `{entity_type}`, `{killer}`, `{location}` | Смерть моба или сущности. |
| `core:player_consume` | `PlayerItemConsumeEvent` | `{player}`, `{item_id}` | Съедание еды или выпивание зелья. |
| `core:player_drop_item` | `PlayerDropItemEvent` | `{player}`, `{item_id}`, `{item_amount}`, `{location}` | Выбрасывание предмета из инвентаря (клавиша Q). |
| `core:player_swap_hand_items` | `PlayerSwapHandItemsEvent`| `{player}`, `{main_hand_item}`, `{off_hand_item}` | Смена предметов в руках (клавиша F). |
| `core:player_toggle_sneak` | `PlayerToggleSneakEvent` | `{player}`, `{is_sneaking}` | Нажатие / отпускание клавиши Shift (приседание). |
| `core:player_toggle_flight`| `PlayerToggleFlightEvent` | `{player}`, `{is_flying}` | Включение / выключение полета игрока. |
| `core:player_jump` | `PlayerJumpEvent` | `{player}`, `{location}` | Прыжок игрока. |
| `core:player_level_change` | `PlayerLevelChangeEvent` | `{player}`, `{old_level}`, `{new_level}` | Изменение уровня опыта игрока. |
| `core:craft_item` | `CraftItemEvent` | `{player}`, `{recipe_result}`, `{recipe_result_amount}` | Крафт предмета на верстаке. |
| `core:player_advancement_done` | `PlayerAdvancementDoneEvent`| `{player}`, `{advancement}` | Получение достижения игроком. |

---

## 🔍 2. Все Фильтры и Условия ядра (`conditions: [ - type: "..." ]`)

Всего в ядре зарегистрировано **19 фильтров**:

### Логические операторы и Базовые проверки:
1. **`core:always_true`**: Всегда возвращает `true`.
2. **`core:and`**: Логическое И. Принимает список условий `conditions: [...]`. Истинно, если все дочерние условия верны.
3. **`core:or`**: Логическое ИЛИ. Истинно, если хотя бы одно условие верно.
4. **`core:not`**: Логическое отрицание. Инвертирует результат дочернего условия `condition: {...}`.
5. **`core:chance`**: Проверка вероятности. Параметр `chance: 0.15` (от 0.0 до 1.0) или `percent: 15` (в процентах).
6. **`core:permission`**: Проверка прав игрока. Параметр `permission: "some.perm"`.

### Сравнение строк, чисел и контекста:
7. **`core:match`**: Проверка совпадения шаблона с текстом.
   - `template`: строка с переменной (например `{to_world}`).
   - `value`: ожидаемое значение (например `creative`).
   - `ignore_case: true/false`.
8. **`core:eq`**: Проверка равенства значения в контексте: `key: "button"`, `value: "RIGHT"`.
9. **`core:has`**: Проверяет, присутствует ли ключ в контексте: `key: "block"`.
10. **`core:in`**: Проверяет, входит ли значение контекста в список: `key: "world"`, `values: ["world", "lobby"]`.
11. **`core:gt`**: Числовое сравнение «Больше, чем» ($>$): `key: "damage"`, `value: 5.0`.
12. **`core:lt`**: Числовое сравнение «Меньше, чем» ($<$): `key: "damage"`, `value: 10.0`.
13. **`core:regex`**: Проверка регулярным выражением: `key: "message"`, `pattern: "^!(.*)"`.

### Проверки предметов, блоков и взгляда:
14. **`core:has_item`**: Проверяет наличие предмета в инвентаре игрока.
    - Параметры: `material` (или `item`): ID предмета (например `oraxen:astral_atlas`, `minecraft:diamond`).
    - `amount`: минимальное количество (по умолчанию 1).
15. **`core:has_not_item`**: Истинно, если указанного предмета **нет** в инвентаре игрока (защита от повторной выдачи).
16. **`core:check_item`**: Проверяет предмет в контексте (`context_key` или рука игрока) на совпадение по материалу.
17. **`core:check_block`**: Проверяет блок в контексте на совпадение по типу (поддерживает Oraxen/ItemsAdder/Vanilla).
18. **`core:looking_at`**: Проверяет, смотрит ли игрок на блок указанного типа в радиусе `distance` блоков.
19. **`core:looking_at_water`**: Проверяет, смотрит ли игрок на воду или блок воды в заданном радиусе.

---

## ⚡ 3. Все Действия ядра (`actions: [ - action: "..." ]`)

Всего в ядре зарегистрировано **14 экшенов**:

### 1. `core:command` — Выполнение консольной команды или команды игрока
- `command`: Текст команды (поддерживает плейсхолдеры, например `mv tp {player} creative`).
- `as_console`: `true` — от имени консоли сервера, `false` — от имени самого игрока.

### 2. `core:message` — Отправка форматированных сообщений MiniMessage
- `text`: Текст сообщения с градиентами (например `<gradient:#70e1f5:#ffd194>Текст</gradient>`).
- `type`: Тип вывода:
  - `chat` — в обычный чат игрока.
  - `actionbar` — над панелью быстрого доступа.
  - `title` — крупный заголовок по центру экрана (поддерживает параметр `subtitle`).

### 3. `core:sound` — Воспроизведение звука
- `sound`: Идентификатор звука (например `entity.enderman.teleport`, `item.book.page_turn`).
- `volume`: Громкость (float, по умолчанию 1.0).
- `pitch`: Высота тона (float, по умолчанию 1.0).

### 4. `core:give_item` — Выдача предмета в инвентарь
- `material` (или `item`): ID предмета (`oraxen:astral_atlas`, `minecraft:iron_ingot`).
- `amount`: Количество (по умолчанию 1).
- `if_absent` (или `unique`): `true` — выдавать **только если предмета еще нет в инвентаре**.

### 5. `core:damage` — Нанесение урона игроку
- `amount`: Количество урона в полусердцах.

### 6. `core:kill` — Мгновенное убийство игрока
- Устанавливает здоровье игрока в 0.

### 7. `core:potion_effect` — Наложение эффекта зелья
- `effect`: Название эффекта (например `SPEED`, `REGENERATION`, `NIGHT_VISION`).
- `duration`: Длительность в секундах.
- `amplifier`: Уровень эффекта (0 = 1 уровень, 1 = 2 уровень и т.д.).
- `particles`: `true`/`false` — отображать ли пузырьки эффекта.

### 8. `core:particle` — Спавн визуальных частиц
- `particle`: Название частицы (например `PORTAL`, `FLAME`, `SOUL_FIRE_FLAME`).
- `count`: Количество частиц.
- `dx`, `dy`, `dz` (или `spread_x`, `spread_y`, `spread_z`): Разброс.
- `speed`: Скорость разлета.

### 9. `core:firework` — Запуск фейерверка
- `power`: Сила полета ракеты (1–3).
- `type`: Форма взрыва (`BALL`, `BALL_LARGE`, `STAR`, `BURST`, `CREEPER`).
- `colors`: Список RGB или HEX цветов взрыва.

### 10. `core:teleport` — Телепортация
- `world`: Название целевого мира.
- `x`, `y`, `z`: Координаты назначения.
- `yaw`, `pitch`: Угол обзора камеры.

### 11. `core:push` — Отталкивание / импульс движения
- `dx`, `dy`, `dz`: Вектор импульса (например подбросить вверх `dy: 1.5`).

### 12. `core:spawn_entity` — Спавн сущности / моба
- `entity`: Тип сущности (например `ZOMBIE`, `LIGHTNING_BOLT`, `IRON_GOLEM`).
- `x`, `y`, `z`: Относительное или абсолютное смещение.

### 13. `core:grant_advancement` — Выдача достижения игроку
- `advancement`: Ключ достижения (например `seasons:ice_fabricator`).

### 14. `core:cancel_event` — Отмена базового события Bukkit
- Отменяет действие (например, отменяет разрушение защищенного блока или взаимодействие).
