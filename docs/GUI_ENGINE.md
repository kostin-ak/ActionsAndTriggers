# 🖥️ AAT Widget-Oriented GUI Engine

## 1. Концепция Виджето-ориентированного GUI
Движок GUI в **ActionsAndTriggers** построен на паттерне **Компоновщик (Composite Pattern)**. Вместо прямого ручного указания номеров слотов от 0 до 53, интерфейс собирается из **дерева виджетов**.

Каждый виджет:
- Имеет свою прямоугольную область (**Bounding Box**: `x, y, width, height`).
- Может содержать вложенные дочерние виджеты (`children`).
- Отвечает за собственный жизненный цикл: рендеринг (`render`), перехват кликов (`onClick`), реакцию на перемещение предметов (`onItemChange`) и обновление состояния (`update`).
- Поддерживает динамическое связывание (**Data Binding**) с `PersistentDataContainer` (PDC) блоков и игроков.

---

## 2. Координатная сетка и Bounding Box
Инвентарь Minecraft представляет собой 2D-сетку:
- Ширина (`width`): строго 9 слотов (координата `x`: от `0` до `8`).
- Высота (`height` / `rows`): от 1 до 6 рядов (координата `y`: от `0` до `5`).
- Абсолютный слот рассчитывается автоматически: $\text{slot} = y \times 9 + x$.

Виджеты-контейнеры могут задавать локальные системы координат для своих детей.

---

## 3. Полный каталог виджетов

### 3.1. Контейнеры и Компоновка (Layout Widgets)
1. **`panel` (Панель)**:
   - Базовый прямоугольный контейнер.
   - Параметры: `x`, `y`, `width`, `height`, `children`.
2. **`mask` (Маска-шаблон)**:
   - Позволяет визуально рисовать раскладку символами в YAML:
     ```yaml
     type: mask
     pattern:
       - "#########"
       - "#..C.C..#"
       - "B###X###N"
     components:
       "#": { type: filler, material: "minecraft:gray_stained_glass_pane" }
       "X": { type: button, material: "minecraft:barrier", name: "<red>Закрыть</red>", actions: [...] }
     ```
3. **`tabs` (Вкладки)**:
   - Позволяет разместить несколько страниц-вкладок в одном окне без переоткрытия инвентаря.
   - Переключение вкладок обновляет только зону контента без мерцания экрана у игрока.
4. **`paged_list` (Автопагинация)**:
   - Принимает список элементов любого объема (10, 50, 500 штук).
   - Автоматически нарезает список на страницы под размер выделенной зоны.
   - Связывается с кнопками `prev_button` и `next_button`. Поддерживает плейсхолдеры `{page}` и `{max_pages}`.

---

### 3.2. Интерактивные элементы (Controls)
1. **`button` (Кнопка)**:
   - Стандартный элемент клика.
   - Поддерживает:
     - `material`: ID предмета (Vanilla, Oraxen, ItemsAdder).
     - `name`: Заголовок (MiniMessage).
     - `lore`: Список строк описания.
     - `actions`: Список выполняемых AAT-действий при клике.
     - `condition`: Условие видимости / активности кнопки.
2. **`toggle` (Переключатель)**:
   - Двухпозиционная кнопка (ВКЛ / ВЫКЛ).
   - Меняет визуал в зависимости от булевого состояния.
   - Поддерживает привязку к сессии, метаданным игрока или PDC блока.
3. **`cycle_button` (Циклический селектор)**:
   - Переключает список состояний по очереди при каждом клике (например: Режим 1 $\rightarrow$ Режим 2 $\rightarrow$ Режим 3 $\rightarrow$ Режим 1).
4. **`stepper` (Счетчик)**:
   - Виджет с кнопками уменьшения `[-]` и увеличения `[+]` значения с числовым полем посередине.

---

### 3.3. Виджеты для кастомных блоков и станков (Machines)
1. **`input_slot` (Слот загрузки сырья)**:
   - Разрешает игроку помещать предметы в слот.
   - Параметры:
     - `allowed_items`: Список разрешенных ID предметов (`["minecraft:water_bucket", "oraxen:frost_crystal"]`).
     - `max_stack`: Максимальный размер пачки.
     - `on_insert`: Действия при помещении предмета.
     - `on_extract`: Действия при извлечении предмета.
