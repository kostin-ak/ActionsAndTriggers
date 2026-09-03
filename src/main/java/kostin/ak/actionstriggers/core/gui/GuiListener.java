package kostin.ak.actionstriggers.core.gui;

import kostin.ak.actionstriggers.api.gui.AATGuiHolder;
import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.Widget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * Слушатель событий инвентаря для безопасной обработки GUI и защиты от дюпов.
 */
public class GuiListener implements Listener {

    private final GuiRegistry registry;

    public GuiListener(GuiRegistry registry) {
        this.registry = registry;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AATGuiHolder holder)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int rawSlot = event.getRawSlot();
        int invSize = event.getInventory().getSize();

        // 1. Клик внутри верхнего инвентаря AAT GUI
        if (rawSlot >= 0 && rawSlot < invSize) {
            Widget widget = holder.getSlotWidgets().get(rawSlot);
            GuiContext guiContext = new GuiContext(player, holder);
            ClickContext clickContext = new ClickContext(guiContext, event);

            // Если виджет не назначен или неактивен, ищем активный виджет для этого слота
            if ((widget == null || !widget.isVisible(guiContext)) && holder.getGuiDefinition() != null) {
                for (int i = holder.getGuiDefinition().getWidgets().size() - 1; i >= 0; i--) {
                    Widget w = holder.getGuiDefinition().getWidgets().get(i);
                    if (w.occupiesSlot(rawSlot) && w.isVisible(guiContext)) {
                        widget = w;
                        holder.getSlotWidgets().put(rawSlot, w);
                        break;
                    }
                }
            }

            if (widget != null && widget.isVisible(guiContext)) {
                boolean cancel = widget.handleClick(clickContext);
                if (cancel) {
                    event.setCancelled(true);
                }
            } else {
                // Если слот пустой и не назначен активному виджету — защищаем от забирания/размещения
                event.setCancelled(true);
            }
        } else if (event.isShiftClick()) {
            // Защита от Shift-клика из инвентаря игрока в закрытые слоты GUI
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof AATGuiHolder)) {
            return;
        }

        int invSize = event.getInventory().getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < invSize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof AATGuiHolder holder)) {
            return;
        }

        if (event.getPlayer() instanceof Player player) {
            GuiContext ctx = new GuiContext(player, holder);
            if (holder.getGuiDefinition() instanceof GuiDefinition def) {
                for (Widget w : def.getWidgets()) {
                    w.onClose(ctx);
                }
            }

            // 1. Возврат ресурсов из слотов ввода (InputSlotWidget) и авто-забор готовой продукции (OutputSlotWidget)
            org.bukkit.inventory.Inventory inv = event.getInventory();
            for (int slot = 0; slot < inv.getSize(); slot++) {
                Widget widget = holder.getSlotWidgets().get(slot);

                // Если виджет не был привязан напрямую, ищем его в GuiDefinition
                if (widget == null && holder.getGuiDefinition() != null) {
                    for (Widget w : holder.getGuiDefinition().getWidgets()) {
                        if (w.occupiesSlot(slot) && w.isVisible(ctx)) {
                            widget = w;
                            break;
                        }
                    }
                }

                if (widget instanceof kostin.ak.actionstriggers.api.gui.widget.impl.InputSlotWidget inputWidget) {
                    org.bukkit.inventory.ItemStack item = inv.getItem(slot);
                    if (item != null && item.getType() != org.bukkit.Material.AIR) {
                        if (inputWidget.isPlaceholder(item)) {
                            continue;
                        }
                        inv.setItem(slot, null);
                        java.util.HashMap<Integer, org.bukkit.inventory.ItemStack> leftovers = player.getInventory().addItem(item);
                        for (org.bukkit.inventory.ItemStack drop : leftovers.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), drop);
                        }
                    }
                } else if (widget instanceof kostin.ak.actionstriggers.api.gui.widget.impl.OutputSlotWidget outputWidget) {
                    org.bukkit.inventory.ItemStack item = inv.getItem(slot);
                    if (item != null && item.getType() != org.bukkit.Material.AIR) {
                        if (outputWidget.isPlaceholder(item)) {
                            continue;
                        }
                        inv.setItem(slot, null);
                        java.util.HashMap<Integer, org.bukkit.inventory.ItemStack> leftovers = player.getInventory().addItem(item);
                        for (org.bukkit.inventory.ItemStack drop : leftovers.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), drop);
                        }
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f);
                    }
                }
            }
        }
    }
}
