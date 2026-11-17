# ⚡ Реестр AAT: Триггеры, Фильтры и Действия

Этот документ — **эталонный справочник** по компонентам, которые регистрирует `ActionsTriggers#onEnable`, `DefaultActionParsers` и `DefaultFilterParsers`. Здесь перечислено только то, что реально существует в коде. Всё остальное не зарегистрировано, даже если упоминалось в прежних версиях документации.

> Источник истины: `aat-core/src/main/java/kostin/ak/actionstriggers/core/CoreTriggerKeys.java`, `core/defaults/actions/DefaultActionParsers.java`, `core/defaults/filters/DefaultFilterParsers.java`.

---

## 1. Триггеры

Все триггеры регистрируются в `ActionsTriggers#onEnable`. ID триггера — это точная строка для ключа `trigger:` в YAML-скрипте.

| ID триггера | Событие Bukkit / механизм | Ключи контекста | Примечания |
| :--- | :--- | :--- | :--- |
| `core:interval` | Фоновый таймер ядра (`runTaskTimer`, 20 тиков) | `tick` (`Long`) | Срабатывает раз в секунду. Если подписчиков нет, контекст не создаётся. |
| `core:player_interact` | `PlayerInteractEvent` | `player`, `action`, `button`, `item_in_hand_id`, `has_block`, `block`, `block_material`, `clicked_block_id`, `location` | Слушает с `ignoreCancelled = false`. Событие приходит для обеих рук, `EquipmentSlot` не фильтруется: взаимодействие оффхендом может дать второй вызов. |
| `core:player_world_change` | `PlayerChangedWorldEvent` | `player`, `from_world`, `to_world`, `world`, `world_name`, `location` | `from_world`/`to_world` — имена миров (`String`). |
| `core:player_join` | `PlayerJoinEvent` | `player`, `location` | Текста приветствия в контексте нет. |
| `core:player_quit` | `PlayerQuitEvent` | `player`, `location` | Текста прощания в контексте нет. |
| `core:block_break` | `BlockBreakEvent` | `player`, `block`, `block_material`, `location`, `item_in_hand`, `block_id`, `item_in_hand_id` | `ignoreCancelled = true`. |
| `core:block_place` | `BlockPlaceEvent` | `player`, `block`, `block_material`, `location`, `block_id`, `item_in_hand_id` | Ключ `block` содержит **поставленный** блок. |
| `core:block_damage` | `BlockDamageEvent` | `player`, `block`, `block_material`, `location`, `item_in_hand`, `block_id`, `item_in_hand_id` | Срабатывает в начале копания. |
| `core:player_chat` | `AsyncChatEvent` | `player`, `location`, `message` | Событие асинхронное: из действий этого триггера нельзя трогать мир и инвентарь. |
| `core:player_damage` | `EntityDamageEvent` (только игроки) | `player`, `damage`, `location`, `damage_cause`, `damager` | `damager` заполняется только для `EntityDamageByEntityEvent`. Срабатывает на любой урон, включая падение и огонь. |
| `core:player_death` | `PlayerDeathEvent` | `player`, `location`, `damage_source`, `item_drops` | Ключей `killer`, `death_message`, `entity_type` **нет**. |
| `core:entity_death` | `EntityDeathEvent` | `entity`, `location`, `player`, `killer`, `item_in_hand` | `player` привязан к `getKiller()`, то есть это убийца, а не жертва. Может быть `null`. |
| `core:player_consume` | `PlayerItemConsumeEvent` | `player`, `item`, `location`, `item_id` | — |
| `core:drop_item` | `PlayerDropItemEvent` | `player`, `location`, `item`, `item_id` | Ключа `item_amount` нет. |
| `core:swap_items` | `PlayerSwapHandItemsEvent` | `player`, `location`, `item_in_hand`, `main_hand_item_id`, `off_hand_item_id` | `item_in_hand` — предмет в основной руке. |
| `core:player_sneak` | `PlayerToggleSneakEvent` | `player`, `location` | Срабатывает **только при входе в присед** (`if (e.isSneaking())`). Ключа `is_sneaking` нет. |
| `core:player_flight` | `PlayerToggleFlightEvent` | `player`, `location`, `is_flying` | — |
| `core:player_jump` | `com.destroystokyo.paper.event.player.PlayerJumpEvent` | `player`, `location` | `location` — точка старта (`getFrom()`). |
| `core:player_level_change` | `PlayerLevelChangeEvent` | `player`, `level` | `level` — **новый** уровень. Ключа `old_level` нет. |
| `core:item_craft` | `CraftItemEvent` | `player`, `item`, `location`, `item_id` | `item` — результат рецепта. Ключа `recipe_result_amount` нет. |
| `core:advancement_done` | `PlayerAdvancementDoneEvent` | `player`, `location`, `advancement_key` | Формат ключа `namespace:path`, например `minecraft:story/shiny_gear`. |