2. **`output_slot` (Слот выдачи готовой продукции)**:
   - Игрок может только **забирать** предметы из этого слота. Положить туда что-либо невозможно (защита от замусоривания и дюпов).
   - `on_take`: Действия при взятии готового результата (звуки, эффекты, статистика).
3. **`progress_bar` (Индикатор прогресса)**:
   - Анимированная полоса / стрелка выполнения операции (переплавка, крио-синтез).
   - Направление: `RIGHT`, `LEFT`, `UP`, `DOWN`.
   - Заполняется динамически во времени.
4. **`fluid_tank` (Резервуар жидкостей / энергии)**:
   - Вертикальный или горизонтальный резервуар (например, уровень воды или маны в блоке).
   - Отображает текущее количество миллибакетов / единиц энергии и емкость: `{current}/{capacity} mB`.

---

## 4. Интеграция с кастомными блоками Oraxen / ItemsAdder (PDC Data Binding)

При открытии GUI блока (по клику ПКМ на кастомный блок в мире):
1. В `GuiContext` передается ссылка на блок `org.bukkit.block.Block`.
2. Все виджеты со свойством `persistent_key: "key_name"` автоматически:
   - Считывают свое начальное состояние из `PersistentDataContainer` (PDC) этого блока.
   - При клике игрока мгновенно обновляют данные в PDC блока в мире.
3. Это позволяет делать полностью сохраняемые настройки станков (режимы работы, права доступа, накопленную энергию, залитые жидкости) без внешних баз данных!

---

## 5. Полный пример YAML-конфигурации: Крио-Фабрикатор

```yaml
id: ice_fabricator_gui
title: "<gradient:#74B9FF:#0984E3>❄ Криогенный Фабрикатор ❄</gradient>"
rows: 4
target_block: "oraxen:ice_fabricator"

layout:
  type: panel
  x: 0
  y: 0
  width: 9
  height: 4
  children:
    # 1. Шаблон оформления
    - type: mask
      pattern:
        - "#########"
        - "#.I..P..O#"
        - "#.F.....M#"
        - "####R####"
      components:
        "#":
          type: filler
          material: "minecraft:blue_stained_glass_pane"
          name: " "

    # 2. Слот подачи воды
    - type: input_slot
      symbol: "I"
      allowed_items: ["minecraft:water_bucket"]
      on_insert:
        - action: "core:sound"
          sound: "item.bucket.empty"

    # 3. Шкала прогресса заморозки
    - type: progress_bar
      symbol: "P"
      direction: RIGHT
      material_empty: "minecraft:light_gray_dye"
      material_full: "minecraft:light_blue_dye"
      name: "<aqua>Крио-заморозка...</aqua>"
      duration_ticks: 60

    # 4. Слот выхода готового льда
    - type: output_slot
      symbol: "O"
      on_take:
        - action: "core:sound"
          sound: "block.glass.break"

    # 5. Слот катализатора
    - type: input_slot
      symbol: "F"
      allowed_items: ["oraxen:frost_crystal"]

    # 6. Селектор типа льда
    - type: cycle_button
      symbol: "M"
      persistent_key: "fabricator_mode"
      states:
        - id: "blue_ice"
          material: "minecraft:blue_ice"
          name: "<aqua>Режим: Синий лед</aqua>"
        - id: "packed_ice"
          material: "minecraft:packed_ice"
          name: "<blue>Режим: Плотный лед</blue>"

    # 7. Переключатель редстоун-контроля
    - type: toggle
      symbol: "R"
      persistent_key: "redstone_enabled"
      on_state:
        material: "minecraft:redstone_torch"
        name: "<red>Редстоун: ВКЛ</red>"
      off_state:
        material: "minecraft:lever"
        name: "<gray>Редстоун: ВЫКЛ (Всегда)</gray>"
```
