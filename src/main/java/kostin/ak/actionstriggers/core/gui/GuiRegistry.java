package kostin.ak.actionstriggers.core.gui;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.gui.AATGuiHolder;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.Widget;
import kostin.ak.actionstriggers.core.CoreKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Центральный реестр и менеджер графических интерфейсов AAT GUI.
 */
public class GuiRegistry {

    private final Map<String, GuiDefinition> guis = new ConcurrentHashMap<>();

    public void register(@NotNull GuiDefinition definition) {
        guis.put(definition.getId().toLowerCase(), definition);
    }

    public @Nullable GuiDefinition get(@NotNull String id) {
        return guis.get(id.toLowerCase());
    }

    public void clear() {
        guis.clear();
    }

    public Map<String, GuiDefinition> getGuis() {
        return java.util.Collections.unmodifiableMap(guis);
    }

    public List<String> getAvailableIds() {
        return new java.util.ArrayList<>(guis.keySet());
    }

    public boolean openGui(@NotNull Player player, @NotNull String guiId) {
        return openGui(player, guiId, null);
    }

    public boolean openGui(@NotNull Player player, @NotNull String guiId, @Nullable Block boundBlock) {
        GuiDefinition def = get(guiId);
        if (def == null) {
            Bukkit.getLogger().warning("[A&T] Попытка открыть неизвестный GUI: " + guiId);
            return false;
        }

        AATGuiHolder holder = new AATGuiHolder(def.getId(), def.getRows(), player, boundBlock);
        holder.setGuiDefinition(def);

        String titleStr = def.getTitle().replace("{player}", player.getName());
        Component titleComponent = MiniMessage.miniMessage().deserialize(titleStr);

        Inventory inventory = Bukkit.createInventory(holder, def.getRows() * 9, titleComponent);
        holder.setInventory(inventory);

        GuiContext ctx = new GuiContext(player, holder);
        renderGui(ctx, def, inventory);

        player.openInventory(inventory);

        // Колбеки открытия
        for (Widget widget : def.getWidgets()) {
            widget.onOpen(ctx);
        }
        executeActions(player, boundBlock, def.getOnOpenActions());

        return true;
    }

    public void renderGui(@NotNull GuiContext ctx, @NotNull GuiDefinition def, @NotNull Inventory inventory) {
        AATGuiHolder holder = ctx.getHolder();
        holder.getSlotWidgets().clear();

        Map<Integer, ItemStack> matrix = new HashMap<>();

        for (Widget widget : def.getWidgets()) {
            widget.render(ctx, matrix);
        }

        for (Map.Entry<Integer, ItemStack> entry : matrix.entrySet()) {
            int slot = entry.getKey();
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, entry.getValue());
                // Привязываем активный (видимый) виджет к слоту для быстрого роутинга кликов
                for (int i = def.getWidgets().size() - 1; i >= 0; i--) {
                    Widget w = def.getWidgets().get(i);
                    if (w.occupiesSlot(slot) && w.isVisible(ctx)) {
                        holder.getSlotWidgets().put(slot, w);
                        break;
                    }
                }
            }
        }
    }

    private void executeActions(Player player, Block block, java.util.List<Action> actions) {
        if (actions == null || actions.isEmpty()) return;

        ExecutionContext execCtx = new ExecutionContext();
        execCtx.set(CoreKeys.PLAYER, player);
        execCtx.set(CoreKeys.LOCATION, player.getLocation());
        execCtx.set(CoreKeys.WORLD, player.getWorld().getName());
        if (block != null) {
            execCtx.set(CoreKeys.BLOCK, block);
        }
        for (Action action : actions) {
            if (action != null) {
                action.execute(execCtx);
            }
        }
    }
}
