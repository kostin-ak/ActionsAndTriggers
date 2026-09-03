# 💻 AAT Java & Kotlin API Guide

## 1. Введение
В дополнение к декларативным YAML-конфигурациям, **ActionsAndTriggers** предоставляет мощный **Fluent API** для создания GUI-интерфейсов, регистрации кастомных виджетов, триггеров и действий напрямую из кода плагинов на Java и Kotlin.

---

## 2. Создание GUI через Fluent API

### 2.1. Простое меню с кнопками
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
            .title(MM.deserialize("<gradient:#70e1f5:#ffd194>❖ Навигатор Миров ❖</gradient>"))
            .rows(3)
            // Добавление фоновой маски
            .mask(Widgets.mask()
                .pattern(
                    "#########",
                    "#.S.L.C.#",
                    "####X####"
                )
                .filler('#', Material.GRAY_STAINED_GLASS_PANE)
                // Кнопка перехода в мир выживания
                .button('S', Material.SPRUCE_SAPLING, b -> b
                    .name(MM.deserialize("<green>Дикие Земли</green>"))
                    .lore(MM.deserialize("<gray>Суровое выживание</gray>"))
                    .onClick(ctx -> {
                        ctx.getPlayer().performCommand("mv tp world");
                        ctx.playSound("entity.enderman.teleport", 1.0f, 1.0f);
                    })
                )
                // Кнопка возврата в лобби
                .button('L', "oraxen:astral_atlas", b -> b
                    .name(MM.deserialize("<gold>Забытое Убежище</gold>"))
                    .onClick(ctx -> ctx.getPlayer().performCommand("mv tp lobby"))
                )
                // Кнопка перехода в креатив
                .button('C', Material.SMOOTH_QUARTZ, b -> b
                    .name(MM.deserialize("<aqua>Чертоги Созидания</aqua>"))
                    .onClick(ctx -> ctx.getPlayer().performCommand("mv tp creative"))
                )
                // Кнопка закрытия
                .button('X', Material.BARRIER, b -> b
                    .name(MM.deserialize("<red>Закрыть</red>"))
                    .onClick(ctx -> ctx.getPlayer().closeInventory())
                )
            )
            .open(player);
    }
}
```

---

### 2.2. Меню с автопагинацией (PagedList)
```java
import kostin.ak.actionstriggers.api.gui.AATGui;
import kostin.ak.actionstriggers.api.gui.widget.Widgets;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.List;

public class PlayerShopMenu {

    public static void open(Player player, List<ShopOffer> offers) {
        AATGui.builder("shop")
            .title(ctx -> "<gold>Магазин</gold> <gray>(Стр. " + ctx.getPage() + "/" + ctx.getMaxPages() + ")</gray>")
            .rows(6)
            // Зона пагинации: со 2-го по 5-й ряд (слоты X=1..7, Y=1..4)
            .pagedList(Widgets.pagedList()
                .bounds(1, 1, 7, 4)
                .items(offers, (offer, builder) -> {
                    builder.material(offer.getItem())
                        .name("<yellow>" + offer.getName() + "</yellow>")
                        .lore("<gray>Цена: " + offer.getPrice() + " рубинов</gray>")
                        .onClick(ctx -> offer.buy(ctx.getPlayer()));
                })
                .prevButton(0, 5, Material.ARROW, "<yellow>◀ Назад</yellow>")
                .nextButton(8, 5, Material.ARROW, "<yellow>Вперед ▶</yellow>")
            )
            .open(player);
    }
}
```

---

### 2.3. Привязка к блоку станка и PDC (Data Binding)
```java
import kostin.ak.actionstriggers.api.gui.AATGui;
import kostin.ak.actionstriggers.api.gui.widget.Widgets;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class MachineGui {

    public static void open(Player player, Block machineBlock) {
        AATGui.builder("machine_panel")
            .rows(3)
            .bindBlock(machineBlock) // Связывание контекста с блоком в мире
            // Переключатель режима работы
            .widget(4, 1, Widgets.toggle()
                .key("overdrive_mode") // автоматически читает/пишет boolean в PDC блока
                .on(Material.LIME_DYE, "<green>Режим форсажа: ВКЛ</green>")
                .off(Material.GRAY_DYE, "<gray>Режим форсажа: ВЫКЛ</gray>")
                .onChange((ctx, state) -> {
                    ctx.sendMessage("<yellow>Режим изменен на: " + state + "</yellow>");
                })
            )
            // Слот загрузки сырья
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

## 3. Создание кастомных виджетов

Для создания собственного типа виджета достаточно отнаследоваться от `AbstractWidget`:

```java
public class MyCustomRadarWidget extends AbstractWidget {

    @Override
    public void render(GuiContext ctx, SlotMatrix matrix) {
        // Отрисовка предметов в переданную матрицу слотов
        matrix.set(getLocalX(), getLocalY(), createRadarIcon(ctx));
    }

    @Override
    public boolean handleClick(ClickContext ctx) {
        // Обработка клика
        ctx.getPlayer().sendMessage("Радар активирован!");
        return true; // отменить событие клика (не давать забрать предмет)
    }
}
```
Затем зарегистрировать виджет в фабрике:
```java
ActionTriggerAPI.getGuiRegistry().registerWidgetType("radar", MyCustomRadarWidget::new);
```
Теперь этот виджет можно использовать **и в Java-коде, и в YAML-конфигурациях**!
