package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.AbstractWidget;
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
 * Слот для загрузки сырья/предметов в кастомные станки.
 * Проверяет разрешенные предметы и вызывает колбеки on_insert / on_extract.
 */
public class InputSlotWidget extends AbstractWidget {

    private List<String> allowedItems = new ArrayList<>();
    private String placeholderMaterial;
    private String placeholderName;

    private List<Action> onInsert = new ArrayList<>();
    private List<Action> onExtract = new ArrayList<>();

    public InputSlotWidget() {
        super(0, 0, 1, 1);
    }

    public InputSlotWidget(int x, int y) {
        super(x, y, 1, 1);
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        // Если в слоте еще ничего нет и настроен плейсхолдер — отрисовываем подсказку
        if (placeholderMaterial != null && !placeholderMaterial.isEmpty()) {
            ItemStack placeholder = ActionTriggerAPI.getItems().resolveItem(placeholderMaterial);
            if (placeholder == null) placeholder = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            else placeholder = placeholder.clone();

            ItemMeta meta = placeholder.getItemMeta();
            if (meta != null && placeholderName != null) {
                meta.displayName(MiniMessage.miniMessage().deserialize(placeholderName));
                placeholder.setItemMeta(meta);
            }
            matrix.put(toSlot(0, 0), placeholder);
        }
    }

    public boolean isPlaceholder(ItemStack item) {
        if (item == null || placeholderMaterial == null || placeholderMaterial.isEmpty()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        return placeholderName != null;
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        InventoryClickEvent event = ctx.getEvent();
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // 1. Попытка положить предмет из курсора
        if (cursor != null && cursor.getType() != Material.AIR) {
            String fullId = ActionTriggerAPI.getItems().getFullId(cursor);

            if (!isItemAllowed(fullId, cursor.getType())) {
                ctx.playSound("entity.villager.no", 1.0f, 1.0f);
                ctx.sendMessage("<red>Этот предмет нельзя поместить в данный слот!</red>");
                return true; // блокируем размещение
            }

            // Если в слоте лежал плейсхолдер — убираем его перед размещением реального предмета
            if (isPlaceholder(current)) {
                event.setCurrentItem(null);
            }

            ctx.executeActions(onInsert);
            return false; // разрешаем Bukkit переместить предмет в слот
        }

        // 2. Попытка забрать предмет из слота
        if (current != null && current.getType() != Material.AIR) {
            // Защита: нельзя забрать сам декоративный плейсхолдер!
            if (isPlaceholder(current)) {
                return true; // блокируем взятие плейсхолдера
            }

            ctx.executeActions(onExtract);
            return false; // разрешаем забрать реальный предмет
        }

        return false;
    }

    private boolean isItemAllowed(String fullId, Material material) {
        if (allowedItems == null || allowedItems.isEmpty()) return true;

        for (String allowed : allowedItems) {
            if (fullId != null && fullId.equalsIgnoreCase(allowed)) return true;
            if (material.name().equalsIgnoreCase(allowed) || ("minecraft:" + material.name()).equalsIgnoreCase(allowed)) return true;
        }
        return false;
    }

    public List<String> getAllowedItems() { return allowedItems; }
    public void setAllowedItems(List<String> allowedItems) { this.allowedItems = allowedItems; }

    public String getPlaceholderMaterial() { return placeholderMaterial; }
    public void setPlaceholderMaterial(String placeholderMaterial) { this.placeholderMaterial = placeholderMaterial; }

    public String getPlaceholderName() { return placeholderName; }
    public void setPlaceholderName(String placeholderName) { this.placeholderName = placeholderName; }

    public List<Action> getOnInsert() { return onInsert; }
    public void setOnInsert(List<Action> onInsert) { this.onInsert = onInsert; }

    public List<Action> getOnExtract() { return onExtract; }
    public void setOnExtract(List<Action> onExtract) { this.onExtract = onExtract; }
}
