# 📊 Пример 06: Вертикальный Резервуар и Вкладки (Fluid Tank & Tabs GUI)

## Описание сценария
Продвинутый графический интерфейс, демонстрирующий новые компоненты ядра:
- **`fluid_tank`**: Вертикальный резервуар хладагента/жидкой маны, заполняющийся снизу вверх в зависимости от значения `session_key` или PDC блока.
- **`cycle_button`**: Долив хладагента по клику (циклическое переключение 25% -> 50% -> 75% -> 100%).
- **`transparent_slot`**: Чистый кастомный интерфейс без текстур стекла.
- **Item Safety**: Полная защита от вытаскивания заполнителей.

---

## Файлы конфигурации

### `guis/coolant_generator_gui.yml`
```yaml
id: coolant_generator_gui
title: "<gradient:#00CEC9:#0984E3>⚙ Генератор Охлаждения ⚙</gradient>"
rows: 4
allow_combat: false
close_on_damage: true

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
      level_key: "coolant_percentage"
      max_level: 100
      filled_material: "minecraft:cyan_stained_glass_pane"
      empty_material: "minecraft:light_gray_stained_glass_pane"
      title: "<aqua>Хладагент: {level}%</aqua>"

    "F":
      type: cycle_button
      session_key: "coolant_percentage"
      states:
        - id: "25"
          material: "minecraft:water_bucket"
          name: "<aqua>Уровень: 25% (Нажмите для долива)</aqua>"
        - id: "50"
          material: "minecraft:water_bucket"
          name: "<aqua>Уровень: 50% (Нажмите для долива)</aqua>"
        - id: "75"
          material: "minecraft:water_bucket"
          name: "<aqua>Уровень: 75% (Нажмите для долива)</aqua>"
        - id: "100"
          material: "minecraft:water_bucket"
          name: "<aqua>Уровень: 100% (БАК ПОЛОН)</aqua>"

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
        template: "{block_type}"
        value: "DISPENSER"
      - type: "core:not_in_combat"
    actions:
      - action: "core:open_gui"
        gui: "coolant_generator_gui"
      - action: "core:sound"
        sound: "block.iron_trapdoor.open"
```
