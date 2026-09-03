# ⚡ ActionsAndTriggers (AAT)

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Paper](https://img.shields.io/badge/Paper-1.21.4+-blue.svg?style=flat-square)](https://papermc.io/)
[![Tests](https://img.shields.io/badge/Тесты-34%2F34%20Пройдено-brightgreen.svg?style=flat-square)](docs/ru/BENCHMARK_REPORT.md)
[![Performance](https://img.shields.io/badge/Throughput-767k%20оп%2Fсек-purple.svg?style=flat-square)](docs/ru/BENCHMARK_REPORT.md)

**Высокопроизводительный событийно-ориентированный движок скриптов, планировщика задач и компонентных GUI-интерфейсов для серверов Paper и Purpur.**

[🇬🇧 Read in English](README.md) • [📖 Портал документации](docs/README.md) • [⚡ Триггеры и Действия](docs/ru/TRIGGERS_AND_ACTIONS.md) • [🖥️ GUI-Движок](docs/ru/GUI_ENGINE.md)

</div>

---

## 🌟 Почему ActionsAndTriggers?

Современные серверы Minecraft перегружены десятками разрозненных плагинов: один отвечает за меню, второй — за команды по условиям, третий — за способности предметов, четвертый — за таймеры.

**ActionsAndTriggers (AAT)** объединяет все эти механики в единую корпоративную экосистему:
- 🚀 **Zero-Allocation на горячих путях**: Собственный линейный index-of сканер обеспечивает **ускорение резолва плейсхолдеров в 3.12x раза** и снижает выделение памяти в куче на **95.6%** по сравнению с регулярными выражениями.
- 🎨 **Виджето-ориентированный GUI-движок**: Наглядные YAML-маски, анимированные 10 FPS прогресс-бары, вертикальные резервуары жидкостей, автопагинация, вкладки и поддержка бесшовных кастомных фонов со сдвигами глифов.
- 🛡️ **100% Защита ресурсов от потери**: Слоты автоматически распаковываются из масок. При закрытии окон, получении урона или перезагрузке сервера предметы **никогда не пропадут** — они гарантированно возвращаются в инвентарь или сбрасываются под ноги.
- ⏱️ **Встроенный планировщик и кулдауны**: Отложенные цепочки (`core:delay`), циклические повторы (`core:repeat`), именованные задачи (`core:schedule`) и персональные перезарядки способностей.
- 🌐 **Чистые интеграции**: Прозрачная работа с **Oraxen**, **ItemsAdder** и **PlaceholderAPI** без жестких зависимостей и вылетов.
- 🌍 **Полная интернационализация (i18n)**: Ноль захардкоженных строк. Централизованный словарь с автораспаковкой языковых бандлов (`messages_ru.yml`, `messages_en.yml`).

---

## 📊 Результаты бенчмарков производительности (Java 21)

Замеры проводились на **100 000 итераций** в изолированном тестовом стенде:

| Исследуемый компонент / Сценарий | До оптимизации (Legacy Regex) | После оптимизации AAT | Прирост производительности |
| :--- | :--- | :--- | :--- |
| **`ContextPlaceholderParser.resolve()`** | 4 074.8 нс/оп | **1 303.5 нс/оп** | **⚡ 3.12x быстрее (+212.6%)** |
| **`Нагрузка памяти Regex (Matcher Churn)`** | 57.47 МБ | **2.51 МБ** | **⚡ 95.6% сокращение памяти** |
| **`ExecutionContext Dispatch`** | 140.2 нс/оп | **93.1 нс/оп** | **⚡ 1.51x быстрее (+50.6%)** |

> Полный отчет замеров и профилирования доступен в [BENCHMARK_REPORT.md](docs/ru/BENCHMARK_REPORT.md).

---

## 🛠️ Быстрый старт

### 1. Установка
1. Поместите `ActionsAndTriggers.jar` в папку `plugins/` вашего сервера.
2. Убедитесь, что сервер работает на **Paper** или **Purpur** с **Java 21+**.
3. Запустите сервер. Плагин автоматически создаст рабочие папки, конфиги и файлы локализации.

### 2. Пример триггера (`plugins/ActionsAndTriggers/triggers/navigation.yml`)
```yaml
triggers:
  # Открытие Атласа по ПКМ вне боя
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
        text: "<red><bold>Связь разорвана в бою! Подождите {player.combat_remaining} сек.</bold></red>"
```

### 3. Пример GUI станка с ускорителем (`plugins/ActionsAndTriggers/guis/ice_fabricator.yml`)
```yaml
id: ice_fabricator_gui
title: "<gradient:#74B9FF:#0984E3>❄ Криогенный Ледогенератор</gradient>"
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
      placeholder_name: "<aqua>💧 Ведро с водой</aqua>"
    "C":
      type: input_slot
      allowed_items: ["oraxen:frost_crystal", "minecraft:amethyst_shard"]
      placeholder_material: "minecraft:amethyst_shard"
      placeholder_name: "<gradient:#E0C3FC:#8EC5FC>❄ Морозный кристалл</gradient>"
    ">":
      type: progress_bar
      idle_material: "minecraft:spectral_arrow"
      idle_name: "<aqua>⚡ Запустить заморозку</aqua>"
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
    "U":
      type: input_slot
      allowed_items: ["oraxen:cryo_accelerator_t1", "minecraft:netherite_ingot"]
      placeholder_material: "minecraft:redstone_torch"
      placeholder_name: "<gradient:#FFA07A:#FF6347>⚡ Модуль Ускорения</gradient>"
    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Закрыть</red>"
      on_click:
        - action: "core:close_gui"
```

---

## 💻 Fluent API для разработчиков (Java / Kotlin)

Создавайте интерфейсы прямо из кода ваших собственных плагинов:

```java
AATGui.builder("navigator")
    .title(MiniMessage.miniMessage().deserialize("<gradient:#70E1F5:#FFD194>Навигатор</gradient>"))
    .rows(3)
    .mask(Widgets.mask()
        .pattern(
            "#########",
            "#.S.L.C.#",
            "####X####"
        )
        .filler('#', Material.GRAY_STAINED_GLASS_PANE)
        .button('S', Material.SPRUCE_SAPLING, b -> b
            .name(Component.text("Выживание", NamedTextColor.GREEN))
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

## 📚 Разделы документации и Каталог примеров

- [📦 Каталог готовых примеров (Examples)](docs/examples/README.md) — 6 подробных рабочих сценариев: от навигации до криогенного фабрикатора.
- [🏛️ Архитектура и жизненный цикл ядра](docs/ru/ARCHITECTURE.md)
- [🖥️ Виджето-ориентированный GUI-движок](docs/ru/GUI_ENGINE.md)
- [🎨 Руководство по созданию GUI из YAML](docs/ru/CONFIG_GUI_TUTORIAL.md)
- [⚡ Полный реестр триггеров, фильтров и действий](docs/ru/TRIGGERS_AND_ACTIONS.md)
- [📜 Руководство по скриптингу триггеров из YAML](docs/ru/CONFIG_SCRIPTS_TUTORIAL.md)
- [💻 Руководство для Java и Kotlin разработчиков (API)](docs/ru/API_GUIDE.md)
- [📊 Отчет замеров производительности и оптимизации](docs/ru/BENCHMARK_REPORT.md)

---

## 🔨 Сборка и Модульная архитектура

Проект разделен на независимые модули по золотому стандарту индустрии:
- **`:aat-api`**: Полностью изолированный модуль публичного API (интерфейсы, контексты, виджеты, билдеры). Имеет НОЛЬ зависимостей от реализации ядра.
- **`:aat-core`**: Серверная реализация плагина, затенение команд (Lamp), загрузчик YAML, станки и мягкие интеграции (`LuckPerms`, `Oraxen`, `ItemsAdder`).

```bash
# 1. Собрать чистый легковесный API jar (для сторонних разработчиков)
./gradlew apiJar
# Результат: build/libs/ActionsAndTriggers-1.0-SNAPSHOT-api.jar (117 КБ)

# 2. Собрать полный плагин с ядром для сервера (с затенением зависимостей)
./gradlew shadowJar
# Результат: build/libs/ActionsAndTriggers-1.0-SNAPSHOT-all.jar (569 КБ)
```

---

## ⚖️ Двойное лицензирование (Dual-Licensing), Интеллектуальная собственность и Enterprise

Проект работает по профессиональной бизнес-модели **Двойного лицензирования (Dual-Licensing)**:

1. **Бесплатная редакция для сообщества (Community Edition)**:
   - Распространяется под лицензией **[PolyForm Noncommercial License 1.0.0](LICENSE)**.
   - Бесплатно для личного использования, изучения, тестирования и некоммерческих серверов без монетизации.
   - **Бесплатная самостоятельная сборка**: Любой разработчик или владелец сервера может бесплатно клонировать репозиторий и собрать плагин из исходного кода через Gradle (`./gradlew build`).
   - **Официальные готовые сборки (Pre-compiled Releases)**: Распространение и продажа готовых скомпилированных JAR-файлов на торговых площадках (SpigotMC, BuiltByBit, Polymart и др.) закреплены исключительно за автором (**Костиным Александром**). Распространение готовых бинарных файлов третьими лицами запрещено.
   - **Форки и авторство**: Разрешено форкать и дорабатывать для некоммерческих целей со строгим сохранением указания оригинального автора (**Костин Александр**).
   - **Исключение для аддонов**: Сторонние плагины, использующие только модуль `:aat-api`, могут распространяться под любой лицензией.

2. **Коммерческая редакция (Commercial & Enterprise Edition)**:
   - Использование плагина на коммерческих серверах (с автодонатом, магазином предметов, платными привилегиями или платным входом) требует официальной **Коммерческой Лицензии**, выдаваемой напрямую автором (**Костиным Александром**).
   - **Платная поддержка и кастомные разработки**: Написание уникальных экшенов под ваш проект, приватных триггеров и SLA-поддержка доступны напрямую от автора.
   - **Контакты для приобретения лицензии**: `kostin.ak@mail.ru`.

3. **Вклад сторонних разработчиков (Contributions & CLA)**:
   - Любые предложения изменений (Pull Requests, патчи, исправления) безотзывно передают исключительные авторские права автору (**Костину Александру**) согласно [CONTRIBUTING.md](CONTRIBUTING.md) и разделу 3.2 [LICENSE](LICENSE). Контрибуция не дает прав совладения или претензий на прибыль.
