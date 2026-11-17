# 💻 Developer Guide: AAT Java & Kotlin API

## 1. What the API actually offers

`:aat-api` contains the public contracts and the GUI builders: `ActionTriggerAPI`, registries (`ActionRegistry`, `FilterRegistry`, `TriggerRegistry`, `ItemRegistry`, `BlockRegistry`, `ScriptRegistry`), `Registrar`, `ExecutionContext`, `ContextKey`/`CoreKeys`, `Action`, `Filter`, `Filters`, `Trigger`, `BukkitEventTrigger`, and the GUI classes (`AATGui`, `GuiDefinition`, `AATGuiHolder`, `GuiContext`, `ClickContext`, `Widget`, `AbstractWidget`, twelve widget implementations, `PapiHook`).

`AATParser`, `ParserOptions` and the Oraxen/ItemsAdder providers live in `:aat-core`, although their packages are `...api.parser` and `...api.provider.impl`. An addon compiled against `:aat-api` only therefore cannot parse YAML scripts; the plugin itself parses `triggers/` and `guis/`. See `POTENTIAL_ISSUES.md`.

The API is initialised by `ActionsTriggers#onEnable` through `ActionTriggerAPI#init`. `ActionTriggerAPI` getters throw `IllegalStateException` when called before that, so an addon must run its registration in `onEnable` with `ActionsAndTriggers` in `depend`/`softdepend` and should check that the plugin is enabled.

## 2. Registering a custom action

Custom actions are static methods annotated with `@ConfigAction` that return an `Action` and take `Map<String, Object>`. The class is scanned once by `ActionRegistry#scanAndRegister`.

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
    @ActionParam(key = "amount", type = Double.class, description = "Health to restore")
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

// in onEnable, after ActionsAndTriggers is enabled:
ActionTriggerAPI.getRegistrar().registerActions(MyActions.class);
```

The YAML then uses `action: "myplugin:heal"` with `amount: 4`.

Parameter placeholders are resolved by `ActionParameters`, not by the parser. If you read `params.get("amount")` directly in the factory, a value like `{iteration}` is not expanded; wrap the map in `new ActionParameters(params, context)` inside the returned lambda to get placeholder support.

## 3. Registering a custom filter

```java
import kostin.ak.actionstriggers.api.filter.ConfigFilter;
import kostin.ak.actionstriggers.api.filter.Filter;
import kostin.ak.actionstriggers.api.filter.IFilterParsers;
import kostin.ak.actionstriggers.api.filter.Filters;
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

The factory signature is validated: the method must be `static`, must return `Filter`, and must accept exactly one `Map` parameter. Otherwise the registry logs a warning and skips it. `@ConfigAction` has the same requirement (static, returns `Action`, one `Map` parameter).

## 4. Registering a custom trigger

Extend `BukkitEventTrigger<T>` and declare which context keys the trigger exposes. `registerTriggers` also registers the class as a Bukkit listener, so `@EventHandler` methods are picked up.

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

// inside your JavaPlugin subclass:
ActionTriggerAPI.getRegistrar().registerTriggers(this, new MyDeathTrigger());
```

`handleEvent` already performs the fast-exit check (`hasSubscriptions`) and cancels the Bukkit event if an action called `context.cancel()`. A trigger that is not a `Listener` (like the core heartbeat) can extend `Trigger` directly and call `dispatch(context)` itself.

## 5. Programmatic GUIs

`AATGui.builder(...)` supports exactly these methods: `title(String)`, `title(Component)`, `rows(int)`, `bindTo(Block)`, `widget(Widget)`, `button(int slot, String material, Consumer<ButtonWidget>)`, `filler(int[] slots, String material)`, `mask(MaskWidget)`, `build()` and `open(Player)`.

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
        close.setName("<red>Close</red>");
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
                .title("<gradient:#70E1F5:#FFD194>Navigator</gradient>")
                .rows(3)
                .mask(mask)
                .open(player);
    }
}
```

Important details:

- `ButtonWidget` has no `onClick(...)`; use `setActions(List<Action>)`.
- `AATGui.Builder#button(int slot, ...)` builds a button at that absolute slot; the `Consumer<ButtonWidget>` receives the widget before it is added.
- `MaskWidget` places each component into the mask cell. A multi-cell widget (fluid tank, paged list, tab container) inside a mask is rendered once per mask cell and only its first item is used, so place such widgets through `widget(...)` with explicit coordinates instead.
- `bindTo(Block)` binds the GUI to a block: actions receive `CoreKeys.BLOCK` and PDC helpers work if the block state is a `TileState`.
- There is no builder for `PagedListWidget` or `ToggleWidget`; construct them and pass them to `widget(...)`.

## 6. Reading registries at runtime

```java
ActionTriggerAPI.getActions().asList();          // registered action ids
ActionTriggerAPI.getTriggers().asList();         // registered trigger ids
ActionTriggerAPI.getFilters().asList();          // registered filter ids
ActionTriggerAPI.getActions().getMetadata(key);  // @ActionParam metadata
ActionTriggerAPI.getTriggers().getProvidedContext(key);
ActionTriggerAPI.getScheduler();                 // delays, repeats, cooldowns
ActionTriggerAPI.getItems().resolveItem("oraxen:astral_atlas");
ActionTriggerAPI.getItems().getFullId(itemStack);
ActionTriggerAPI.getBlocks().getFullId(block);
ActionTriggerAPI.getScripts().getLoadedScripts();
```

`ItemRegistry`/`BlockRegistry` are backed by a plain `HashMap`, so register providers during `onEnable` rather than from asynchronous tasks.

## 7. Custom GUI widgets

Extend `AbstractWidget`. `render` writes items into the matrix; `handleClick` returns `true` to cancel the Bukkit click. `isVisible` already evaluates the widget's `condition` filter; call `super.isVisible(ctx)` or keep the `condition` field in mind.

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
        ctx.getPlayer().sendMessage("Radar ping");
        return true;
    }
}
```

A widget created this way can be used from Java, but the YAML loader has no branch for it. To make it YAML-configurable, the loader in `:aat-core` must be extended.
