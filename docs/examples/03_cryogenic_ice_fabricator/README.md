# ❄️ Пример 03: Криогенный Ледогенератор со слотом Ускорителя

## Описание сценария
Полноценный промышленный станок с комплексным интерфейсом:
- **Слот воды (Input)**: принимает ведра с водой (`minecraft:water_bucket`), возвращает пустое ведро.
- **Слот катализатора (Input)**: принимает морозные кристаллы (`oraxen:frost_crystal` или `minecraft:amethyst_shard`).
- **Слот ускорителя (Input)**: принимает модули ускорения трех уровней:
  * Tier 1 (Медь/Железо): ускорение на **+25%**.
  * Tier 2 (Золото/Изумруд): ускорение на **+50%**.
  * Tier 3 (Алмаз/Незерит): ускорение на **+75%**.
- **Кнопка режима (Cycle Button)**: переключает производство между Синим льдом (`blue_ice`), Плотным льдом (`packed_ice`) и Обычным льдом (`ice`).
- **Прогресс-бар (10 FPS)**: анимирует охлаждение камеры с выводом температуры (`-273°C`) и стадий синтеза.
- **Безопасность (Safety)**: закрытие меню возвращает все вещи игроку или выбрасывает на землю при переполненном инвентаре.

---

## Файлы конфигурации

### 1. `guis/ice_fabricator_gui.yml`
```yaml
id: ice_fabricator_gui
title: "<shift:-8><glyph:ice_fabricator_bg><shift:-164><gradient:#74B9FF:#0984E3>❄ Криогенный Фабрикатор ❄</gradient>"
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
      placeholder_lore:
        - "<gray>Поместите ведро с водой</gray>"

    "C":
      type: input_slot
      allowed_items:
        - "oraxen:frost_crystal"
        - "minecraft:amethyst_shard"
      placeholder_material: "minecraft:amethyst_shard"
      placeholder_name: "<gradient:#E0C3FC:#8EC5FC>❄ Морозный Катализатор</gradient>"
      placeholder_lore:
        - "<gray>Кристалл льда или аметист</gray>"

    ">":
      type: progress_bar
      idle_material: "minecraft:spectral_arrow"
      idle_name: "<aqua>⚡ Запустить Крио-Синтез</aqua>"
      idle_lore:
        - "<gray>Нажмите для начала процесса.</gray>"
      running_material: "minecraft:clock"
      running_name: "<gradient:#74B9FF:#0984E3>❄ Синтез: {percent}%</gradient>"
      running_lore:
        - "<gray>Температура: <aqua>-273.15°C</aqua></gray>"
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

### 2. `triggers/fabricator_block_click.yml`
```yaml
triggers:
  # Открытие фабрикатора при клике по блоку синего льда с шифтом
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{block_type}"
        value: "BLUE_ICE"
      - type: "core:not_in_combat"
    actions:
      - action: "core:open_gui"
        gui: "ice_fabricator_gui"
      - action: "core:sound"
        sound: "block.snow.step"
```
