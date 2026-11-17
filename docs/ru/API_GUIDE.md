# 💻 Руководство разработчика: Java и Kotlin API AAT

## 1. Что реально даёт API

`:aat-api` содержит публичные контракты и GUI-билдеры: `ActionTriggerAPI`, реестры (`ActionRegistry`, `FilterRegistry`, `TriggerRegistry`, `ItemRegistry`, `BlockRegistry`, `ScriptRegistry`), `Registrar`, `ExecutionContext`, `ContextKey`/`CoreKeys`, `Action`, `Filter`, `Filters`, `Trigger`, `BukkitEventTrigger` и классы GUI (`AATGui`, `GuiDefinition`, `AATGuiHolder`, `GuiContext`, `ClickContext`, `Widget`, `AbstractWidget`, двенадцать реализаций виджетов, `PapiHook`).

`AATParser`, `ParserOptions` и провайдеры Oraxen/ItemsAdder физически лежат в `:aat-core`, хотя их пакеты — `...api.parser` и `...api.provider.impl`. Поэтому аддон, скомпилированный только против `:aat-api`, не может парсить YAML-скрипты; сами `triggers/` и `guis/` разбирает плагин. Подробности — в `POTENTIAL_ISSUES.md`.

API инициализируется в `ActionsTriggers#onEnable` через `ActionTriggerAPI#init`. Геттеры `ActionTriggerAPI` бросают `IllegalStateException`, если вызваны раньше, поэтому регистрацию аддона нужно делать в `onEnable`, указав `ActionsAndTriggers` в `depend`/`softdepend`, и проверять, что плагин включён.

## 2. Регистрация своего действия

Своё действие — это статический метод с `@ConfigAction`, возвращающий `Action` и принимающий `Map<String, Object>`. Класс сканируется один раз в `ActionRegistry#scanAndRegister`.

```java
import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ConfigAction;
import kostin.ak.actionstriggers.api.action.IActionParsers;
import kostin.ak.actionstriggers.api.context.CoreKeys;
import kostin.ak.actionstriggers.api.meta.ActionParam;
import org.bukkit.entity.Player;

import java.util.Map;

public final class MyActions implements IActionParsers {

    private MyActions() {}

    @ConfigAction("myplugin:heal")
    @ActionParam(key = "amount", type = Double.class, description = "Сколько здоровья восстановить")
    public static Action heal(Map<String, Object> params) {
        double amount = Double.parseDouble(String.valueOf(params.getOrDefault("amount", "2.0")));
        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + amount));
            return true;
        };
    }
}

// в onEnable, после включения ActionsAndTriggers:
ActionTriggerAPI.getRegistrar().registerActions(MyActions.class);
```

В YAML это выглядит как `action: "myplugin:heal"` с параметром `amount: 4`.

Плейсхолдеры в параметрах раскрывает `ActionParameters`, а не парсер. Если читать `params.get("amount")` напрямую в фабрике, значение вида `{iteration}` не раскроется; чтобы получить подстановку, оберните мапу в `new ActionParameters(params, context)` внутри возвращаемой лямбды.

## 3. Регистрация своего фильтра

```java
import kostin.ak.actionstriggers.api.filter.ConfigFilter;
import kostin.ak.actionstriggers.api.filter.Filter;
import kostin.ak.actionstriggers.api.filter.IFilterParsers;
import kostin.ak.actionstriggers.api.context.CoreKeys;

public final class MyFilters implements IFilterParsers {

    private MyFilters() {}

    @ConfigFilter("myplugin:below_health")
    public static Filter belowHealth(Map<String, Object> params) {
        double threshold = Double.parseDouble(String.valueOf(params.getOrDefault("value", "10")));
        return context -> {
            var player = context.get(CoreKeys.PLAYER);
            return player != null && player.getHealth() < threshold;
        };
    }
}

ActionTriggerAPI.getRegistrar().registerFilters(MyFilters.class);
```

Сигнатура фабрики проверяется: метод должен быть `static`, возвращать `Filter` и принимать ровно один параметр `Map`. Иначе реестр пишет предупреждение и пропускает метод. У `@ConfigAction` те же требования: `static`, возвращает `Action`, один параметр `Map`.

## 4. Регистрация своего триггера

Наследуйтесь от `BukkitEventTrigger<T>` и объявите ключи контекста. `registerTriggers` заодно регистрирует класс как Bukkit-слушатель, поэтому методы `@EventHandler` начнут работать.

```java
import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.CoreKeys;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class MyDeathTrigger extends BukkitEventTrigger<PlayerDeathEvent> {

    public MyDeathTrigger() {
        declare(CoreKeys.PLAYER, PlayerDeathEvent::getEntity);
        declare(CoreKeys.LOCATION, e -> e.getEntity().getLocation());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        handleEvent(event);
    }

    @Override
    public NamespacedKey getKey() {
        return NamespacedKey.fromString("myplugin:player_died");
    }
}

// внутри вашего класса-наследника JavaPlugin:
ActionTriggerAPI.getRegistrar().registerTriggers(this, new MyDeathTrigger());
```

