# ActionsAndTriggers (AAT)

<div align="center">

[![Java](https://img.shields.io/badge/Java-21+-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4%20--%2026.2-brightgreen.svg?style=flat-square)](https://papermc.io/)
[![Platform](https://img.shields.io/badge/Платформа-Paper%20%2F%20Purpur-blue.svg?style=flat-square)](https://purpurmc.org/)
[![License](https://img.shields.io/badge/Лицензия-PolyForm%20Noncommercial-yellow.svg?style=flat-square)](LICENSE)

**Модульный плагин скриптов, планировщика задач и графических меню для серверов Minecraft.**

[🇬🇧 Read in English](README.md) • [📖 Портал документации](docs/README.md) • [Триггеры и Действия](docs/ru/TRIGGERS_AND_ACTIONS.md) • [GUI-Движок](docs/ru/GUI_ENGINE.md)

</div>

---

## Описание

**ActionsAndTriggers (AAT)** объединяет в единую систему настройку меню, событийные триггеры, цепочки команд, таймеры и управление ресурсами. Вместо установки нескольких отдельных плагинов для интерфейсов, кулдаунов и интерактивных предметов, AAT позволяет настраивать всё через понятные YAML-файлы или использовать Java API.

### Основные возможности
- **Событийные триггеры**: запуск действий по клику, поломке блока, получению урона, крафту, достижениям и таймерам.
- **Планировщик и перезарядки**: задержки (`core:delay`), циклы со счетчиком (`core:repeat`), отложенные задачи (`core:schedule`) и персональные кулдауны игроков.
- **Интерфейсы по маске**: визуальная разметка меню символами. Поддержка входных/выходных слотов, шкал прогресса, индикаторов жидкостей, вкладок и постраничных списков.
- **Безопасность предметов**: все ресурсы во входных слотах привязаны к игроку. При закрытии меню, получении урона или перезагрузке сервера предметы возвращаются в инвентарь или сбрасываются под ноги, исключая дюпы и пропажу.
- **Мягкие интеграции**: поддержка кастомных блоков и предметов из **Oraxen** и **ItemsAdder**, интеграция с **PlaceholderAPI** и **LuckPerms**.
- **Локализация**: все сообщения и названия элементов вынесены в языковые файлы (`messages_ru.yml`, `messages_en.yml`).

---

## 📋 Требования и совместимость

| Компонент | Требование | Примечание |
| :--- | :--- | :--- |
| **Версия Minecraft** | **1.21.4 — 26.2** | Протестировано на стабильных релизах Paper и Purpur |
| **Ядро сервера** | **Paper**, **Purpur** | Необходимы для работы современной системы компонентов Adventure и асинхронных событий |
| **Среда Java** | **Java 21+** | Для серверов 26.2 рекомендуется Java 25+ |
| **Опциональные плагины** | **PlaceholderAPI**, **LuckPerms**, **Oraxen**, **ItemsAdder** | Подключаются автоматически при наличии на сервере |

---

## 🛠️ Быстрый старт

### 1. Установка
1. Поместите файл `ActionsAndTriggers.jar` в папку `plugins/` вашего сервера.
2. Убедитесь, что сервер запущен на **Paper** или **Purpur** с **Java 21+**.
3. Запустите сервер для автоматического создания конфигурационных файлов и словарей локализации.

### 2. Пример триггера (`plugins/ActionsAndTriggers/triggers/navigation.yml`)
```yaml
triggers:
  # Открытие меню по ПКМ, если игрок не находится в бою
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "oraxen:astral_atlas"
        ignore_case: true
      - type: "core:not_in_combat"
    actions:
      - action: "core:open_gui"
        gui: "astral_atlas"

  # Блокировка открытия во время боя
  - trigger: "core:player_interact"
    conditions:
      - type: "core:match"
        template: "{item_in_hand_id}"
        value: "oraxen:astral_atlas"
        ignore_case: true
      - type: "core:in_combat"
    actions:
      - action: "core:sound"
        sound: "entity.villager.no"
      - action: "core:message"
        type: "actionbar"
        text: "<red>Нельзя открывать во время боя! Подождите {player.combat_remaining} сек.</red>"
```

### 3. Пример интерфейса станка (`plugins/ActionsAndTriggers/guis/ice_fabricator.yml`)
```yaml
id: ice_fabricator_gui
title: "<gradient:#74B9FF:#0984E3>Криогенный Ледогенератор</gradient>"
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
      allowed_items: ["minecraft:water_bucket"]
      placeholder_material: "minecraft:bucket"
      placeholder_name: "<aqua>Слот для ведра с водой</aqua>"
    "C":
      type: input_slot
      allowed_items: ["oraxen:frost_crystal", "minecraft:amethyst_shard"]
      placeholder_material: "minecraft:amethyst_shard"
      placeholder_name: "<gradient:#E0C3FC:#8EC5FC>Слот катализатора</gradient>"
    ">":
      type: progress_bar
      idle_material: "minecraft:spectral_arrow"
      idle_name: "<aqua>Запустить заморозку</aqua>"
      running_material: "minecraft:clock"
      running_name: "<gradient:#74B9FF:#0984E3>Заморозка: {percent}%</gradient>"
      on_click:
        - action: "core:cryo_freeze"
          water_slot: 10
          crystal_slot: 12
          output_slot: 16
          upgrade_slot: 24
    "O":
      type: output_slot
    "U":
      type: input_slot
      allowed_items: ["oraxen:cryo_accelerator_t1", "minecraft:netherite_ingot"]
      placeholder_material: "minecraft:redstone_torch"
      placeholder_name: "<gradient:#FFA07A:#FF6347>Слот ускорителя</gradient>"
    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Закрыть</red>"
      on_click:
        - action: "core:close_gui"
```

---

## 💻 API для разработчиков (Java / Kotlin)

Сторонние плагины могут взаимодействовать с AAT через публичный модуль `:aat-api`:

```java
AATGui.builder("navigator")
    .title(MiniMessage.miniMessage().deserialize("<gradient:#70E1F5:#FFD194>Навигатор миров</gradient>"))
    .rows(3)
    .mask(Widgets.mask()
        .pattern(
            "#########",
            "#.S.L.C.#",
            "####X####"
        )
        .filler('#', Material.GRAY_STAINED_GLASS_PANE)
        .button('S', Material.SPRUCE_SAPLING, b -> b
            .name(Component.text("Дикий мир", NamedTextColor.GREEN))
            .onClick(ctx -> ctx.getPlayer().performCommand("mv tp world"))
        )
        .button('X', Material.BARRIER, b -> b
            .name(Component.text("Закрыть", NamedTextColor.RED))
            .onClick(ctx -> ctx.getPlayer().closeInventory())
        )
    )
    .open(player);
```

---

## 📚 Документация и примеры

- [Каталог готовых примеров](docs/examples/README.md) — 6 рабочих сценариев (навигация, легендарное оружие, генераторы ресурсов, прогрессия).
- [Архитектура плагина](docs/ru/ARCHITECTURE.md) — обзор подсистем и жизненного цикла.
- [Руководство по GUI-движку](docs/ru/GUI_ENGINE.md) — виды виджетов, маски и возврат предметов.
- [Создание GUI в YAML](docs/ru/CONFIG_GUI_TUTORIAL.md) — пошаговое руководство по настройке меню.
- [Реестр триггеров и действий](docs/ru/TRIGGERS_AND_ACTIONS.md) — полный список встроенных компонентов и параметров.
- [Скриптинг триггеров в YAML](docs/ru/CONFIG_SCRIPTS_TUTORIAL.md) — руководство по написанию механик.
- [Руководство по API](docs/ru/API_GUIDE.md) — регистрация кастомных действий и триггеров из кода.

---

## 🔨 Сборка проекта

Проект разделен на два независимых Gradle-модуля:
- **`:aat-api`**: публичные интерфейсы и билдеры без зависимостей от реализации ядра.
- **`:aat-core`**: реализация плагина, обработка команд и хуки сторонних плагинов.

```bash
# Сборка публичного API jar
./gradlew apiJar
# Результат: build/libs/ActionsAndTriggers-1.0-SNAPSHOT-api.jar

# Сборка готового плагина для сервера
./gradlew shadowJar
# Результат: build/libs/ActionsAndTriggers-1.0-SNAPSHOT-all.jar
```

---

## ⚖️ Лицензирование и условия использования

1. **Некоммерческое использование (Community)**:
   - Исходный код доступен по лицензии **[PolyForm Noncommercial License 1.0.0](LICENSE)**.
   - Бесплатно для некоммерческих серверов, локального тестирования и образовательных целей.
   - Сборка из исходного кода бесплатна для всех (`./gradlew build`).
   - Распространение готовых скомпилированных сборок третьими лицами запрещено.
   - Плагины-аддоны, использующие модуль `:aat-api`, могут распространяться под любой лицензией.

2. **Коммерческое использование**:
   - Использование на коммерческих серверах с монетизацией требует приобретения **Коммерческой лицензии** у правообладателя (**Костин Александр**).
   - Почта для связи: `kostin.ak@mail.ru`.

3. **Контрибьюция**:
   - Все пул-реквесты принимаются на условиях полной передачи авторских прав правообладателю (**Костин Александр**) согласно [CONTRIBUTING.md](CONTRIBUTING.md) и разделу 3.2 лицензии [LICENSE](LICENSE).
