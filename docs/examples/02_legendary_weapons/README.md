# ⚔️ Пример 02: Кастомное легендарное оружие (Legendary Weapons)

## Описание сценария
Реализация двух легендарных видов оружия с кастомными способностями:
1. **Катана Бури (`oraxen:storm_katana` или `minecraft:netherite_sword`)**:
   - При ПКМ наносит удар молнией, активирует Combat Tag на 15 секунд и уходит на перезарядку 6 секунд.
   - Во время кулдауна воспроизводит щелчок осечки и сообщает остаток времени.
2. **Кинжал Пустоты (`oraxen:void_dagger` или `minecraft:iron_sword`)**:
   - При приседании (Shift) + ПКМ телепортирует вперед на 7 блоков (`core:push`), спавнит портальные частицы и накладывает слепоту на врагов в радиусе.
   - Доступен только игрокам группы LuckPerms `vip` или `hero` (`core:in_group`).

---

## Файлы конфигурации

### `triggers/legendary_weapons.yml`
```yaml
triggers:
  # 1. КАТАНА БУРИ - УДАР МОЛНИЕЙ
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "oraxen:storm_katana"
      - type: "core:not_on_cooldown"
        key: "storm_strike"
    actions:
      - action: "core:set_cooldown"
        key: "storm_strike"
        duration: 6
      - action: "core:tag_combat"
        seconds: 15
      - action: "core:sound"
        sound: "entity.lightning_bolt.thunder"
        volume: 1.0
        pitch: 1.0
      - action: "core:particle"
        particle: "ELECTRIC_SPARK"
        count: 40
        dx: 0.5
        dy: 1.0
        dz: 0.5
        speed: 0.2
      - action: "core:command"
        command: "execute at {player} run summon lightning_bolt ^ ^ ^6"
        as_console: true
      - action: "core:message"
        type: "actionbar"
        text: "<yellow>⚡ Громовой раскат активирован!</yellow>"

  # 2. КАТАНА БУРИ - ОПОВЕЩЕНИЕ О КУЛДАУНЕ
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "oraxen:storm_katana"
      - type: "core:on_cooldown"
        key: "storm_strike"
    actions:
      - action: "core:sound"
        sound: "block.fire.extinguish"
        pitch: 1.8
      - action: "core:message"
        type: "actionbar"
        text: "<gray>Катана перезаряжается...</gray>"

  # 3. КИНЖАЛ ПУСТОТЫ - СКАЧОК СКВОЗЬ ПРОСТРАНСТВО (ТОЛЬКО ДЛЯ VIP)
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "oraxen:void_dagger"
      - type: "core:in_group"
        group: "vip"
      - type: "core:not_on_cooldown"
        key: "void_blink"
    actions:
      - action: "core:set_cooldown"
        key: "void_blink"
        duration: 8
      - action: "core:sound"
        sound: "entity.enderman.teleport"
      - action: "core:particle"
        particle: "PORTAL"
        count: 50
        dx: 0.3
        dy: 0.8
        dz: 0.3
      - action: "core:push"
        dy: 0.4
      - action: "core:potion_effect"
        effect: "SPEED"
        duration: 3
        amplifier: 2
      - action: "core:message"
        type: "actionbar"
        text: "<purple>❖ Пространственный скачок выполнен!</purple>"
```
