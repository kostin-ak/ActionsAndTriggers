package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.AbstractWidget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Слот выдачи готовой продукции (OutputSlot).
 * Игрок может ТОЛЬКО извлекать предметы. Помещение предметов заблокировано.
 * Поддерживает наглядные плейсхолдеры подсказок.
 */
public class OutputSlotWidget extends AbstractWidget {

    private String placeholderMaterial = null;
    private String placeholderName = null;
    private List<String> placeholderLore = new ArrayList<>();
    private List<Action> onTake = new ArrayList<>();

    public OutputSlotWidget() {
        super(0, 0, 1, 1);
    }

    public OutputSlotWidget(int x, int y) {
        super(x, y, 1, 1);
    }

    public static final org.bukkit.NamespacedKey PLACEHOLDER_KEY =
            new org.bukkit.NamespacedKey("actionstriggers", "gui_placeholder");

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        if (placeholderMaterial != null && !placeholderMaterial.isEmpty()) {
            ItemStack placeholder = ActionTriggerAPI.getItems().resolveItem(placeholderMaterial);
            if (placeholder == null) placeholder = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            else placeholder = placeholder.clone();

            ItemMeta meta = placeholder.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(PLACEHOLDER_KEY, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
                if (placeholderName != null && !placeholderName.isEmpty()) {
                    meta.displayName(MiniMessage.miniMessage().deserialize(kostin.ak.actionstriggers.core.hook.PapiHook.parse(ctx.getPlayer(), placeholderName)));
                }
                if (placeholderLore != null && !placeholderLore.isEmpty()) {
                    List<Component> lore = new ArrayList<>();
                    for (String l : placeholderLore) {
                        lore.add(MiniMessage.miniMessage().deserialize(kostin.ak.actionstriggers.core.hook.PapiHook.parse(ctx.getPlayer(), l)));
                    }
                    meta.lore(lore);
                }
                placeholder.setItemMeta(meta);
            }
            matrix.put(toSlot(0, 0), placeholder);
        }
    }

    public boolean isPlaceholder(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(PLACEHOLDER_KEY, org.bukkit.persistence.PersistentDataType.BYTE);
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        InventoryClickEvent event = ctx.getEvent();
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // 1. Попытка положить предмет в слот выхода - БЛОКИРУЕМ!
        if (cursor != null && cursor.getType() != Material.AIR) {
            ctx.playSound("entity.villager.no", 1.0f, 1.0f);
            return true; // отменяем клик
        }

        // 2. Попытка забрать готовый предмет
        if (current != null && current.getType() != Material.AIR) {
            // Если в слоте лежит подсказка - блокируем извлечение
            if (isPlaceholder(current)) {
                return true;
            }

            ctx.executeActions(onTake);
            return false; // разрешаем забрать предмет
        }

        return true;
    }

    public String getPlaceholderMaterial() { return placeholderMaterial; }
    public void setPlaceholderMaterial(String placeholderMaterial) { this.placeholderMaterial = placeholderMaterial; }

    public String getPlaceholderName() { return placeholderName; }
    public void setPlaceholderName(String placeholderName) { this.placeholderName = placeholderName; }

    public List<String> getPlaceholderLore() { return placeholderLore; }
    public void setPlaceholderLore(List<String> placeholderLore) { this.placeholderLore = placeholderLore; }

    public List<Action> getOnTake() { return onTake; }
    public void setOnTake(List<Action> onTake) { this.onTake = onTake; }
}
