# 🖥️ Виджетный GUI-движок AAT

## 1. Архитектурная концепция

GUI-движок — это дерево составных виджетов, которое рендерится в инвентарь Bukkit. Вместо ручного хардкода номеров слотов раскладка задаётся символьной маской 9×N.

- **Сетка**: виджеты используют `x` (0..8) и `y` (0..rows-1); абсолютный слот — `y * 9 + x`. Виджет можно разместить и одним полем `slot`.
- **Маска**: `mask.pattern` — список строк по 9 символов, `mask.components` сопоставляет символ с описанием виджета. `MaskWidget` разворачивает маску в карту «слот → виджет», которая используется для кликов и для возврата предметов при закрытии.
- **Рендеринг**: при открытии окна (и при `GuiContext#refreshGui`) каждый виджет пишет предметы в `Map<Integer, ItemStack>`, затем реестр переносит эту матрицу в инвентарь. Автоматически выполняются только действия `on_open`; действия `on_close` парсятся, но `GuiListener` их никогда не вызывает.
- **Материалы**: любое поле `material`/`item` принимает `minecraft:<id>`, `oraxen:<id>` или `itemsadder:<id>` и разрешается через `ItemRegistry`. Неизвестный ID заменяется ванильным запасным материалом.

## 2. Структура YAML (`guis/*.yml`)

```yaml
id: custom_bench          # используется в core:open_gui
title: "<gradient:#FFA07A:#FF6347>Верстак Инженера</gradient>"
rows: 3                   # ограничивается диапазоном 1..6
# target_block: "some_block_id"   # парсится и хранится, но рантайм это поле не читает

on_open:                  # опционально: выполняется сразу после открытия инвентаря
  - action: "core:sound"
    sound: "block.anvil.use"
on_close:                 # опционально: ПАРСИТСЯ, но НЕ выполняется (см. раздел 6)
  - action: "core:message"
    text: "closed"

mask:
  pattern:
    - "#########"
    - "#I#+#C#=#"
    - "####X####"
  components:
    "#":
      type: slot_cover
      material: "minecraft:gray_stained_glass_pane"
    "X":
      type: button
      material: "minecraft:barrier"
      name: "<red>Закрыть</red>"
      on_click:
        - action: "core:close_gui"

# Альтернатива маске: плоский список виджетов с явными координатами.
items:                    # "widgets" принимается как алиас
  close_button:
    type: button
    slot: 22
    material: "minecraft:barrier"
    name: "<red>Закрыть</red>"
    on_click:
      - action: "core:close_gui"
```

`allow_combat` и `close_on_damage` существуют как поля `GuiDefinition`, но `YamlGuiLoader` их не читает, и ни одна проверка в рантайме их не использует. Полагаться на них нельзя; подробности — в `POTENTIAL_ISSUES.md`.

## 3. Виджеты, настраиваемые из YAML

| Значение `type` | Поля, которые читает `YamlGuiLoader` | Поведение |
| :--- | :--- | :--- |
| `slot_cover`, `cover`, `blank`, `transparent_slot`, `invisible_slot` | `material`/`item`, `fallback`, `transparent` | Декоративные, некликабельные. `transparent_slot` не рендерит ничего (слот остаётся пустым). Без `material`/`item` берётся серая стеклянная панель; затем проверяются необязательные ключи `gui.slot_cover.*` в `config.yml`. |
| `filler` | `material`/`item`, `name`, `slots`, `x`/`y` | Кладёт один и тот же предмет в перечисленные слоты (или в один слот по `x`/`y`). |
| `button` и любой неизвестный `type` | `material`/`item`, `name`, `lore`, `amount`, `custom_model_data`, `actions`/`on_click` | Обычная кнопка. Неизвестный `type` молча становится кнопкой. |
| `input_slot` | `allowed_items`, `placeholder_material`, `placeholder_name`, `placeholder_lore`, `on_insert`, `on_extract` | Принимает только перечисленные предметы (пустой список — любые). Рисует плейсхолдер с PDC-тегом `actionstriggers:gui_placeholder`, который нельзя забрать. |
| `output_slot` | `placeholder_material`, `placeholder_name`, `placeholder_lore`, `on_take` | Только извлечение. Вставка и забирание плейсхолдера блокируются. |
| `progress_bar` | `idle_material`/`material`, `idle_name`/`name`, `idle_lore`/`lore`, `running_material`, `running_name`, `running_lore`, `bar_length`, `filled_color`, `empty_color`, `on_click` | Двухсостоянийный индикатор. Состояние «выполняется» включает `ProgressBarWidget#startProcess`, который вызывают встроенные станочные действия. Поддерживает `{percent}`, `{bar}`, `{stage}`, `{temp}`, `{status}` и PAPI. |
| `toggle` | `persistent_key`, `session_key`, `default`, `on_state` (`material`, `name`, `lore`), `off_state` (`material`, `name`, `lore`), `on_change` | Булев переключатель: хранится в PDC блока (`persistent_key`, нужен привязанный tile-блок) или в сессии GUI (`session_key`). |
| `cycle_button` | `persistent_key`, `session_key`, `states` (`id`, `material`/`item`, `name`, `lore`, `actions`) | Перебирает состояния по клику. Состояние хранится как **строка** — id состояния. |
| `fluid_tank` | `height`, `level_key`, `max_level`, `filled_material`, `empty_material`, `title` | Вертикальная шкала, заполняется снизу вверх. Читает **числовое** значение только из сессии GUI; `title` рендерится как есть (подстановки `{level}` нет). |
| `paged_list` | `width`, `height`, `prev_slot`, `next_slot`, `session_key` | Рамка пагинации. У `YamlGuiLoader` нет способа передать предметы, поэтому объявленный в YAML список всегда пуст. |