### Устаревшие и несуществующие ID

Эти строки встречаются в старых доках и примерах, но **не зарегистрированы**: `core:async_chat`, `core:player_toggle_sneak`, `core:player_toggle_flight`, `core:craft_item`, `core:player_advancement_done`, `core:player_swap_hand_items`, `core:player_drop_item`, `core:block_click`, `core:player_leave`.

Подписка на неизвестный ID не бросает ошибку: `TriggerRegistry` просто сохраняет подписку, которую никто не вызывает. Такой скрипт молча не работает.

---

## 2. Фильтры (условия)

Список условий объединяется логическим **И**. Для вложенной логики используйте фильтры из таблицы ниже.

| ID фильтра | Параметры (как их читает код) | Семантика |
| :--- | :--- | :--- |
| `core:always_true` | — | Всегда пропускает. |
| `core:in_combat` | — | Игрок сейчас в боевом режиме (`CombatTracker`). |
| `core:not_in_combat` | — | Игрок вне боевого режима. |
| `core:on_cooldown` | `key` (по умолчанию `global`) | Кулдаун активен у выполняющего игрока. |
| `core:not_on_cooldown` | `key` (по умолчанию `global`) | Кулдаун отсутствует или истёк. |
| `core:cooldown` | `key` | Алиас `core:not_on_cooldown` (оставлен для совместимости). |
| `core:permission` | `permission` | `Player#hasPermission`. |
| `core:in_group` | `group` (по умолчанию `default`) | Членство в группе LuckPerms; без LuckPerms проверяется право `group.<name>`. |
| `core:not_in_group` | `group` | Отрицание `core:in_group`. |
| `core:chance` | `chance` (0.0–1.0, по умолчанию `1.0`) | Случайный пропуск. Параметра `percent` **нет**. |
| `core:looking_at` | `material` (`Material`, по умолчанию `STONE`), `distance` (по умолчанию `5`) | Блок в прицеле совпадает с материалом. |
| `core:looking_at_water` | — | Блок в прицеле в пределах 5 блоков — вода (fluid collision `ALWAYS`). |
| `core:has` | `key` | В контексте есть ключ нужного типа. |
| `core:eq` | `key`, `value` | Точное равенство сырому значению контекста. Типы должны совпадать: `Integer` не равен `String`. |
| `core:in` | `key`, `values` (YAML-список) | Значение контекста входит в список. Имена параметров — `key`/`values`, а не `template`/`list`. |
| `core:gt` | `key`, `value` | Числовое `>`; ключ читается как `Double`, поэтому `Integer`-ключи (например `level`) всегда дают `false`. |
| `core:lt` | `key`, `value` | Числовое `<`; то же ограничение по `Double`. |
| `core:regex` | `key`, `pattern` | `String#matches` (совпадение всей строки). |
| `core:match` | `template`, `value`, `ignore_case` (по умолчанию `true`) | Раскрывает `{плейсхолдеры}` в `template` и сравнивает с `value`. |
| `core:mismatch` | как у `core:match` | Отрицание `core:match`. |
| `core:and` | `conditions` (список) | Проходят все вложенные условия. |
| `core:or` | `conditions` (список) | Проходит хотя бы одно вложенное условие. |
| `core:not` | `condition` (одна мапа) | Инвертирует вложенное условие. |
| `core:check_item` | `context_key` (по умолчанию `item_in_hand_id`), `material` (строка или список) | Сравнение с полными ID предметов без учёта регистра. |
| `core:check_block` | `context_key` (по умолчанию `block_id`), `material` (строка или список) | Сравнение с полными ID блоков без учёта регистра. |
| `core:has_item` | `material` или `item` (строка или список), `amount` (по умолчанию `1`) | Считает подходящие предметы в инвентаре игрока. |
| `core:has_not_item` | как у `core:has_item` | Отрицание `core:has_item`. |

