# 📦 Практические примеры ActionsAndTriggers

В каталоге собраны конфигурации разной сложности. Каждый пример изолирован и содержит готовые для копирования файлы `guis/*.yml` и `triggers/*.yml`.

Это **справочные** примеры, а не автотесты: они показывают актуальный синтаксис и типовые приёмы. Там, где текущая реализация чего-то не умеет, это явно указано в README примера.

---

## 📑 Оглавление примеров

1. [🧭 01. Навигатор Миров (World Navigator GUI)](01_world_navigator_gui/README.md)
   - *Сложность*: **Базовая**
   - *Охват*: меню выбора миров, маски `9xN`, прозрачные слоты `transparent_slot`, ограничение в бою через условия `core:in_combat`/`core:not_in_combat`, звуки.

2. [⚔️ 02. Легендарное оружие и способности (Legendary Weapons)](02_legendary_weapons/README.md)
   - *Сложность*: **Средняя**
   - *Охват*: способности Катаны Бури и Кинжала Пустоты, кулдауны (`core:set_cooldown`, `core:on_cooldown`), боевой режим (`core:tag_combat`), частицы, проверка группы LuckPerms (`core:in_group`), импульс через `core:push` с `x`/`y`/`z`.

3. [❄️ 03. Криогенный Ледогенератор (Cryogenic Ice Fabricator)](03_cryogenic_ice_fabricator/README.md)
   - *Сложность*: **Продвинутая**
   - *Охват*: станок со слотами сырья (`input_slot`), катализатором и модулем ускорения (Tier 1-3 сокращают время цикла на 25/50/75%), анимированный прогресс-бар (`core:cryo_freeze`), переключатель режимов (`cycle_button`). Ограничения сохранности предметов описаны в README.

4. [⏱️ 04. Автоматические генераторы и таймеры (Generators & Timers)](04_automated_generators_and_timers/README.md)
   - *Сложность*: **Продвинутая**
   - *Охват*: фоновый такт ядра (`core:interval` с `{tick}`), циклы (`core:repeat` со счётчиком `{iteration}`), отложенные действия (`core:delay`), именованные задачи с отменой (`core:schedule`, `core:cancel_schedule`).

5. [🌟 05. Прогрессия игрока и LuckPerms (Player Progression)](05_player_progression_luckperms/README.md)
   - *Сложность*: **Средняя**
   - *Охват*: стартовый набор и титул при входе (`core:player_join`), плейсхолдеры `{player.group}` и `{player.prefix}`, достижения (`core:advancement_done` и `{advancement_key}`), повышение ранга (`core:set_group`), выдача прав (`core:add_permission`), фейерверк с настраиваемой только мощностью.

6. [📊 06. Вертикальный резервуар (Fluid Tank GUI)](06_advanced_fluid_and_tabs_gui/README.md)
   - *Сложность*: **Продвинутая**
   - *Охват*: `fluid_tank` (требует числового значения в сессии GUI), `cycle_button` со строковыми состояниями, `transparent_slot`. Вкладок в YAML нет: `tab_container` доступен только из Java.

---

## Общие замечания

- **ID триггеров** чувствительны к точному имени. Актуальный список — в [реестре](../ru/TRIGGERS_AND_ACTIONS.md); например, используется `core:advancement_done`, а не `core:player_advancement_done`.
- **Ключи контекста** тоже точные: у `core:player_interact` это `{clicked_block_id}`/`{block_material}`, у поломки/установки блока — `{block_id}`, у достижений — `{advancement_key}`, у предметов — `{item_id}`/`{main_hand_item_id}`. Опечатка в ключе не даёт ошибку — токен просто становится пустым.
- **Тики, а не секунды**: `core:potion_effect#duration` задаётся в тиках (200 = 10 секунд), `core:repeat#interval` — в тиках.
- **`core:push`** принимает `x`/`y`/`z`; `dx`/`dy`/`dz` есть только у `core:particle` (как смещения).
- Полный перечень актуальных возможностей и ограничений — в [реестре триггеров и действий](../ru/TRIGGERS_AND_ACTIONS.md) и в [списке известных проблем](../ru/POTENTIAL_ISSUES.md).