Общие поля любого виджета: `x`, `y` или `slot`, а также `condition`/`conditions` (дерево фильтров, вычисляемое от игрока и привязанного блока).

## 4. Виджеты, доступные только из Java

`TabContainerWidget` полностью реализован, но ветки `tab_container` в `YamlGuiLoader` нет. Запись `type: tab_container` в YAML провалится в `button`. Вкладки, как и источники предметов для `PagedListWidget`, собираются в коде:

```java
TabContainerWidget tabs = new TabContainerWidget();
tabs.addTab("general", 0, iconA, List.of(widgetA));
tabs.addTab("upgrades", 1, iconB, List.of(widgetB));
```

## 5. Состояние сессии и данные блока

- Каждый открытый инвентарь получает `AATGuiHolder` с картой `sessionState` и картой `slotWidgets`.
- `GuiContext` предоставляет типизированные помощники сессии (`getState`/`setState`) и помощники PDC блока (`getPdcString/Int/Boolean` и сеттеры).
- Помощники PDC работают, только если GUI открыт с привязанным блоком и состояние этого блока — `TileState` (сундук, печь, бочка и т.п.). Иначе это молчаливые no-op.
- `GuiContext#refreshGui` просит `GuiRegistry` перерисовать весь инвентарь; такие виджеты, как `CycleButtonWidget` и `ToggleWidget`, дополнительно перерисовывают свой слот сразу после клика.

## 6. Клики, драги и сохранность предметов

`GuiListener` обрабатывает `InventoryClickEvent`, `InventoryDragEvent` и `InventoryCloseEvent` с приоритетом `HIGHEST`.

- Клики по слотам, которые не заняты видимым виджетом, отменяются.
- Драги, затрагивающие верхний инвентарь, отменяются.
- Shift-клики из инвентаря игрока отменяются всегда, поэтому shift-вставка в `input_slot` невозможна.
- При закрытии предметы из `input_slot` и `output_slot`, не являющиеся плейсхолдерами, переносятся в инвентарь игрока; остаток выбрасывается через `dropItemNaturally`.
- Возврат предметов реализован **только** для `InventoryCloseEvent`. Обработчиков выключения плагина, остановки сервера и `close_on_damage` нет: предметы, оставленные в станке на момент рестарта, AAT не восстанавливает.
- Действия `on_close` в описании GUI парсятся, но не выполняются.

## 7. Текст и плейсхолдеры

- `ButtonWidget` заменяет `{player}`, известные ключи прогресса и **все ключи сессии** (`{<sessionKey>}`), затем вызывает PlaceholderAPI.
- `CycleButtonWidget`, `InputSlotWidget`, `OutputSlotWidget` и `ProgressBarWidget` вызывают PlaceholderAPI, но не подставляют ключи сессии (кроме собственных переменных шаблона).
- `ToggleWidget` и `FluidTankWidget` PlaceholderAPI не вызывают вовсе.
- Шаблоны прогресс-бара поддерживают `{percent}`, `{bar}`, `{stage}`, `{temp}`, `{status}`; по умолчанию используются встроенные крио-стадии, если кастомные не добавлены из Java.

## 8. Известные ограничения

Сейчас GUI-движок поддерживает из YAML меньше, чем обещала прежняя документация: нет вкладок из YAML, нет источника предметов для постраничных списков, нет рабочего числового резервуара из YAML, нет флагов боя, не выполняются `on_close`, нет восстановления предметов при остановке сервера. Детали, последствия и варианты исправления собраны в `docs/ru/POTENTIAL_ISSUES.md`.
