# ⚡ Полный реестр Триггеров, Фильтров и Экшенов AAT

Данный документ содержит **исчерпывающий** список всех компонентов ядра ActionsAndTriggers, зарегистрированных в `ActionsTriggers.java`, `DefaultActionParsers.java` и `DefaultFilterParsers.java`.

---

## 🎯 1. Все Триггеры ядра (`triggers: [ - trigger: "..." ]`)

Всего в ядре зарегистрирован **21 триггер** событий:

| ID Триггера | Класс события / Механизм | Ключи контекста в шаблонах `{key}` | Описание |
| :--- | :--- | :--- | :--- |
| `core:interval` | Фоновый таймер ядра | `{tick}` | Периодический тик ядра (каждую секунду / 20 тиков) для станочных циклов, фоновых проверок и генераторов. |
| `core:player_interact` | `PlayerInteractEvent` | `{player}`, `{action}`, `{button}`, `{has_block}`, `{block_type}`, `{item_in_hand_id}`, `{location}` | Клик ПКМ / ЛКМ по воздуху или блоку. |
| `core:player_world_change` | `PlayerChangedWorldEvent` | `{player}`, `{from_world}`, `{to_world}`, `{world}`, `{location}` | Переход игрока между мирами сервера. |
| `core:player_join` | `PlayerJoinEvent` | `{player}`, `{join_message}`, `{world}`, `{location}` | Вход игрока на сервер. |
| `core:player_quit` | `PlayerQuitEvent` | `{player}`, `{quit_message}` | Выход игрока с сервера. |
| `core:block_break` | `BlockBreakEvent` | `{player}`, `{block_type}`, `{item_in_hand_id}`, `{location}` | Разрушение блока игроком. |
| `core:block_place` | `BlockPlaceEvent` | `{player}`, `{block_type}`, `{block_placed}`, `{item_in_hand_id}`, `{location}` | Установка блока игроком. |
| `core:block_damage` | `BlockDamageEvent` | `{player}`, `{block_type}`, `{item_in_hand_id}`, `{location}` | Начало копания/удар по блоку. |
| `core:async_chat` | `AsyncChatEvent` | `{player}`, `{message}`, `{world}` | Отправка сообщения в чат. |
| `core:player_damage` | `EntityDamageEvent` | `{player}`, `{damage}`, `{cause}`, `{damager}` | Получение урона игроком (активирует Combat Tag). |
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

### Логические операторы и Базовые проверки:
1. **`core:always_true`**: Всегда возвращает `true`.
2. **`core:and`**: Логическое И. Принимает список `conditions: [...]`. Истинно, если все дочерние условия верны.
3. **`core:or`**: Логическое ИЛИ. Истинно, если хотя бы одно условие верно.
4. **`core:not`**: Логическое отрицание. Инвертирует результат дочернего условия `condition: {...}`.
5. **`core:chance`**: Проверка вероятности. Параметр `chance: 0.15` (от 0.0 до 1.0) или `percent: 15` (в процентах).
6. **`core:permission`**: Проверка прав игрока. Параметр `permission: "some.perm"`.

### Боевой режим (Combat Tracker) и Кулдауны:
7. **`core:in_combat`**: Истинно, если игрок находится в бою.
8. **`core:not_in_combat`**: Истинно, если игрок вне боя.
9. **`core:on_cooldown`**: Истинно, если у игрока активен кулдаун `key: "ability_name"`.
10. **`core:not_on_cooldown`**: Истинно, если кулдаун завершился или отсутствует.

### Сравнение строк, чисел и контекста:
11. **`core:eq`**: Строгое равенство строки или плейсхолдера (`key: "world", value: "lobby"`).
12. **`core:match`**: Сравнение шаблона с поддержкой переменных (`template: "{item_in_hand_id}", value: "oraxen:astral_atlas"`).
13. **`core:in`**: Вхождение значения в список разрешенных (`template: "{world}", list: ["world", "world_nether"]`).
14. **`core:numeric`**: Числовое сравнение (`template: "{player.health}", op: "<=", value: 5.0`). Доступные операторы: `==`, `!=`, `>`, `>=`, `<`, `<=`.

---

## 🚀 3. Все Действия ядра (`actions: [ - action: "..." ]`)

