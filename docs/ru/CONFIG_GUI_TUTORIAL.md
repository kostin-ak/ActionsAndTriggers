# 🎨 Руководство: Создание GUI в YAML

## 1. Концепция

Меню и панели станков описываются в `plugins/ActionsAndTriggers/guis/*.yml`; для типов, которые поддерживает `YamlGuiLoader`, Java не нужна.

- **Маски**: рисуйте раскладку символами 9×N и сопоставляйте каждый символ виджету. Загрузчик принимает и плоскую секцию `items` (или `widgets`) с явными `slot`/`x`/`y`.
- **ID материалов**: `minecraft:`, `oraxen:` и `itemsadder:` разрешаются через `ItemRegistry`.
- **Глифы в заголовке**: MiniMessage, включая `<shift:...>` и `<glyph:...>`. Если Oraxen не установлен, загрузчик вырезает теги `<glyph:...>` и `<shift:...>` из заголовка.
- **Сохранность предметов**: реальные предметы из `input_slot`/`output_slot` возвращаются игроку при закрытии инвентаря. Обработчика остановки сервера и выключения плагина у AAT нет, поэтому на рестарты эта гарантия не распространяется.

## 2. Анатомия файла

```yaml
id: custom_bench
title: "<gradient:#FFA07A:#FF6347>❖ Верстак Инженера ❖</gradient>"
rows: 3

on_open:
  - action: "core:sound"
    sound: "block.anvil.use"

mask:
  pattern:
    - "#########"
    - "#I#+#C#=#"
    - "####X####"
  components:
    "#":
      type: slot_cover
      material: "minecraft:gray_stained_glass_pane"
    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Закрыть</red>"
      on_click:
        - action: "core:close_gui"
```

`rows` ограничивается диапазоном 1..6. `on_close` парсится, но текущий слушатель его не выполняет; `target_block` сохраняется, но рантайм это поле не читает. `allow_combat` и `close_on_damage` игнорируются — см. раздел 6.

## 3. Типы виджетов, доступные из YAML

### 3.1. Декор

`slot_cover` (алиасы `cover`, `blank`) ставит физический предмет и блокирует клики. Поля: `material`/`item`, `fallback`. `transparent_slot` (алиас `invisible_slot`) оставляет слот физически пустым и тоже блокирует клики; `transparent: true` даёт тот же эффект для любой заглушки.

`filler` кладёт один предмет сразу в несколько слотов: `material`/`item`, `name`, `slots` (список) или `x`/`y`.

### 3.2. Кнопки

```yaml
"B":
  type: button
  material: "oraxen:astral_atlas"
  name: "<gold>Телепорт в Убежище</gold>"
  lore:
    - "<gray>Нажмите, чтобы вернуться</gray>"
  amount: 1
  custom_model_data: 0
  on_click:            # "actions" принимается как алиас
    - action: "core:command"
      command: "spawn"
      as_console: false
```

Неизвестный `type` трактуется как `button`, поэтому опечатку в типе виджета легко не заметить.

`cycle_button` перебирает `states`; у каждого состояния есть `id`, `material`/`item`, `name`, `lore`, `actions`. Текущий id хранится в `session_key` или `persistent_key` (PDC привязанного tile-блока).

`toggle` — булев переключатель с `session_key`/`persistent_key`, `default`, `on_state` и `off_state` (у каждого `material`, `name`, `lore`) и действиями `on_change`.

### 3.3. Слоты станка

```yaml
"W":
  type: input_slot
  allowed_items:
    - "minecraft:water_bucket"
  placeholder_material: "minecraft:bucket"
  placeholder_name: "<aqua>Поместите ведро с водой</aqua>"
  placeholder_lore:
    - "<gray>Нужно для охлаждения.</gray>"
```

`input_slot` принимает только `allowed_items` (пустой список — любые) и вызывает `on_insert`/`on_extract`. Плейсхолдеры несут PDC-тег `actionstriggers:gui_placeholder` и не забираются.

`output_slot` работает только на извлечение, поддерживает те же поля плейсхолдера и хук `on_take`. Вставка предметов и забирание плейсхолдера блокируются.

Shift-клик из инвентаря игрока отменяется всегда, поэтому shift-вставка в слот станка невозможна; предметы кладутся обычным кликом.

### 3.4. Индикаторы

`progress_bar` показывает состояние ожидания и состояние выполнения. Поддерживает `{percent}`, `{bar}`, `{stage}`, `{temp}`, `{status}` и PAPI в текстах. Сам процесс запускается из Java (встроенные станочные действия вызывают `startProcess`); `on_click` выполняет ваш список действий, где обычно и стоит `core:cryo_freeze`.

`fluid_tank` рисует вертикальный столбец, заполняя его снизу вверх. Читает **числовое** значение из сессии GUI по ключу `level_key`; заголовок рендерится как есть (подстановок `{level}`/`{max}` нет). YAML не умеет положить число в сессию, поэтому резервуар, объявленный только в YAML, показывает 0, пока плагин не запишет это значение.

`paged_list` не имеет источника предметов в YAML; список всегда пуст, если не заполнен из Java.

`tab_container` загрузчиком не поддерживается: `type: tab_container` превращается в кнопку. Вкладки собираются из Java.

## 4. Справочник полей виджетов

