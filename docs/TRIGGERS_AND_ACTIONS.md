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

### Боевой режим (Combat Tracker):
7. **`core:in_combat`**: Истинно, если игрок находится в бою (получил/нанес урон менее 15 сек. назад).
8. **`core:not_in_combat`**: Истинно, если игрок вне боя (безопасен для телепортаций и открытия меню).

### Сравнение строк, чисел и контекста:
9. **`core:match`**: Проверка совпадения шаблона с текстом (`template`, `value`, `ignore_case: true/false`).
10. **`core:mismatch`**: Проверка **несовпадения** шаблона с текстом (`template`, `value`).
11. **`core:eq`**: Проверка равенства значения в контексте: `key: "button"`, `value: "RIGHT"`.
12. **`core:has`**: Проверяет, присутствует ли ключ в контексте: `key: "block"`.
13. **`core:in`**: Проверяет, входит ли значение контекста в список: `key: "world"`, `values: ["world", "lobby"]`.
14. **`core:gt`**: Числовое сравнение «Больше, чем» ($>$): `key: "damage"`, `value: 5.0`.
15. **`core:lt`**: Числовое сравнение «Меньше, чем» ($<$): `key: "damage"`, `value: 10.0`.
16. **`core:regex`**: Проверка регулярным выражением: `key: "message"`, `pattern: "^!(.*)"`.

### Проверки предметов, блоков и взгляда:
17. **`core:has_item`**: Проверяет наличие предмета в инвентаре игрока (`material`, `amount`).
18. **`core:has_not_item`**: Истинно, если предмета **нет** в инвентаре игрока.
19. **`core:check_item`**: Проверяет предмет в контексте на совпадение по ID.
20. **`core:check_block`**: Проверяет блок в контексте на совпадение по типу.
21. **`core:looking_at`**: Проверяет, смотрит ли игрок на блок указанного типа.
22. **`core:looking_at_water`**: Проверяет, смотрит ли игрок на воду или блок воды.

---

## ⚡ 3. Все Действия ядра (`actions: [ - action: "..." ]`)

### Графические интерфейсы (GUI):
1. **`core:open_gui`**: Открытие графического интерфейса.
   - `gui` (или `id`): Идентификатор GUI из папки `guis/*.yml`.
   - *Безопасность*: При открытии `astral_atlas` автоматически проверяется статус боя — в бою открытие блокируется с выводом таймера.
2. **`core:close_gui`**: Безопасное закрытие текущего открытого инвентаря игрока.
3. **`core:cryo_freeze`**: Специализированный экшен крио-заморозки для станка.
   - `water_slot`: Слот с ведром воды (сырье).
   - `crystal_slot`: Слот с морозным кристаллом (катализатор).
   - `output_slot`: Слот выхода готовой продукции.
   - Мгновенно расходует ингредиенты, запускает анимацию виджета `progress_bar` со стадиями и звуками, по завершении выдает готовый лед.

### Боевой режим (Combat):
4. **`core:tag_combat`**: Принудительно переводит игрока в боевой режим.
   - `seconds`: Длительность в секундах (по умолчанию 15).
5. **`core:untag_combat`**: Досрочно снимает боевой режим с игрока.

### Базовые игровые действия:
6. **`core:command`**: Выполнение команды (`command`, `as_console: true/false`).
7. **`core:message`**: Отправка сообщений (`text`, `type: chat/actionbar/title`, `subtitle`).
8. **`core:sound`**: Воспроизведение звука (`sound`, `volume`, `pitch`).
9. **`core:give_item`**: Выдача предмета (`material`, `amount`, `if_absent: true/false`).
10. **`core:damage`**: Нанесение урона (`amount`).
11. **`core:kill`**: Мгновенное убийство игрока.
12. **`core:potion_effect`**: Наложение эффекта зелья (`effect`, `duration`, `amplifier`, `particles`).
13. **`core:particle`**: Спавн частиц (`particle`, `count`, `dx`, `dy`, `dz`, `speed`).
14. **`core:firework`**: Запуск фейерверка (`power`, `type`, `colors`).
15. **`core:teleport`**: Телепортация игрока (`world`, `x`, `y`, `z`, `yaw`, `pitch`).
16. **`core:push`**: Импульс движения игрока (`dx`, `dy`, `dz`).
17. **`core:spawn_entity`**: Спавн моба или сущности (`entity`, `x`, `y`, `z`).
18. **`core:grant_advancement`**: Выдача достижения (`advancement`).
19. **`core:cancel_event`**: Отмена базового события Bukkit.

---

## 🌐 4. Поддержка PlaceholderAPI и Контекстные переменные

Благодаря встроенному модулю **`PapiHook`**, во всех строках сообщений, заголовках и описаниях виджетов поддерживаются плейсхолдеры:
- В стандартном формате PAPI: `%player_name%`, `%vault_eco_balance%`, `%server_online%`
- В формате фигурных скобок: `{player_name}`, `{vault_eco_balance}`, `{statistic_time_played}`
- Встроенные системные теги:
  * `{player}`: Имя игрока.
  * `{player.combat_remaining}`: Количество оставшихся секунд боя.
  * `{progress_status}`: Состояние криостата.
  * `{progress_temp}`: Текущая температура станка.
  * `{progress_stage}`: Фаза криогенного синтеза.
  * `{progress_percent}`: Процент завершения (0–100).
