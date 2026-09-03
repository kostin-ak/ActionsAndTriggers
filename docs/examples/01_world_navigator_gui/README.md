# 🧭 Пример 01: Навигатор Миров (World Navigator GUI)

## Описание сценария
Интерактивное меню выбора миров (Выживание, Креатив, Лобби), вызываемое при клике по компасу (`minecraft:compass`).
- **Связка систем**: Декларативный триггер (`core:player_interact`) + Графический интерфейс (`guis/*.yml`) + Проверка боевого режима (`core:not_in_combat`) + Звуки и эффекты.
- **Безопасность**: Запрещено открывать в бою (`allow_combat: false`), мгновенное закрытие при получении любого урона.

---

## Файлы конфигурации

### 1. `guis/world_navigator.yml`
```yaml
id: world_navigator
title: "<gradient:#70E1F5:#FFD194>❖ Навигатор Миров ❖</gradient>"
rows: 3
allow_combat: false
close_on_damage: true

mask:
  pattern:
    - "#########"
    - "#.S.L.C.#"
    - "####X####"
  components:
    "#":
      type: slot_cover
      material: "minecraft:gray_stained_glass_pane"
      name: " "

    ".":
      type: transparent_slot

    "S":
      type: button
      material: "minecraft:spruce_sapling"
      name: "<gradient:#55EFC4:#00B894>Дикие Земли (Выживание)</gradient>"
      lore:
        - "<gray>Классическое выживание</gray>"
        - "<gray>с опасными мобами и ресурсами.</gray>"
        - ""
        - "<yellow>▶ Нажмите для телепортации</yellow>"
      on_click:
        - action: "core:command"
          command: "mv tp world"
          as_console: false
        - action: "core:sound"
          sound: "entity.enderman.teleport"
          volume: 1.0
          pitch: 1.2
        - action: "core:close_gui"

    "L":
      type: button
      material: "minecraft:beacon"
      name: "<gradient:#FFA07A:#FF6347>Забытое Убежище (Лобби)</gradient>"
      lore:
        - "<gray>Безопасный спавн сервера.</gray>"
        - ""
        - "<yellow>▶ Нажмите для возврата</yellow>"
      on_click:
        - action: "core:command"
          command: "spawn"
          as_console: false
        - action: "core:close_gui"

    "C":
      type: button
      material: "minecraft:smooth_quartz"
      name: "<gradient:#74B9FF:#0984E3>Чертоги Созидания (Креатив)</gradient>"
      lore:
        - "<gray>Мир плоских участков</gray>"
        - "<gray>для свободного строительства.</gray>"
      on_click:
        - action: "core:command"
          command: "mv tp creative"
          as_console: false
        - action: "core:close_gui"

    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Закрыть навигатор</red>"
      on_click:
        - action: "core:close_gui"
```

### 2. `triggers/compass_navigator.yml`
```yaml
triggers:
  # Открытие меню компасом вне боя
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "minecraft:compass"
      - type: "core:not_in_combat"
    actions:
      - action: "core:open_gui"
        gui: "world_navigator"
      - action: "core:sound"
        sound: "item.book.page_turn"

  # Блокировка открытия в бою
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "minecraft:compass"
      - type: "core:in_combat"
    actions:
      - action: "core:sound"
        sound: "entity.villager.no"
      - action: "core:message"
        type: "actionbar"
        text: "<red>Навигатор заблокирован в бою! Подождите {player.combat_remaining} сек.</red>"
```