`handleEvent` уже делает быстрый выход при отсутствии подписчиков (`hasSubscriptions`) и отменяет событие Bukkit, если какое-то действие вызвало `context.cancel()`. Триггер, не являющийся `Listener` (как фоновый таймер ядра), может наследоваться от `Trigger` напрямую и сам вызывать `dispatch(context)`.

## 5. GUI из кода

`AATGui.builder(...)` поддерживает ровно такие методы: `title(String)`, `title(Component)`, `rows(int)`, `bindTo(Block)`, `widget(Widget)`, `button(int slot, String material, Consumer<ButtonWidget>)`, `filler(int[] slots, String material)`, `mask(MaskWidget)`, `build()` и `open(Player)`.

```java
import kostin.ak.actionstriggers.api.context.CoreKeys;
import kostin.ak.actionstriggers.api.gui.AATGui;
import kostin.ak.actionstriggers.api.gui.widget.Widget;
import kostin.ak.actionstriggers.api.gui.widget.impl.ButtonWidget;
import kostin.ak.actionstriggers.api.gui.widget.impl.MaskWidget;
import kostin.ak.actionstriggers.api.gui.widget.impl.SlotCoverWidget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NavigatorMenu {

    public static void open(org.bukkit.entity.Player player) {
        ButtonWidget close = new ButtonWidget(0, 0);
        close.setMaterialStr("minecraft:barrier");
        close.setName("<red>Закрыть</red>");
        close.setActions(List.of(context -> {
            context.get(CoreKeys.PLAYER).closeInventory();
            return true;
        }));

        SlotCoverWidget cover = new SlotCoverWidget(0, 0);
        cover.setTransparent(true);

        Map<Character, Widget> components = new HashMap<>();
        components.put('#', cover);
        components.put('X', close);

        MaskWidget mask = new MaskWidget(
                List.of("#########", "####X####", "#########"),
                components
        );

        AATGui.builder("navigator")
                .title("<gradient:#70E1F5:#FFD194>Навигатор</gradient>")
                .rows(3)
                .mask(mask)
                .open(player);
    }
}
```

Важные детали:

- У `ButtonWidget` нет `onClick(...)`; используйте `setActions(List<Action>)`.
- `AATGui.Builder#button(int slot, ...)` создаёт кнопку по абсолютному слоту; `Consumer<ButtonWidget>` получает виджет до добавления в GUI.
- `MaskWidget` кладёт каждый компонент в ячейку маски. Многослотовый виджет (резервуар, постраничный список, контейнер вкладок) внутри маски рендерится на каждую ячейку, и берётся только его первый предмет, поэтому такие виджеты размещайте через `widget(...)` с явными координатами.
- `bindTo(Block)` привязывает GUI к блоку: действия получают `CoreKeys.BLOCK`, а помощники PDC работают, если состояние блока — `TileState`.
- Готовых билдеров для `PagedListWidget` и `ToggleWidget` нет; создавайте их и передавайте в `widget(...)`.

## 6. Чтение реестров в рантайме

```java
ActionTriggerAPI.getActions().asList();          // зарегистрированные ID действий
ActionTriggerAPI.getTriggers().asList();         // зарегистрированные ID триггеров
ActionTriggerAPI.getFilters().asList();          // зарегистрированные ID фильтров
ActionTriggerAPI.getActions().getMetadata(key);  // метаданные @ActionParam
ActionTriggerAPI.getTriggers().getProvidedContext(key);
ActionTriggerAPI.getScheduler();                 // задержки, повторы, кулдауны
ActionTriggerAPI.getItems().resolveItem("oraxen:astral_atlas");
ActionTriggerAPI.getItems().getFullId(itemStack);
ActionTriggerAPI.getBlocks().getFullId(block);
ActionTriggerAPI.getScripts().getLoadedScripts();
```

`ItemRegistry`/`BlockRegistry` построены на обычном `HashMap`, поэтому провайдеры регистрируйте в `onEnable`, а не из асинхронных задач.

## 7. Свои GUI-виджеты

Наследуйтесь от `AbstractWidget`. `render` пишет предметы в матрицу, `handleClick` возвращает `true`, чтобы отменить клик Bukkit. `isVisible` уже учитывает фильтр `condition`; вызывайте `super.isVisible(ctx)` или помните про это поле.

```java
public final class RadarWidget extends AbstractWidget {

    public RadarWidget(int x, int y) { super(x, y, 1, 1); }

    @Override
    public void render(GuiContext ctx, Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;
        matrix.put(toSlot(0, 0), createRadarIcon(ctx));
    }

    @Override
    public boolean handleClick(ClickContext ctx) {
        ctx.getPlayer().sendMessage("Радар активирован");
        return true;
    }
}
```

Такой виджет доступен из Java, но ветки для него в YAML-загрузчике нет. Чтобы он настраивался из YAML, нужно расширить загрузчик в `:aat-core`.
