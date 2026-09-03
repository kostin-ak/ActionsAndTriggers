package kostin.ak.actionstriggers.api.gui;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Контекст открытого графического окна.
 * Предоставляет доступ к игроку, сессии, пагинации и PDC кастомных блоков.
 */
public class GuiContext {

    private final Player player;
    private final AATGuiHolder holder;
    private final Block boundBlock;

    public GuiContext(@NotNull Player player, @NotNull AATGuiHolder holder) {
        this.player = player;
        this.holder = holder;
        this.boundBlock = holder.getBoundBlock();
    }

    public @NotNull Player getPlayer() { return player; }
    public @NotNull AATGuiHolder getHolder() { return holder; }
    public @Nullable Block getBoundBlock() { return boundBlock; }

    public int getPage() { return holder.getCurrentPage(); }
    public void setPage(int page) { holder.setCurrentPage(page); }

    public int getMaxPages() { return holder.getMaxPages(); }
    public void setMaxPages(int maxPages) { holder.setMaxPages(maxPages); }

    public String getActiveTab() { return holder.getActiveTab(); }
    public void setActiveTab(String tab) { holder.setActiveTab(tab); }

    // --- Сессионные переменные ---
    public Object getState(String key, Object def) {
        return holder.getSessionState().getOrDefault(key, def);
    }

    public void setState(String key, Object val) {
        holder.getSessionState().put(key, val);
    }

    // --- Data Binding с PersistentDataContainer (PDC) блока ---

    private @Nullable PersistentDataContainer getBlockPdc() {
        if (boundBlock != null && boundBlock.getState() instanceof TileState tile) {
            return tile.getPersistentDataContainer();
        }
        return null;
    }

    private void saveBlockPdc() {
        if (boundBlock != null && boundBlock.getState() instanceof TileState tile) {
            tile.update();
        }
    }

    public String getPdcString(String key, String def) {
        PersistentDataContainer pdc = getBlockPdc();
        if (pdc == null) return def;
        NamespacedKey nsk = new NamespacedKey("aat", key.toLowerCase());
        return pdc.getOrDefault(nsk, PersistentDataType.STRING, def);
    }

    public void setPdcString(String key, String value) {
        PersistentDataContainer pdc = getBlockPdc();
        if (pdc == null) return;
        NamespacedKey nsk = new NamespacedKey("aat", key.toLowerCase());
        pdc.set(nsk, PersistentDataType.STRING, value);
        saveBlockPdc();
    }

    public boolean getPdcBoolean(String key, boolean def) {
        PersistentDataContainer pdc = getBlockPdc();
        if (pdc == null) return def;
        NamespacedKey nsk = new NamespacedKey("aat", key.toLowerCase());
        Byte val = pdc.get(nsk, PersistentDataType.BYTE);
        return val == null ? def : val == 1;
    }

    public void setPdcBoolean(String key, boolean value) {
        PersistentDataContainer pdc = getBlockPdc();
        if (pdc == null) return;
        NamespacedKey nsk = new NamespacedKey("aat", key.toLowerCase());
        pdc.set(nsk, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
        saveBlockPdc();
    }

    public int getPdcInt(String key, int def) {
        PersistentDataContainer pdc = getBlockPdc();
        if (pdc == null) return def;
        NamespacedKey nsk = new NamespacedKey("aat", key.toLowerCase());
        return pdc.getOrDefault(nsk, PersistentDataType.INTEGER, def);
    }

    public void setPdcInt(String key, int value) {
        PersistentDataContainer pdc = getBlockPdc();
        if (pdc == null) return;
        NamespacedKey nsk = new NamespacedKey("aat", key.toLowerCase());
        pdc.set(nsk, PersistentDataType.INTEGER, value);
        saveBlockPdc();
    }
}
