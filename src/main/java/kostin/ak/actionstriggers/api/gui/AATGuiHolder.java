package kostin.ak.actionstriggers.api.gui;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Holder инвентаря для системы AAT GUI.
 * Позволяет идентифицировать окно, хранить привязку к блоку (PDC) и контекст отображения.
 */
public class AATGuiHolder implements InventoryHolder {

    private final String id;
    private final int rows;
    private final Player player;
    private final Block boundBlock;
    private Inventory inventory;

    private int currentPage = 1;
    private int maxPages = 1;
    private String activeTab = "";

    // Сессионные переменные окна
    private final Map<String, Object> sessionState = new HashMap<>();

    // Связка: номер слота -> виджет, управляющий этим слотом
    private final Map<Integer, kostin.ak.actionstriggers.api.gui.widget.Widget> slotWidgets = new HashMap<>();

    // Ссылка на определение интерфейса
    private kostin.ak.actionstriggers.core.gui.GuiDefinition guiDefinition;

    public AATGuiHolder(@NotNull String id, int rows, @NotNull Player player, @Nullable Block boundBlock) {
        this.id = id;
        this.rows = rows;
        this.player = player;
        this.boundBlock = boundBlock;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public String getId() { return id; }
    public int getRows() { return rows; }
    public Player getPlayer() { return player; }
    public @Nullable Block getBoundBlock() { return boundBlock; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int page) { this.currentPage = page; }

    public int getMaxPages() { return maxPages; }
    public void setMaxPages(int maxPages) { this.maxPages = Math.max(1, maxPages); }

    public String getActiveTab() { return activeTab; }
    public void setActiveTab(String activeTab) { this.activeTab = activeTab; }

    public Map<String, Object> getSessionState() { return sessionState; }
    public Map<Integer, kostin.ak.actionstriggers.api.gui.widget.Widget> getSlotWidgets() { return slotWidgets; }

    public kostin.ak.actionstriggers.core.gui.GuiDefinition getGuiDefinition() { return guiDefinition; }
    public void setGuiDefinition(kostin.ak.actionstriggers.core.gui.GuiDefinition guiDefinition) { this.guiDefinition = guiDefinition; }
}
