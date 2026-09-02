package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.ActionsTriggers;
import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.gui.AATGuiHolder;
import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.AbstractWidget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Виджет интерактивной полосы прогресса (ProgressBar).
 * Поддерживает состояние ожидания (idle) и активного процесса (running) с динамической анимацией прогресса.
 */
public class ProgressBarWidget extends AbstractWidget {

    private String idleMaterial = "minecraft:arrow";
    private String idleName = "<gradient:#74B9FF:#0984E3><bold>Запуск процесса</bold></gradient>";
    private List<String> idleLore = new ArrayList<>();

    private String runningMaterial = "minecraft:clock";
    private String runningName = "<gradient:#74B9FF:#0984E3><bold>Обработка...</bold></gradient>";
    private List<String> runningLoreTemplate = new ArrayList<>();

    private int barLength = 10;
    private String filledChar = "■";
    private String emptyChar = "□";
    private String filledColor = "#74B9FF";
    private String emptyColor = "#636E72";

    private final List<Action> clickActions = new ArrayList<>();

    public ProgressBarWidget() {
        super(0, 0, 1, 1);
    }

    public ProgressBarWidget(int x, int y) {
        super(x, y, 1, 1);
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        AATGuiHolder holder = ctx.getHolder();
        boolean isRunning = Boolean.TRUE.equals(holder.getSessionState().get("progress_running"));
        double progress = 0.0;
        Object pObj = holder.getSessionState().get("progress_value");
        if (pObj instanceof Number num) {
            progress = Math.min(1.0, Math.max(0.0, num.doubleValue()));
        }

        ItemStack item;
        if (isRunning) {
            item = ActionTriggerAPI.getItems().resolveItem(runningMaterial);
            if (item == null) item = new ItemStack(Material.CLOCK);
            item = item.clone();

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                int percent = (int) Math.round(progress * 100);
                String barStr = buildBar(progress);

                String nameStr = runningName
                        .replace("{percent}", String.valueOf(percent))
                        .replace("{bar}", barStr);
                meta.displayName(MiniMessage.miniMessage().deserialize(nameStr));

                List<Component> lore = new ArrayList<>();
                if (!runningLoreTemplate.isEmpty()) {
                    for (String line : runningLoreTemplate) {
                        String formatted = line
                                .replace("{percent}", String.valueOf(percent))
                                .replace("{bar}", barStr);
                        lore.add(MiniMessage.miniMessage().deserialize(formatted));
                    }
                } else {
                    lore.add(MiniMessage.miniMessage().deserialize("<gray>Прогресс: <aqua>" + percent + "%</aqua>"));
                    lore.add(MiniMessage.miniMessage().deserialize(barStr));
                }
                meta.lore(lore);
                item.setItemMeta(meta);
            }
        } else {
            item = ActionTriggerAPI.getItems().resolveItem(idleMaterial);
            if (item == null) item = new ItemStack(Material.ARROW);
            item = item.clone();

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(MiniMessage.miniMessage().deserialize(idleName));
                if (!idleLore.isEmpty()) {
                    List<Component> lore = new ArrayList<>();
                    for (String line : idleLore) {
                        lore.add(MiniMessage.miniMessage().deserialize(line));
                    }
                    meta.lore(lore);
                }
                item.setItemMeta(meta);
            }
        }

        int slot = getY() * 9 + getX();
        matrix.put(slot, item);
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        AATGuiHolder holder = ctx.getGuiContext().getHolder();
        if (Boolean.TRUE.equals(holder.getSessionState().get("progress_running"))) {
            ctx.getPlayer().playSound(ctx.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            ctx.getPlayer().sendActionBar(MiniMessage.miniMessage().deserialize("<red>✖ Станок уже выполняет крио-заморозку! Ожидайте окончания.</red>"));
            return true;
        }

        // Запуск действий клика (например, core:cryo_freeze)
        ctx.executeActions(clickActions);
        return true;
    }

    /**
     * Запуск плавного анимированного процесса
     */
    public void startProcess(@NotNull AATGuiHolder holder, int totalDurationTicks, @NotNull Runnable onComplete) {
        holder.getSessionState().put("progress_running", true);
        holder.getSessionState().put("progress_value", 0.0);

        final int interval = 2; // Каждые 2 тика (10 раз в секунду) для плавной анимации
        final int totalSteps = Math.max(1, totalDurationTicks / interval);
        final int slot = getY() * 9 + getX();

        new BukkitRunnable() {
            int currentStep = 0;

            @Override
            public void run() {
                currentStep++;
                double currentProgress = Math.min(1.0, (double) currentStep / totalSteps);
                holder.getSessionState().put("progress_value", currentProgress);

                Player player = holder.getPlayer();
                boolean isOnline = player != null && player.isOnline();

                // Обновляем отображение слота в реальном времени, если инвентарь все еще открыт
                if (isOnline && player.getOpenInventory().getTopInventory().equals(holder.getInventory())) {
                    Map<Integer, ItemStack> matrix = new HashMap<>();
                    GuiContext gCtx = new GuiContext(player, holder);
                    render(gCtx, matrix);
                    ItemStack updatedItem = matrix.get(slot);
                    if (updatedItem != null) {
                        holder.getInventory().setItem(slot, updatedItem);
                    }

                    // Звуковые микро-эффекты каждые полсекунды (5 шагов)
                    if (currentStep % 5 == 0 && currentProgress < 1.0) {
                        player.playSound(player.getLocation(), Sound.BLOCK_POWDER_SNOW_STEP, 0.6f, 1.2f + (float) currentProgress * 0.5f);
                        if (holder.getBoundBlock() != null) {
                            holder.getBoundBlock().getWorld().spawnParticle(
                                    org.bukkit.Particle.SNOWFLAKE,
                                    holder.getBoundBlock().getLocation().add(0.5, 1.1, 0.5),
                                    5, 0.2, 0.2, 0.2, 0.02
                            );
                        }
                    }
                }

                if (currentStep >= totalSteps) {
                    cancel();
                    holder.getSessionState().put("progress_running", false);
                    holder.getSessionState().put("progress_value", 0.0);

                    // Выполняем полезное действие (выдача льда)
                    onComplete.run();

                    // Окончательный рендер слота обратно в idle
                    if (isOnline && player.getOpenInventory().getTopInventory().equals(holder.getInventory())) {
                        Map<Integer, ItemStack> matrix = new HashMap<>();
                        GuiContext gCtx = new GuiContext(player, holder);
                        render(gCtx, matrix);
                        ItemStack resetItem = matrix.get(slot);
                        if (resetItem != null) {
                            holder.getInventory().setItem(slot, resetItem);
                        }
                    }
                }
            }
        }.runTaskTimer(ActionsTriggers.getInstance(), 0L, interval);
    }

    private String buildBar(double progress) {
        int filledCount = (int) Math.round(progress * barLength);
        filledCount = Math.min(barLength, Math.max(0, filledCount));
        int emptyCount = barLength - filledCount;

        StringBuilder sb = new StringBuilder();
        sb.append("<").append(filledColor).append(">");
        for (int i = 0; i < filledCount; i++) sb.append(filledChar);
        sb.append("</").append(filledColor).append(">");

        sb.append("<").append(emptyColor).append(">");
        for (int i = 0; i < emptyCount; i++) sb.append(emptyChar);
        sb.append("</").append(emptyColor).append(">");
        return sb.toString();
    }

    public void setIdleMaterial(String idleMaterial) { this.idleMaterial = idleMaterial; }
    public void setIdleName(String idleName) { this.idleName = idleName; }
    public void setIdleLore(List<String> idleLore) { this.idleLore = idleLore; }
    public void setRunningMaterial(String runningMaterial) { this.runningMaterial = runningMaterial; }
    public void setRunningName(String runningName) { this.runningName = runningName; }
    public void setRunningLoreTemplate(List<String> runningLoreTemplate) { this.runningLoreTemplate = runningLoreTemplate; }
    public void setBarLength(int barLength) { this.barLength = barLength; }
    public void setFilledChar(String filledChar) { this.filledChar = filledChar; }
    public void setEmptyChar(String emptyChar) { this.emptyChar = emptyChar; }
    public void setFilledColor(String filledColor) { this.filledColor = filledColor; }
    public void setEmptyColor(String emptyColor) { this.emptyColor = emptyColor; }

    public void addClickAction(Action action) { this.clickActions.add(action); }
}
