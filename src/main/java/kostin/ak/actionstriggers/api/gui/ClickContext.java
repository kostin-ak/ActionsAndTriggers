package kostin.ak.actionstriggers.api.gui;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreKeys;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контекст клика игрока по слоту или виджету инвентаря.
 */
public class ClickContext {

    private final GuiContext guiContext;
    private final InventoryClickEvent event;
    private final int slot;
    private final ClickType clickType;

    public ClickContext(@NotNull GuiContext guiContext, @NotNull InventoryClickEvent event) {
        this.guiContext = guiContext;
        this.event = event;
        this.slot = event.getSlot();
        this.clickType = event.getClick();
    }

    public @NotNull GuiContext getGuiContext() { return guiContext; }
    public @NotNull InventoryClickEvent getEvent() { return event; }
    public @NotNull Player getPlayer() { return guiContext.getPlayer(); }
    public int getSlot() { return slot; }
    public @NotNull ClickType getClickType() { return clickType; }

    public @Nullable ItemStack getCurrentItem() { return event.getCurrentItem(); }
    public @Nullable ItemStack getCursorItem() { return event.getCursor(); }

    public boolean isLeftClick() { return event.isLeftClick(); }
    public boolean isRightClick() { return event.isRightClick(); }
    public boolean isShiftClick() { return event.isShiftClick(); }

    /**
     * Выполняет список AAT-экшенов в контексте кликнувшего игрока.
     */
    public void executeActions(@Nullable List<Action> actions) {
        if (actions == null || actions.isEmpty()) return;

        ExecutionContext execCtx = new ExecutionContext();
        execCtx.set(CoreKeys.PLAYER, getPlayer());
        execCtx.set(CoreKeys.LOCATION, getPlayer().getLocation());
        execCtx.set(CoreKeys.WORLD, getPlayer().getWorld().getName());
        if (guiContext.getBoundBlock() != null) {
            execCtx.set(CoreKeys.BLOCK, guiContext.getBoundBlock());
        }
        for (Action action : actions) {
            if (action != null) {
                action.execute(execCtx);
            }
        }
    }

    public void playSound(String soundKey, float volume, float pitch) {
        try {
            getPlayer().playSound(getPlayer().getLocation(), soundKey, volume, pitch);
        } catch (Exception ignored) {}
    }

    public void sendMessage(String miniMessageText) {
        getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(miniMessageText));
    }
}
