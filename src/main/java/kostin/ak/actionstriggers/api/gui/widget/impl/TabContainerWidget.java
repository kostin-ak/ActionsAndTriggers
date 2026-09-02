package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.AbstractWidget;
import kostin.ak.actionstriggers.api.gui.widget.Widget;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контейнер вкладок (TabContainerWidget).
 * Позволяет переключать вкладки в одном инвентаре без мерцания и переоткрытия.
 */
public class TabContainerWidget extends AbstractWidget {

    public static class TabEntry {
        private String id;
        private int slot;
        private String iconMaterial;
        private String name;
        private Widget content;

        public TabEntry() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public int getSlot() { return slot; }
        public void setSlot(int slot) { this.slot = slot; }
        public String getIconMaterial() { return iconMaterial; }
        public void setIconMaterial(String iconMaterial) { this.iconMaterial = iconMaterial; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Widget getContent() { return content; }
        public void setContent(Widget content) { this.content = content; }
    }

    private List<TabEntry> tabs = new ArrayList<>();
    private String defaultTab;

    private final Map<Integer, TabEntry> slotToTab = new HashMap<>();

    public TabContainerWidget() {
        super(0, 0, 9, 6);
    }

    private TabEntry getActiveTabEntry(GuiContext ctx) {
        String active = ctx.getActiveTab();
        if (active == null || active.isEmpty()) {
            active = (defaultTab != null && !defaultTab.isEmpty()) ? defaultTab : (tabs.isEmpty() ? "" : tabs.get(0).getId());
            ctx.setActiveTab(active);
        }

        for (TabEntry tab : tabs) {
            if (tab.getId().equalsIgnoreCase(active)) {
                return tab;
            }
        }
        return tabs.isEmpty() ? null : tabs.get(0);
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx) || tabs.isEmpty()) return;

        slotToTab.clear();
        TabEntry activeEntry = getActiveTabEntry(ctx);
        MiniMessage mm = MiniMessage.miniMessage();

        // 1. Отрисовываем кнопки вкладок
        for (TabEntry tab : tabs) {
            slotToTab.put(tab.getSlot(), tab);

            ItemStack icon = ActionTriggerAPI.getItems().resolveItem(tab.getIconMaterial());
            if (icon == null) icon = new ItemStack(Material.BOOK);
            else icon = icon.clone();

            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                boolean isSelected = activeEntry != null && activeEntry.getId().equalsIgnoreCase(tab.getId());
                String title = isSelected ? ("<green>▶ " + tab.getName() + " ◀</green>") : ("<gray>" + tab.getName() + "</gray>");
                meta.displayName(mm.deserialize(title));
                icon.setItemMeta(meta);
            }

            matrix.put(tab.getSlot(), icon);
        }

        // 2. Отрисовываем контент активной вкладки
        if (activeEntry != null && activeEntry.getContent() != null) {
            activeEntry.getContent().render(ctx, matrix);
        }
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        int slot = ctx.getSlot();

        // Клик по кнопке вкладки
        TabEntry clickedTab = slotToTab.get(slot);
        if (clickedTab != null) {
            if (!clickedTab.getId().equalsIgnoreCase(ctx.getGuiContext().getActiveTab())) {
                ctx.getGuiContext().setActiveTab(clickedTab.getId());
                ctx.playSound("ui.button.click", 1.0f, 1.0f);
                refreshTabContent(ctx);
            }
            return true;
        }

        // Клик по контенту активной вкладки
        TabEntry activeEntry = getActiveTabEntry(ctx.getGuiContext());
        if (activeEntry != null && activeEntry.getContent() != null) {
            if (activeEntry.getContent().occupiesSlot(slot)) {
                return activeEntry.getContent().handleClick(ctx);
            }
        }

        return true;
    }

    private void refreshTabContent(ClickContext ctx) {
        Map<Integer, ItemStack> matrix = new HashMap<>();
        render(ctx.getGuiContext(), matrix);

        // Очищаем инвентарь и выставляем новое состояние
        for (int i = 0; i < 54; i++) {
            ctx.getEvent().getInventory().setItem(i, null);
        }
        for (Map.Entry<Integer, ItemStack> entry : matrix.entrySet()) {
            ctx.getEvent().getInventory().setItem(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean occupiesSlot(int slot) {
        if (slotToTab.containsKey(slot)) return true;
        TabEntry active = tabs.isEmpty() ? null : tabs.get(0);
        if (active != null && active.getContent() != null) {
            return active.getContent().occupiesSlot(slot);
        }
        return false;
    }

    public List<TabEntry> getTabs() { return tabs; }
    public void setTabs(List<TabEntry> tabs) { this.tabs = tabs; }

    public String getDefaultTab() { return defaultTab; }
    public void setDefaultTab(String defaultTab) { this.defaultTab = defaultTab; }
}
