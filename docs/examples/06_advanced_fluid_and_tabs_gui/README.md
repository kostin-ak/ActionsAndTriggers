# 📊 Пример 06: Вертикальный резервуар (Fluid Tank GUI)

## Описание сценария

Продвинутый интерфейс, демонстрирующий виджеты `fluid_tank`, `cycle_button` и прозрачные слоты.

- **`fluid_tank`**: вертикальный резервуар, заполняющийся снизу вверх. Читает **числовое** значение из сессии GUI по ключу `level_key` (`coolant_level`). YAML не умеет записать число в сессию, поэтому без Java-кода или кастомного действия индикатор всегда показывает 0 — это ограничение текущей реализации, а не ошибка конфигурации.
- **`cycle_button`**: переключает строковое состояние режима (`coolant_mode`). На резервуар оно не влияет: `cycle_button` всегда хранит строку, а `fluid_tank` ожидает число.
- **`transparent_slot`** / **`slot_cover`**: чистая раскладка без лишних текстур.
- **Item Safety**: плейсхолдеры и декоративные предметы забрать нельзя.

Флаги `allow_combat`/`close_on_damage` в YAML не поддерживаются: ограничение боя задаётся условием `core:not_in_combat` в триггере. `title` резервуара рендерится как есть, подстановок `{level}`/`{max}` нет.

---

## Файлы конфигурации

### `guis/coolant_generator_gui.yml`
```yaml
id: coolant_generator_gui
title: "<gradient:#00CEC9:#0984E3>⚙ Генератор Охлаждения ⚙</gradient>"
rows: 4

mask:
  pattern:
    - "###T#####"
    - "###T#P#R#"
    - "###T#####"
    - "###F####X"
  components:
    "#":
      type: slot_cover
      material: "minecraft:gray_stained_glass_pane"
      name: " "

    "T":
      type: fluid_tank
      level_key: "coolant_level"
      max_level: 100
      filled_material: "minecraft:cyan_stained_glass_pane"
      empty_material: "minecraft:light_gray_stained_glass_pane"
      title: "<aqua>Хладагент</aqua>"

    "F":
      type: cycle_button
      session_key: "coolant_mode"
      states:
        - id: "eco"
          material: "minecraft:lime_dye"
          name: "<green>Режим: ЭКОНОМНЫЙ</green>"
        - id: "normal"
          material: "minecraft:yellow_dye"
          name: "<yellow>Режим: СТАНДАРТ</yellow>"
        - id: "turbo"
          material: "minecraft:red_dye"
          name: "<red>Режим: ТУРБО</red>"

    "P":
      type: button
      material: "minecraft:redstone_torch"
      name: "<green>Статус питания: НОРМА</green>"

    "R":
      type: button
      material: "minecraft:clock"
      name: "<yellow>Сбросить параметры</yellow>"
      on_click:
        - action: "core:sound"
          sound: "block.lever.click"
        - action: "core:message"
          type: "actionbar"
          text: "<yellow>Параметры охлаждения стабилизированы.</yellow>"

    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Закрыть панель</red>"
      on_click:
        - action: "core:close_gui"
```

### `triggers/open_coolant_gui.yml`
```yaml
triggers:
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{clicked_block_id}"
        value: "minecraft:dispenser"
      - type: "core:not_in_combat"
    actions:
      - action: "core:open_gui"
        gui: "coolant_generator_gui"
      - action: "core:sound"
        sound: "block.iron_trapdoor.open"
```

## Как заставить резервуар работать

Нужен источник числового значения в сессии GUI. Варианты:

1. Собственное действие на Java: получить `AATGuiHolder` из открытого инвентаря игрока и вызвать `holder.getSessionState().put("coolant_level", 75);` затем `GuiContext#refreshGui`.
2. Расширить `DefaultActionParsers` действием вида `core:set_session`, если такое действие будет добавлено в ядро.

Вкладки (`tab_container`) в этом примере не используются: YAML-загрузчик этот тип не разбирает. Вкладки собираются из Java API (`TabContainerWidget`).