`core:numeric` **не реализован**. Используйте `core:gt`/`core:lt` либо `core:match` с готовым шаблоном.

---

## 3. Действия

Действия выполняются последовательно. `core:cancel_event` выставляет флаг в контексте; после этого `YamlTriggerLoader` прекращает остальные действия скрипта, а `BukkitEventTrigger` отменяет исходное событие, если оно `Cancellable`.

### Управление потоком и кулдауны

| ID действия | Параметры | Примечания |
| :--- | :--- | :--- |
| `core:delay` | `ticks` или `seconds` (или `delay` в тиках), `actions` | Выполняет вложенные действия на клонированном контексте. По умолчанию 20 тиков. |
| `core:repeat` | `times` (по умолчанию `1`), `interval` (тики, по умолчанию `20`) или `interval_seconds`, `delay` (тики), `actions` | Даёт `{iteration}` (с 1) во вложенном контексте. `times <= 0` — бесконечный цикл. |
| `core:schedule` | `id` (по умолчанию `task_<millis>`), `delay` (тики) или `seconds`, `actions` | Повторное использование `id` отменяет предыдущую задачу с этим id. |
| `core:cancel_schedule` | `id` | Отменяет именованную задачу. |
| `core:set_cooldown` | `key` (по умолчанию `global`), `duration` или `seconds` (по умолчанию `5`) | Кулдаун на игрока, хранится только в памяти. |

### GUI и станки

| ID действия | Параметры | Примечания |
| :--- | :--- | :--- |
| `core:open_gui` | `gui` (или `id`) | Открывает GUI из `guis/*.yml`. Передаёт `context.block` как привязанный блок. Жёсткий спецкейс: `astral_atlas` не открывается, пока игрок в бою. |
| `core:close_gui` | — | Закрывает инвентарь игрока на следующем тике. |
| `core:cryo_freeze` | `water_slot` (10), `crystal_slot` (12), `output_slot` (16), `upgrade_slot` (24) | Серверная механика льда. Требует открытого AAT GUI; при наличии `progress_bar` результат анимируется и откладывается, иначе выдаётся сразу. Вода и катализатор списываются мгновенно, режим берётся из сессионного ключа `fabricator_mode`. |
| `core:magma_smelt` | `orax_slot` (10), `onyx_slot` (12), `fuel_slot` (14), `output_slot` (16), `duration` (100) | Серверная механика сплава. |
| `core:alchemical_distill` | `ingredient_slot` (10), `water_slot` (12), `output_slot` (16), `duration` (80) | Серверная механика дистилляции. |

### Боевой режим

| ID действия | Параметры | Примечания |
| :--- | :--- | :--- |
| `core:tag_combat` | `seconds` (по умолчанию `15`) | Ставит комбат-тег. |
| `core:untag_combat` | — | Снимает комбат-тег. |

### Взаимодействие и визуал

| ID действия | Параметры | Примечания |
| :--- | :--- | :--- |
| `core:command` | `command`, `as_console` (по умолчанию `true`) | `as_console` вычисляется при парсинге скрипта, а не при каждом выполнении. |
| `core:message` | `text`, `type` (`chat`/`actionbar`/`title`), `subtitle` | `type` тоже фиксируется при парсинге. Поддерживается MiniMessage. |
| `core:sound` | `sound`, `volume` (1.0), `pitch` (1.0) | Ключ звука или старое имя Bukkit. |
| `core:give_item` | `material` (или `item`), `amount` (1), `if_absent` (или `unique`) | `if_absent` вычисляется при парсинге; предмет не выдаётся, если такой полный ID уже есть в инвентаре. |
| `core:damage` | `amount` (1.0) | `Player#damage`. |
| `core:kill` | — | `setHealth(0)`. |
| `core:potion_effect` | `effect` (по умолчанию `speed`), `duration` (**тики**, по умолчанию 200), `amplifier` (0), `particles` (true) | `duration: 3` — это 3 тика, а не 3 секунды. |
| `core:particle` | `particle` (имя enum, по умолчанию `FLAME`), `count` (10), `dx`/`dy`/`dz` (смещения, `dy` по умолчанию 1.0), `spread_x`/`spread_y`/`spread_z` (0.5), `speed` (0.0) | Здесь `dx/dy/dz` — позиционные смещения. |
| `core:firework` | `power` (1) | Эффект зашит: красный/жёлтый с оранжевым затуханием, `BALL_LARGE`, trail. `type` и `colors` игнорируются. |
| `core:teleport` | `x`, `y`, `z`, `yaw`, `pitch`, `world` | Незаданные координаты берутся у игрока; неизвестный мир заменяется текущим. |
| `core:push` | `x` (0.0), `y` (0.5), `z` (0.0), `add` (false) | `add: true` прибавляет к текущей скорости, иначе заменяет её. `dx/dy/dz` здесь **не** читаются. |
| `core:spawn_entity` | `entity` (`EntityType`, по умолчанию `ZOMBIE`), `x`, `y`, `z` | `LIGHTNING_BOLT` создаётся через `strikeLightning`. |
| `core:grant_advancement` | `advancement` (namespaced key) | Выдаёт все оставшиеся критерии. |
| `core:cancel_event` | — | Отменяет родительское событие Bukkit и останавливает остаток цепочки. |

