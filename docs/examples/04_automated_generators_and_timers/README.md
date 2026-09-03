# ⏱️ Пример 04: Автоматические генераторы, таймеры и циклы

## Описание сценария
Использование фоновых таймеров ядра, отложенных действий и циклов:
1. **Фоновый пульс ядра (`core:interval`)**: срабатывает автоматически каждую секунду (20 тиков), создавая частицы над активным блоком алтаря и периодически восстанавливая ману игрокам.
2. **Циклическая спираль частиц (`core:repeat`)**: при активации кристалла запускает 10 циклов с шагом 5 тиков, передавая `{iteration}` для создания эффекта расширяющегося кольца.
3. **Отложенный взрыв маны (`core:delay`)**: задержка 3 секунды перед финальным звуковым и визуальным всплеском.
4. **Именованная задача с отменой (`core:schedule` & `core:cancel_schedule`)**: запуск 30-секундного баффа, который досрочно отменяется, если игрок получает урон в бою.

---

## Файлы конфигурации

### `triggers/mana_beacon_timers.yml`
```yaml
triggers:
  # 1. ЕЖЕСЕКУНДНЫЙ ТАКТ СТАНКА / АЛТАРЯ
  - trigger: "core:interval"
    actions:
      - action: "core:particle"
        particle: "ENCHANT"
        count: 5
        dx: 0.5
        dy: 0.5
        dz: 0.5
        speed: 0.1

  # 2. РИТУАЛ МАНЫ: ЦИКЛИЧЕСКИЙ ЭФФЕКТ И ОТЛОЖЕННЫЙ ВСПЛЕСК
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "minecraft:amethyst_cluster"
      - type: "core:not_on_cooldown"
        key: "mana_ritual"
    actions:
      - action: "core:set_cooldown"
        key: "mana_ritual"
        duration: 15

      # Цикл из 8 итераций каждые 5 тиков (0.25 сек)
      - action: "core:repeat"
        times: 8
        interval: 5
        actions:
          - action: "core:sound"
            sound: "block.note_block.chime"
            pitch: 1.5
          - action: "core:particle"
            particle: "SOUL_FIRE_FLAME"
            count: 15
            speed: 0.05
          - action: "core:message"
            type: "actionbar"
            text: "<gradient:#A29BFE:#6C5CE7>⚡ Фокусировка маны: Фаза {iteration}/8</gradient>"

      # Отложенное завершение ритуала через 45 тиков
      - action: "core:delay"
        ticks: 45
        actions:
          - action: "core:sound"
            sound: "entity.player.levelup"
            volume: 1.0
            pitch: 0.8
          - action: "core:potion_effect"
            effect: "REGENERATION"
            duration: 10
            amplifier: 1
          - action: "core:message"
            type: "title"
            text: "<gradient:#00CEC9:#81ECEC>✦ Ритуал Завершен ✦</gradient>"
            subtitle: "<gray>Вы получили благословение регенерации</gray>"

      # Именованная задача на снятие баффа через 200 тиков (10 секунд)
      - action: "core:schedule"
        id: "mana_shield_task"
        delay: 200
        actions:
          - action: "core:message"
            text: "<gray>Действие защитного барьера иссякло.</gray>"

  # 3. ОТМЕНА БАФФА ПРИ ПОЛУЧЕНИИ УРОНА
  - trigger: "core:player_damage"
    actions:
      - action: "core:cancel_schedule"
        id: "mana_shield_task"
```
