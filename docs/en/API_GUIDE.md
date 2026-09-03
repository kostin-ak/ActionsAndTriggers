# 💻 Developer Guide: AAT Java & Kotlin API

## 1. Introduction
In addition to declarative YAML configurations, **ActionsAndTriggers** provides a comprehensive **Fluent API** for assembling GUI menus, registering custom widgets, custom triggers, and actions directly in Java or Kotlin plugin code.

---

## 2. Assembling GUIs via Fluent API

### 2.1. Basic Menu with Buttons
```java
import kostin.ak.actionstriggers.api.gui.AATGui;
import kostin.ak.actionstriggers.api.gui.widget.Widgets;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class NavigationMenu {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public static void open(Player player) {
        AATGui.builder("navigator")
            .title(MM.deserialize("<gradient:#70e1f5:#ffd194>❖ World Navigator ❖</gradient>"))
            .rows(3)
            // Mask pattern layout
            .mask(Widgets.mask()
                .pattern(
                    "#########",
                    "#.S.L.C.#",
                    "####X####"
                )
                .filler('#', Material.GRAY_STAINED_GLASS_PANE)
                // Survival world button
                .button('S', Material.SPRUCE_SAPLING, b -> b
                    .name(MM.deserialize("<green>Wilderness</green>"))
                    .lore(MM.deserialize("<gray>Hardcore survival</gray>"))
                    .onClick(ctx -> {
                        ctx.getPlayer().performCommand("mv tp world");
                        ctx.playSound("entity.enderman.teleport", 1.0f, 1.0f);
                    })
                )
                // Lobby return button
                .button('L', "oraxen:astral_atlas", b -> b
                    .name(MM.deserialize("<gold>Sanctuary</gold>"))
                    .onClick(ctx -> ctx.getPlayer().performCommand("mv tp lobby"))
                )
                // Creative button
                .button('C', Material.SMOOTH_QUARTZ, b -> b
                    .name(MM.deserialize("<aqua>Creative Realm</aqua>"))
                    .onClick(ctx -> ctx.getPlayer().performCommand("mv tp creative"))
                )
                // Close button
                .button('X', Material.BARRIER, b -> b
                    .name(MM.deserialize("<red>Close</red>"))
                    .onClick(ctx -> ctx.getPlayer().closeInventory())
                )
            )
            .open(player);
    }
}
```

---

### 2.2. Paginated Collection Menu (PagedList)
```java
import kostin.ak.actionstriggers.api.gui.AATGui;
import kostin.ak.actionstriggers.api.gui.widget.Widgets;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.List;

public class PlayerShopMenu {

    public static void open(Player player, List<ShopOffer> offers) {
        AATGui.builder("shop")
            .title(ctx -> "<gold>Market</gold> <gray>(Page " + ctx.getPage() + "/" + ctx.getMaxPages() + ")</gray>")
            .rows(6)
            .pagedList(Widgets.pagedList()
                .bounds(1, 1, 7, 4)
                .items(offers, (offer, builder) -> {
                    builder.material(offer.getItem())
                        .name("<yellow>" + offer.getName() + "</yellow>")
                        .lore("<gray>Price: " + offer.getPrice() + " gems</gray>")
                        .onClick(ctx -> offer.buy(ctx.getPlayer()));
                })
                .prevButton(0, 5, Material.ARROW, "<yellow>◀ Previous</yellow>")
                .nextButton(8, 5, Material.ARROW, "<yellow>Next ▶</yellow>")
            )
            .open(player);
    }
}
```

---

### 2.3. Machine Block & PDC Binding
```java
import kostin.ak.actionstriggers.api.gui.AATGui;
import kostin.ak.actionstriggers.api.gui.widget.Widgets;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class MachineGui {

    public static void open(Player player, Block machineBlock) {
        AATGui.builder("machine_panel")
            .rows(3)
            .bindBlock(machineBlock) // Binds context to block in world
            // Operating mode toggle
            .widget(4, 1, Widgets.toggle()
                .key("overdrive_mode") // reads/writes boolean to tile PDC
                .on(Material.LIME_DYE, "<green>Overdrive: ON</green>")
                .off(Material.GRAY_DYE, "<gray>Overdrive: OFF</gray>")
                .onChange((ctx, state) -> {
                    ctx.sendMessage("<yellow>Mode switched to: " + state + "</yellow>");
                })
            )
            // Input hopper slot
            .widget(2, 1, Widgets.inputSlot()
                .filter(item -> item.getType().name().contains("ORE"))
                .onInsert((ctx, item) -> {
                    ctx.playSound("block.anvil.place", 0.5f, 1.5f);
                })
            )
            .open(player);
    }
}
```

---

## 3. Creating Custom Widgets

To author a custom widget, simply extend `AbstractWidget`:

```java
public class MyCustomRadarWidget extends AbstractWidget {

    @Override
    public void render(GuiContext ctx, Map<Integer, ItemStack> matrix) {
        matrix.put(getY() * 9 + getX(), createRadarIcon(ctx));
    }

    @Override
    public boolean handleClick(ClickContext ctx) {
        ctx.getPlayer().sendMessage("Radar ping activated!");
        return true; // cancel event to prevent taking the item
    }
}
```
