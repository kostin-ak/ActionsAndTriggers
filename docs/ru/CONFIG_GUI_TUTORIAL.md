# 🎨 Полное практическое руководство: Создание GUI через YAML

## 1. Введение и концепция
В **ActionsAndTriggers (AAT)** интерфейсы не требуют написания Java-кода. Любое меню (от простого навигатора миров до сложного промышленного станка с анимациями и слотами улучшений) полностью настраивается в файлах `plugins/ActionsAndTriggers/guis/*.yml`.

### Преимущества декларативных GUI:
- **Разметка через маски (Masks)**: Вместо высчитывания номеров слотов (0..53) вы рисуете интерфейс символами `9xN`.
- **Гарантированная сохранность предметов**: Реальные предметы из слотов ввода/выдачи никогда не пропадают при выходе, уроне или перезагрузке сервера.
- **Поддержка кастомных текстур**: Поддержка сдвигов шрифтов MiniMessage (`<shift:-8><glyph:custom_gui><shift:-164>`) и прозрачных слотов (`transparent_slot`) для идеального совмещения с ресурспаками Oraxen и ItemsAdder.

---

## 2. Базовая анатомия файла GUI

Каждый файл в папке `guis/` (например, `guis/custom_bench.yml`) имеет базовую структуру:

```yaml
id: custom_bench        # Уникальный идентификатор для вызова (core:open_gui)
title: "<gradient:#FFA07A:#FF6347>❖ Верстак Инженера ❖</gradient>" # Заголовок окна (MiniMessage)
rows: 3                 # Высота окна: от 1 до 6 рядов

allow_combat: false     # Запретить открытие в бою (по умолчанию false)
close_on_damage: true   # Закрывать меню мгновенно при получении урона

mask:
  pattern:
    - "#########"       # Ряд 1 (слоты 0..8)
    - "#I#+#C#=#"       # Ряд 2 (слоты 9..17)
    - "####X####"       # Ряд 3 (слоты 18..26)
  components:
    "#":
      type: slot_cover
      material: "minecraft:gray_stained_glass_pane"
      name: " "
    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Закрыть окно</red>"
      on_click:
        - action: "core:close_gui"
```

---

## 3. Подробный разбор всех типов виджетов

### 3.1. Декоративные панели и Заглушки

#### `slot_cover` (Физическая панель-заполнитель)
Размещает физический предмет (обычно стекло), очищает лор и блокирует любые клики.
```yaml
"#":
  type: slot_cover
  material: "minecraft:black_stained_glass_pane"
  name: " "
```

#### `transparent_slot` (Прозрачный слот под текстуры)
Физически пустой слот (`Material.AIR`), через который полностью видна фоновая текстура сундука. Все клики, перемещения предметов и Shift-клики блокируются ядром.
```yaml
".":
  type: transparent_slot
```

---

### 3.2. Интерактивные Кнопки

#### `button` (Обычная кнопка)
Выполняет цепочку действий `on_click` при нажатии игроком.
```yaml
"B":
  type: button
  material: "oraxen:astral_atlas"    # Поддерживаются oraxen:id, itemsadder:id и minecraft:id
  name: "<gold>Телепорт в Лобби</gold>"
  lore:
    - "<gray>Нажмите, чтобы вернуться</gray>"
    - "<yellow>в безопасную зону.</yellow>"
  on_click:
    - action: "core:command"
      command: "spawn"
      as_console: false
    - action: "core:sound"
      sound: "entity.enderman.teleport"
      volume: 1.0
      pitch: 1.2
```

#### `cycle_button` (Циклический переключатель режимов)
Переключает список состояний по кругу. Текущее значение сохраняется в `session_key` меню.
```yaml
"M":
  type: cycle_button
  session_key: "machine_speed"
  states:
    - id: "eco"
      material: "minecraft:lime_dye"
      name: "<green>Режим: ЭКО (-50% энергии)</green>"
    - id: "normal"
      material: "minecraft:yellow_dye"
      name: "<yellow>Режим: СТАНДАРТ</yellow>"
    - id: "turbo"
      material: "minecraft:red_dye"
      name: "<red>Режим: ТУРБО (+100% скорость)</red>"
```

---

### 3.3. Производственные слоты станка (Item Safety)

#### `input_slot` (Слот загрузки сырья и катализаторов)
Позволяет игроку класть разрешенные предметы. В пустом состоянии отображает интерактивный фантомный образец (`placeholder`), защищенный от дюпа тегом PDC `actionstriggers:gui_placeholder`.
```yaml
"W":
  type: input_slot
  allowed_items:
    - "minecraft:water_bucket"
  placeholder_material: "minecraft:bucket"
  placeholder_name: "<aqua>💧 Поместите ведро с водой</aqua>"
  placeholder_lore:
    - "<gray>Необходимо для охлаждения станка.</gray>"
```

#### `output_slot` (Слот выдачи готовой продукции)
Слот, доступный **только для извлечения**. Положить туда предмет невозможно.
```yaml
"O":
  type: output_slot
```

---

### 3.4. Динамические индикаторы и шкалы

#### `progress_bar` (Анимированный индикатор процесса)
Отображает процесс выполнения станочной операции с частотой 10 FPS и поддержкой плейсхолдеров `{percent}`, `{stage}`, `{temp}`.
```yaml
">":
  type: progress_bar
  idle_material: "minecraft:spectral_arrow"
  idle_name: "<aqua>⚡ Нажмите для запуска синтеза</aqua>"
  running_material: "minecraft:clock"
  running_name: "<gradient:#74B9FF:#0984E3>⚙ Синтез: {percent}%</gradient>"
  bar_length: 10
  filled_color: "#74B9FF"
  empty_color: "#636E72"
  on_click:
    - action: "core:cryo_freeze"
      water_slot: 10
      crystal_slot: 12
      output_slot: 16
      upgrade_slot: 24
```

#### `fluid_tank` (Вертикальный резервуар жидкостей)
Отображает уровень жидкости или хладагента в виде вертикального столбца слотов, заполняющегося снизу вверх.
```yaml
"T":
  type: fluid_tank
  level_key: "coolant_level"  # ключ сессии
  max_level: 100
  filled_material: "minecraft:water_bucket"
  empty_material: "minecraft:bucket"
  title: "<aqua>Уровень Хладагента: {level}/{max}</aqua>"
```

---

## 4. Комплексный пример: Высокотехнологичный Фабрикатор

Ниже представлен полноценный рабочий конфиг криогенного ледогенератора со слотом ускорителя (Tier 1-3):

```yaml
id: ice_fabricator_gui
title: "<shift:-8><glyph:ice_fabricator_bg><shift:-164><gradient:#74B9FF:#0984E3>❄ Криогенный Ледогенератор ❄</gradient>"
rows: 3
allow_combat: false
close_on_damage: true

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
      idle_name: "<aqua>⚡ Запустить Заморозку</aqua>"
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
      placeholder_name: "<gradient:#FFA07A:#FF6347>⚡ Слот Ускорителя (T1-T3)</gradient>"
      placeholder_lore:
        - "<gray>T1 (-25%): Медь / Железо</gray>"
        - "<gray>T2 (-50%): Золото / Изумруд</gray>"
        - "<gray>T3 (-75%): Алмаз / Незерит</gray>"

    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Закрыть панель</red>"
      on_click:
        - action: "core:close_gui"
```