### LuckPerms

| ID действия | Параметры | Примечания |
| :--- | :--- | :--- |
| `core:add_permission` | `permission` | Без LuckPerms — no-op. |
| `core:remove_permission` | `permission` | Без LuckPerms — no-op. |
| `core:set_group` | `group` (по умолчанию `default`) | Меняет основную группу и добавляет inheritance-узел. Без LuckPerms — no-op. |

---

## 4. Плейсхолдеры контекста

Плейсхолдеры раскрывает `ContextPlaceholderParser` (линейный проход, без regex) в любой строке, которая идёт через `ActionParameters`, и `PapiHook` — в текстах GUI.

### Встроенный формат

- `{key}` подставляет сырое значение контекста через форматтер типа.
- `{key.property}` подставляет свойство, например `{player.name}`, `{location.x}`, `{block.type}`.
- Ключи типизированы. Для значений без отдельного форматтера используется `toString()`, поэтому `{block_material}` даёт `BLUE_ICE`, а `{action}` — `RIGHT_CLICK_BLOCK`.
- **Неизвестный токен удаляется из строки** (заменяется пустотой), а не сохраняется. Условие с опечаткой в ключе молча сравнивается с пустой строкой и не проходит.

### Поддерживаемые свойства

`player`: `name`, `uuid`, `health`, `combat_remaining`, `combat_seconds`, `combat_time`, `combat`, `group`, `prefix`, `suffix`, `weight`.
`location`: `x`, `y`, `z`, `block_x`, `block_y`, `block_z`, `world`.
`block`: `type`, `x`, `y`, `z`, `world`.
`entity`: `name`, `type`, `uuid`.
`item` (`ItemStack`): `type`, `amount`.

Ключи конкретных триггеров перечислены в разделе 1. `{iteration}` даёт `core:repeat`, `{tick}` — `core:interval`.

### PlaceholderAPI

- Стандартный синтаксис PAPI (`%player_name%`) раскрывается через `PlaceholderAPI#setPlaceholders`, если плагин установлен.
- Синтаксис в скобках (`{player_name}`) конвертируется в `%player_name%` и раскрывается **только если PAPI его разрешил**; иначе скобки остаются как есть. Из-за этого синтаксис PAPI в скобках и синтаксис контекста AAT используют одну и ту же форму `{...}`: при совпадении имени в параметрах действий побеждает значение контекста.
- В текстах GUI PAPI применяют сами виджеты (`ButtonWidget`, `InputSlotWidget`, `OutputSlotWidget`, `ProgressBarWidget`, `CycleButtonWidget`). `ToggleWidget` и `FluidTankWidget` PAPI **не** вызывают.

---

## 5. Чего сознательно нет

Это часто спрашивают, но в текущем коде не реализовано; полный список с обходными путями — в `docs/ru/POTENTIAL_ISSUES.md`:

- Числовой фильтр (`core:numeric`).
- Процентный синтаксис у `core:chance`.
- Параметры цвета и типа у `core:firework`.
- Настраиваемые флаги боя для GUI (`allow_combat`, `close_on_damage`).
- Поддержка `tab_container` из YAML, источники предметов для `paged_list` и числовые источники для `fluid_tank`.
- Сохранение кулдаунов, комбат-тегов и именованных задач между перезапусками.