| Тип | Поля |
| :--- | :--- |
| `slot_cover`, `cover`, `blank`, `transparent_slot`, `invisible_slot` | `material`, `item`, `fallback`, `transparent` |
| `filler` | `material`, `item`, `name`, `slots` |
| `button` (и неизвестные типы) | `material`, `item`, `name`, `lore`, `amount`, `custom_model_data`, `actions`, `on_click` |
| `input_slot` | `allowed_items`, `placeholder_material`, `placeholder_name`, `placeholder_lore`, `on_insert`, `on_extract` |
| `output_slot` | `placeholder_material`, `placeholder_name`, `placeholder_lore`, `on_take` |
| `progress_bar` | `idle_material`, `idle_name`, `idle_lore`, `running_material`, `running_name`, `running_lore`, `bar_length`, `filled_color`, `empty_color`, `on_click` |
| `toggle` | `persistent_key`, `session_key`, `default`, `on_state`, `off_state`, `on_change` |
| `cycle_button` | `persistent_key`, `session_key`, `states` |
| `fluid_tank` | `height`, `level_key`, `max_level`, `filled_material`, `empty_material`, `title` |
| `paged_list` | `width`, `height`, `prev_slot`, `next_slot`, `session_key` |

Любой виджет также принимает `slot` или `x`/`y` и `condition`/`conditions` (дерево фильтров, вычисляемое с игроком и привязанным блоком в контексте).

## 5. Полный рабочий пример: криогенный фабрикатор

```yaml
id: ice_fabricator_gui
title: "<shift:-8><glyph:ice_fabricator_bg><shift:-164><gradient:#74B9FF:#0984E3>❄ Криогенный Фабрикатор ❄</gradient>"
rows: 3

mask:
  pattern:
    - "#########"
    - "#W#C#>#O#"
    - "##M#S#U#X"
  components:
    "#":
      type: transparent_slot

    "W":
      type: input_slot
      allowed_items:
        - "minecraft:water_bucket"
      placeholder_material: "minecraft:bucket"
      placeholder_name: "<aqua>💧 Слот Воды</aqua>"

    "C":
      type: input_slot
      allowed_items:
        - "oraxen:frost_crystal"
        - "minecraft:amethyst_shard"
      placeholder_material: "minecraft:amethyst_shard"
      placeholder_name: "<gradient:#E0C3FC:#8EC5FC>❄ Морозный Кристалл</gradient>"

    ">":
      type: progress_bar
      idle_material: "minecraft:spectral_arrow"
      idle_name: "<aqua>⚡ Запустить Процесс</aqua>"
      running_material: "minecraft:clock"
      running_name: "<gradient:#74B9FF:#0984E3>❄ Заморозка: {percent}%</gradient>"
      on_click:
        - action: "core:cryo_freeze"
          water_slot: 10
          crystal_slot: 12
          output_slot: 16
          upgrade_slot: 24

    "O":
      type: output_slot

    "M":
      type: cycle_button
      session_key: "fabricator_mode"
      states:
        - id: "blue_ice"
          material: "minecraft:blue_ice"
          name: "<gradient:#74B9FF:#0984E3>Режим: Синий Лёд</gradient>"
        - id: "packed_ice"
          material: "minecraft:packed_ice"
          name: "<gradient:#A0E7E5:#B4F8C8>Режим: Плотный Лёд</gradient>"
        - id: "ice"
          material: "minecraft:ice"
          name: "<aqua>Режим: Обычный Лёд</aqua>"

    "S":
      type: button
      material: "oraxen:frost_crystal"
      name: "<gradient:#E0C3FC:#8EC5FC>Криостат: АКТИВЕН</gradient>"

    "U":
      type: input_slot
      allowed_items:
        - "oraxen:cryo_accelerator_t1"
        - "oraxen:cryo_accelerator_t2"
        - "oraxen:cryo_accelerator_t3"
        - "minecraft:copper_ingot"
        - "minecraft:iron_ingot"
        - "minecraft:gold_ingot"
        - "minecraft:emerald"
        - "minecraft:diamond"
        - "minecraft:netherite_ingot"
      placeholder_material: "minecraft:redstone_torch"
      placeholder_name: "<gradient:#FFA07A:#FF6347>⚡ Модуль Ускорения (T1-T3)</gradient>"

    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Закрыть</red>"
      on_click:
        - action: "core:close_gui"
```

Номера слотов, передаваемые в `core:cryo_freeze`, — абсолютные слоты инвентаря. В этой маске вода находится в 10, катализатор в 12, выход в 16, слот ускорителя в 24.

## 6. Ограничения, которые стоит учесть до проектирования меню

- **Флагов боя у GUI нет.** `allow_combat`/`close_on_damage` не читаются. Ограничивайте открытие условием `core:not_in_combat` в триггере. Принудительно закрывается только жёстко зашитый GUI `astral_atlas`.
- **`on_close` не выполняется.** Очистку делайте в действии, которое закрывает меню, или в триггере выхода игрока.
- **Нет восстановления предметов при остановке.** Просите игроков закрывать станки перед рестартом либо добавьте свой обработчик остановки.
- **Ячейка маски — один слот.** Многослотовый виджет внутри маски рендерится на каждую ячейку, и берётся только его первый предмет. Для резервуаров и списков используйте `items`/`widgets` с `x`/`y`.
- **Слоты вне маски всё равно защищены** от взятия и размещения: слушатель отменяет клики по слотам без видимого виджета. Закрывать их декором — вопрос внешнего вида, а не безопасности.
- **`persistent_key` требует привязанного блока.** PDC доступен, только если `core:open_gui` выполнился с блоком в контексте и этот блок — tile-сущность.