### Таймеры, Отложенные действия и Кулдауны:
1. **`core:delay`**: Отложенное выполнение цепочки действий с изоляцией контекста.
   - Параметры: `ticks: 40` или `seconds: 2`, `actions: [...]`.
2. **`core:repeat`**: Циклическое повторение набора действий.
   - Параметры: `times: 5`, `interval: 20` (в тиках), `delay: 0`, `actions: [...]`.
   - Внутри цепочки доступна переменная номера итерации `{iteration}`.
3. **`core:schedule`**: Запланированный отложенный запуск с уникальным ID для возможности отмены.
   - Параметры: `id: "task_id"`, `delay: 100`, `actions: [...]`.
4. **`core:cancel_schedule`**: Отмена зарегистрированной задачи по `id: "task_id"`.
5. **`core:set_cooldown`**: Установка перезарядки для игрока.
   - Параметры: `key: "freeze_spell"`, `duration: 10` (в секундах).

### Графический интерфейс (GUI) и Станки:
6. **`core:open_gui`**: Открытие окна интерфейса (`gui: "ice_fabricator_gui"`).
7. **`core:close_gui`**: Закрытие открытого инвентаря игрока.
8. **`core:cryo_freeze`**: Полный цикл криогенного синтеза льда.
   - `water_slot`: Слот ведра с водой.
   - `crystal_slot`: Слот катализатора.
   - `output_slot`: Слот выхода готовой продукции.
   - `upgrade_slot`: Слот ускорителя (Tier 1 = -25%, Tier 2 = -50%, Tier 3 = -75% времени цикла).
   - Мгновенно расходует ингредиенты, запускает анимацию виджета `progress_bar` со стадиями и звуками, по завершении выдает лед (или сбрасывает на землю при переполненном инвентаре).

### Боевой режим (Combat):
9. **`core:tag_combat`**: Принудительно переводит игрока в боевой режим (`seconds: 15`).
10. **`core:untag_combat`**: Досрочно снимает боевой режим с игрока.

### Базовые игровые действия:
11. **`core:command`**: Выполнение команды (`command: "say Hello"`, `as_console: true/false`).
12. **`core:message`**: Отправка сообщений (`text`, `type: chat/actionbar/title`, `subtitle`).
13. **`core:sound`**: Воспроизведение звука (`sound`, `volume`, `pitch`).
14. **`core:give_item`**: Выдача предмета (`material`, `amount`, `if_absent: true/false`).
15. **`core:damage`**: Нанесение урона (`amount`).
16. **`core:kill`**: Мгновенное убийство игрока.
17. **`core:potion_effect`**: Наложение эффекта зелья (`effect`, `duration`, `amplifier`, `particles`).
18. **`core:particle`**: Спавн частиц (`particle`, `count`, `dx`, `dy`, `dz`, `speed`).
19. **`core:firework`**: Запуск фейерверка (`power`, `type`, `colors`).
20. **`core:teleport`**: Телепортация игрока (`world`, `x`, `y`, `z`, `yaw`, `pitch`).
21. **`core:push`**: Импульс движения игрока (`dx`, `dy`, `dz`).
22. **`core:spawn_entity`**: Спавн моба или сущности (`entity`, `x`, `y`, `z`).
23. **`core:grant_advancement`**: Выдача достижения (`advancement`).
24. **`core:cancel_event`**: Отмена базового события Bukkit.

---

## 🌐 4. Поддержка PlaceholderAPI и Контекстные переменные

Во всех строках сообщений, заголовках и описаниях виджетов поддерживаются плейсхолдеры:
- В стандартном формате PAPI: `%player_name%`, `%vault_eco_balance%`, `%server_online%`
- В формате фигурных скобок: `{player_name}`, `{vault_eco_balance}`, `{statistic_time_played}`
- Встроенные системные теги:
  * `{player}`: Имя игрока.
  * `{player.combat_remaining}`: Количество оставшихся секунд боя.
  * `{iteration}`: Номер текущего повтора в `core:repeat`.
  * `{tick}`: Счетчик тиков в `core:interval`.
  * `{progress_status}`: Состояние криостата.
  * `{progress_temp}`: Текущая температура станка.
  * `{progress_stage}`: Фаза криогенного синтеза.
  * `{progress_percent}`: Процент завершения (0–100).
