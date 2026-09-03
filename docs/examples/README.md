# 📦 Практические примеры ActionsAndTriggers (Ready-to-Use Examples)

В этом каталоге собраны полностью рабочие, протестированные примеры конфигураций разной сложности. Каждый пример изолирован и содержит готовые для копирования в ваш сервер файлы `guis/*.yml` и `triggers/*.yml`.

---

## 📑 Оглавление примеров

1. [🧭 01. Навигатор Миров (World Navigator GUI)](01_world_navigator_gui/README.md)
   - *Сложность*: **Базовая**
   - *Охват*: Меню выбора миров, маски `9xN`, прозрачные слоты `transparent_slot`, проверка и блокировка в бою (`core:in_combat`, `core:not_in_combat`), звук перелистывания страниц.

2. [⚔️ 02. Легендарное оружие и способности (Legendary Weapons)](02_legendary_weapons/README.md)
   - *Сложность*: **Средняя**
   - *Охват*: Способности Катаны Бури и Кинжала Пустоты, кулдауны (`core:set_cooldown`, `core:on_cooldown`), наложение боевого режима (`core:tag_combat`), частицы, проверка группы LuckPerms (`core:in_group: "vip"`).

3. [❄️ 03. Криогенный Ледогенератор (Cryogenic Ice Fabricator)](03_cryogenic_ice_fabricator/README.md)
   - *Сложность*: **Продвинутая**
   - *Охват*: Производственный станок, слоты сырья (`input_slot`), катализатор, модуль ускорения (Tier 1-3, ускорение до -75%), анимированный прогресс-бар 10 FPS (`core:cryo_freeze`), переключатель режимов (`cycle_button`), 100% защита от потери ресурсов.

4. [⏱️ 04. Автоматические генераторы и таймеры (Generators & Timers)](04_automated_generators_and_timers/README.md)
   - *Сложность*: **Продвинутая**
   - *Охват*: Фоновый такт ядра (`core:interval`), циклы (`core:repeat` со счетчиком `{iteration}`), отложенные действия (`core:delay`), именованные задачи с возможностью отмены по урону (`core:schedule` и `core:cancel_schedule`).

5. [🌟 05. Прогрессия игрока и LuckPerms (Player Progression)](05_player_progression_luckperms/README.md)
   - *Сложность*: **Средняя**
   - *Охват*: Стартовый набор и MiniMessage-титул при первом входе (`core:player_join`), плейсхолдеры `{player.group}` и `{player.prefix}`, отслеживание достижений (`core:player_advancement_done`), автоматическое повышение ранга (`core:set_group`) и выдача пермишенов (`core:add_permission`).

6. [📊 06. Вертикальный Резервуар и Вкладки (Fluid Tank & Tabs GUI)](06_advanced_fluid_and_tabs_gui/README.md)
   - *Сложность*: **Продвинутая**
   - *Охват*: Вертикальный резервуар хладагента (`fluid_tank`), динамическое заполнение столбца слотов снизу вверх, интерактивный долив через `cycle_button`, полное отсутствие текстур стекла благодаря `transparent_slot`.
