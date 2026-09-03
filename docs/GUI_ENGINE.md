# 🖥️ AAT Widget-Oriented GUI Engine

## 1. Архитектурная концепция
Движок GUI в **ActionsAndTriggers** реализован на базе паттерна **Компоновщик (Composite Pattern)**. Вместо низкоуровневого хардкода номеров слотов от 0 до 53 интерфейс конфигурируется в виде **дерева виджетов** и наглядных **масок-шаблонов**.

### Ключевые возможности:
- **Координатная сетка 9xN**: Виджеты оперируют координатами `x (0..8)` и `y (0..5)`, абсолютный слот рассчитывается автоматически.
- **Поддержка Oraxen и ItemsAdder**: В полях материалов можно указывать `oraxen:item_id` или `itemsadder:item_id`.
- **Поддержка кастомных глифов фона**: Заголовок `title` поддерживает сдвиги шрифта (`<shift:-8><glyph:my_gui_bg><shift:-164>`) для отрисовки бесшовных кастомных интерфейсов.
- **Бесцветные/прозрачные заглушки (`transparent_slot`)**: Физически пустые слоты без текстур стекла и бликов, сквозь которые виден идеальный кастомный фон.
- **Интерактивные призрачные образцы (`ghost placeholders`)**: Пустые слоты отображают визуальные подсказки, защищенные системным тегом PDC (`actionstriggers:gui_placeholder`).
- **Синхронизация состояния (Data Binding)**: Автоматическое сохранение режимов в `session_state` интерфейса или в PDC блоков мира.
- **Гарантированная защита от потери ресурсов**: Слоты масок корректно распаковываются. При закрытии меню, уроне, перезагрузке или выходе с сервера реальные ресурсы игрока возвращаются в инвентарь или выбрасываются на землю, если инвентарь переполнен.

---

## 2. Структура YAML-конфигурации (`guis/*.yml`)

Каждый файл в каталоге `plugins/ActionsAndTriggers/guis/` представляет собой отдельный интерфейс:

```yaml
id: ice_fabricator_gui
title: "<shift:-8><glyph:ice_fabricator_bg><shift:-164><gradient:#74B9FF:#0984E3>❄ Криогенный Ледогенератор ❄</gradient>"
rows: 3  # Количество рядов (от 1 до 6)

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
      placeholder_name: "<aqua>💧 Слот Сырья (Вода)</aqua>"

    "C":
      type: input_slot
      allowed_items:
        - "oraxen:frost_crystal"
        - "minecraft:amethyst_shard"
      placeholder_material: "minecraft:amethyst_shard"
      placeholder_name: "<gradient:#E0C3FC:#8EC5FC>❄ Катализатор</gradient>"

    ">":
      type: progress_bar
      idle_material: "minecraft:spectral_arrow"
      idle_name: "<aqua>⚡ Запустить Крио-Заморозку</aqua>"
      running_material: "minecraft:clock"
      running_name: "<gradient:#74B9FF:#0984E3>❄ Заморозка: {percent}%</gradient>"
      bar_length: 10
      filled_color: "#74B9FF"
      empty_color: "#636E72"
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
      placeholder_name: "<gradient:#FFA07A:#FF6347>⚡ Модуль Ускорения</gradient>"

    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Закрыть панель</red>"
      on_click:
        - action: "core:close_gui"
```

---

## 3. Полный реестр виджетов

### 3.1. Заполнители и заглушки
1. **`transparent_slot`** (или `invisible_slot`):
   - Слот остается физически пустым (`Material.AIR`).
   - Идеален при использовании фоновых текстур ресурспаков: скрывает стандартную сетку сундука.
   - Любые клики и перемещения предметов блокируются.
2. **`slot_cover`** (или `cover`, `blank`):
   - Декоративная физическая панель (например, `minecraft:gray_stained_glass_pane`).
   - Очищает имя и лор, отменяет любые взаимодействия.

### 3.2. Производственные слоты
1. **`input_slot` (Слот сырья и компонентов)**:
   - Разрешает помещение только предметов из `allowed_items`.
   - В пустом состоянии выводит призрачный образец с подсказкой (`placeholder_material`, `placeholder_name`, `placeholder_lore`).
   - Помечен PDC-тегом `actionstriggers:gui_placeholder` для исключения дюпов.
2. **`output_slot` (Слот готовой продукции)**:
   - Доступен **только для извлечения**. Помещение предметов заблокировано.
   - Поддерживает хук `on_take` (звуки, эффекты, статистика).

### 3.3. Динамические индикаторы и шкалы
1. **`progress_bar` (Анимированный прогресс-бар)**:
   - 10 FPS плавная анимация процессов с поддержкой стадий.
   - Переменные: `{percent}`, `{bar}`, `{stage}`, `{temp}`, `{status}`.
2. **`fluid_tank` (Вертикальный резервуар жидкостей)**:
   - Отображает уровень жидкости или хладагента в виде столбца слотов снизу вверх.
   - Параметры: `level_key` (ключ сессии/PDC), `max_level`, `filled_material`, `empty_material`, `title`.

### 3.4. Навигация и контейнеры
1. **`paged_list` (Постраничный список)**:
   - Автоматическая разбивка коллекции предметов на страницы.
   - Кнопки навигации `prev_slot` и `next_slot`.
2. **`tab_container` (Контейнер вкладок)**:
   - Переключение между несколькими экранами/вкладками без закрытия инвентаря.
   - Сохраняет активную вкладку в `active_tab`.
3. **`cycle_button` (Циклический переключатель)**:
   - Переключение состояний (`states`) по клику с записью в `session_key` или `persistent_key`.

---

## 4. Безопасность и Боевой режим (Combat Interruption)

1. **Гарантия сохранности предметов**:
   - При закрытии окна все реальные предметы из слотов ввода/вывода возвращаются в инвентарь игрока.
   - Если инвентарь заполнен, предметы гарантированно сбрасываются под ноги игроку (`dropItemNaturally`).
2. **Боевой режим (Combat Mode)**:
   - Получение урона из любого источника (падение, огонь, лава, мобы, стрелы, PvP) накладывает комбат-тег.
   - Любое окно с запретом работы в бою или флагом закрытия при уроне **мгновенно закрывается**.
   - Попытка открыть запрещенный интерфейс в бою блокируется с выводом кулдауна оставшихся секунд.
