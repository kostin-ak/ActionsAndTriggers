# ActionsAndTriggers (AAT)

<div align="center">

[![Java](https://img.shields.io/badge/Java-21+-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4%20--%2026.3-brightgreen.svg?style=flat-square)](https://papermc.io/)
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
- **Интерфейсы по маске**: визуальная разметка меню символами, входные/выходные слоты и анимированные шкалы прогресса. Вкладки и постраничные списки доступны из Java API.
- **Безопасность предметов**: при закрытии меню предметы из слотов ввода/выдачи возвращаются в инвентарь или сбрасываются под ноги. Обработчика остановки сервера нет, поэтому на перезапуск эта гарантия не распространяется.
- **Мягкие интеграции**: поддержка кастомных блоков и предметов из **Oraxen** и **ItemsAdder**, интеграция с **PlaceholderAPI** и **LuckPerms**.
- **Локализация**: все сообщения и названия элементов вынесены в языковые файлы (`messages_ru.yml`, `messages_en.yml`).

---

## 📋 Требования и совместимость

| Компонент | Требование | Примечание |
| :--- | :--- | :--- |
| **Версия Minecraft** | **1.21.4 – 26.3** | Сборка против Paper API `1.21.4-R0.1-SNAPSHOT`. Проверено запуском на Paper 1.21.4, 1.21.11 и 26.3 |
| **Ядро сервера** | **Paper**, **Purpur** | Необходимы для работы современной системы компонентов Adventure и асинхронных событий |
| **Среда Java** | **Java 21+** | Серверы 1.21.x работают на Java 21; серверы 26.x требуют Java 25 |
| **Опциональные плагины** | **PlaceholderAPI**, **LuckPerms**, **Oraxen**, **ItemsAdder** | Подключаются автоматически при наличии на сервере |

### Проверенная совместимость (0.1.0-beta)

- **Минимум:** Paper 1.21.4 (`api-version: '1.21'`).
- **Максимум проверенного:** Paper 26.3 (build 253).
- **Запуски:** Paper 1.21.4 build 232, 1.21.11 build 132, 26.3 build 253. Плагин включился, `/aat list` показал 29 действий, 21 триггер и 27 фильтров на всех трёх.
- **Проверка компиляции:** Paper API 1.21.4, 1.21.5, 1.21.8, 1.21.11.
- **Не проверено:** Purpur и другие форки (тот же Bukkit/Paper API, ожидаемо работают) и версии ниже 1.21.4.

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
// импорты опущены для краткости
ButtonWidget close = new ButtonWidget(0, 0);
close.setMaterialStr("minecraft:barrier");
close.setName("<red>Закрыть</red>");
close.setActions(List.of(context -> {
    context.get(CoreKeys.PLAYER).closeInventory();
    return true;
}));

Map<Character, Widget> components = Map.of(
        '#', new SlotCoverWidget(0, 0),
        'X', close
);
MaskWidget mask = new MaskWidget(
        List.of("#########", "####X####", "#########"),
        components
);

AATGui.builder("navigator")
        .title("<gradient:#70E1F5:#FFD194>Навигатор миров</gradient>")
        .rows(3)
        .mask(mask)
        .open(player);
```

Кастомные действия, фильтры и триггеры регистрируются через
`ActionTriggerAPI.getRegistrar()` фабриками `@ConfigAction` / `@ConfigFilter`
и наследниками `BukkitEventTrigger`. Компилируемые примеры — в
[руководстве по API](docs/ru/API_GUIDE.md).

---

## 📚 Документация и примеры

- [Каталог примеров](docs/examples/README.md) — 6 сценариев (навигация, легендарное оружие, станки, генераторы, прогрессия) с пометками об актуальных ограничениях.
- [Архитектура плагина](docs/ru/ARCHITECTURE.md) — обзор подсистем и жизненного цикла.
- [Руководство по GUI-движку](docs/ru/GUI_ENGINE.md) — виды виджетов, маски и возврат предметов.
- [Создание GUI в YAML](docs/ru/CONFIG_GUI_TUTORIAL.md) — пошаговое руководство по настройке меню.
- [Реестр триггеров и действий](docs/ru/TRIGGERS_AND_ACTIONS.md) — полный список встроенных компонентов и параметров.
- [Скриптинг триггеров в YAML](docs/ru/CONFIG_SCRIPTS_TUTORIAL.md) — руководство по написанию механик.
- [Руководство по API](docs/ru/API_GUIDE.md) — регистрация кастомных действий и триггеров из кода.
- [Известные проблемы](docs/ru/POTENTIAL_ISSUES.md) — зафиксированные расхождения между кодом, YAML и документацией.

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
