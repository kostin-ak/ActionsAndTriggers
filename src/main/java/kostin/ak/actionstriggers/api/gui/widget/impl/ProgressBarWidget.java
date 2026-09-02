package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.ActionsTriggers;
import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.gui.AATGuiHolder;
import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.AbstractWidget;
import kostin.ak.actionstriggers.api.gui.widget.Widget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
 * Поддерживает этапы технологического процесса, динамические температуры, статусы и анимацию в GUI.
 */
public class ProgressBarWidget extends AbstractWidget {

    public static class StageInfo {
        private final double threshold;
        private final String stage;
        private final String temp;
        private final String status;

        public StageInfo(double threshold, String stage, String temp, String status) {
            this.threshold = threshold;
            this.stage = stage;
            this.temp = temp;
            this.status = status;
        }

        public double getThreshold() { return threshold; }
        public String getStage() { return stage; }
        public String getTemp() { return temp; }
        public String getStatus() { return status; }
    }

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

    private final List<StageInfo> customStages = new ArrayList<>();
    private final List<Action> clickActions = new ArrayList<>();

    public ProgressBarWidget() {
        super(0, 0, 1, 1);
    }

    public ProgressBarWidget(int x, int y) {
        super(x, y, 1, 1);
    }

    @Override
    public void onOpen(@NotNull GuiContext ctx) {
        AATGuiHolder holder = ctx.getHolder();
        if (holder != null) {
            holder.getSessionState().putIfAbsent("progress_running", false);
            holder.getSessionState().putIfAbsent("progress_value", 0.0);
            holder.getSessionState().putIfAbsent("progress_percent", 0);
            holder.getSessionState().putIfAbsent("progress_status", "<green>В норме</green>");
            holder.getSessionState().putIfAbsent("progress_temp", "-180°C");
            holder.getSessionState().putIfAbsent("progress_stage", "Ожидание сырья");
        }
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

        StageInfo currentStage = getCurrentStage(progress);

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
                        .replace("{bar}", barStr)
                        .replace("{stage}", currentStage.getStage())
                        .replace("{temp}", currentStage.getTemp())
                        .replace("{status}", currentStage.getStatus());
                meta.displayName(MiniMessage.miniMessage().deserialize(nameStr));

                List<Component> lore = new ArrayList<>();
                if (!runningLoreTemplate.isEmpty()) {
                    for (String line : runningLoreTemplate) {
                        String formatted = line
                                .replace("{percent}", String.valueOf(percent))
                                .replace("{bar}", barStr)
                                .replace("{stage}", currentStage.getStage())
                                .replace("{temp}", currentStage.getTemp())
                                .replace("{status}", currentStage.getStatus());
                        lore.add(MiniMessage.miniMessage().deserialize(formatted));
                    }
                } else {
                    lore.add(MiniMessage.miniMessage().deserialize("<gray>Прогресс: <aqua>" + percent + "%</aqua>"));
                    lore.add(MiniMessage.miniMessage().deserialize(barStr));
                    lore.add(MiniMessage.miniMessage().deserialize("<gray>Этап: <aqua>" + currentStage.getStage() + "</aqua>"));
                    lore.add(MiniMessage.miniMessage().deserialize("<white>Температура: <blue>" + currentStage.getTemp() + "</blue>"));
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
     * Запуск плавного анимированного процесса со стадиями
     */
    public void startProcess(@NotNull AATGuiHolder holder, int totalDurationTicks, @NotNull Runnable onComplete) {
        holder.getSessionState().put("progress_running", true);
        holder.getSessionState().put("progress_value", 0.0);

        final int interval = 2; // Каждые 2 тика (10 раз в секунду) для плавной анимации
        final int totalSteps = Math.max(1, totalDurationTicks / interval);

        new BukkitRunnable() {
            int currentStep = 0;

            @Override
            public void run() {
                currentStep++;
                double currentProgress = Math.min(1.0, (double) currentStep / totalSteps);
                holder.getSessionState().put("progress_value", currentProgress);

                StageInfo st = getCurrentStage(currentProgress);
                holder.getSessionState().put("progress_stage", st.getStage());
                holder.getSessionState().put("progress_temp", st.getTemp());
                holder.getSessionState().put("progress_status", st.getStatus());
                holder.getSessionState().put("progress_percent", (int) Math.round(currentProgress * 100));

                Player player = holder.getPlayer();
                boolean isOnline = player != null && player.isOnline();

                // Обновляем все виджеты окна в реальном времени (прогресс-бар, криостат и т.д.)
                if (isOnline && player.getOpenInventory().getTopInventory().equals(holder.getInventory())) {
                    Map<Integer, ItemStack> matrix = new HashMap<>();
                    GuiContext gCtx = new GuiContext(player, holder);
                    for (Widget w : holder.getSlotWidgets().values()) {
                        w.render(gCtx, matrix);
                    }
                    for (Map.Entry<Integer, ItemStack> entry : matrix.entrySet()) {
                        holder.getInventory().setItem(entry.getKey(), entry.getValue());
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
                    holder.getSessionState().put("progress_stage", "Ожидание сырья");
                    holder.getSessionState().put("progress_temp", "-180°C");
                    holder.getSessionState().put("progress_status", "<green>В норме</green>");
                    holder.getSessionState().put("progress_percent", 0);

                    // Выполняем полезное действие (выдача льда)
                    onComplete.run();

                    // Окончательный рендер интерфейса обратно в idle
                    if (isOnline && player.getOpenInventory().getTopInventory().equals(holder.getInventory())) {
                        Map<Integer, ItemStack> matrix = new HashMap<>();
                        GuiContext gCtx = new GuiContext(player, holder);
                        for (Widget w : holder.getSlotWidgets().values()) {
                            w.render(gCtx, matrix);
                        }
                        for (Map.Entry<Integer, ItemStack> entry : matrix.entrySet()) {
                            holder.getInventory().setItem(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
        }.runTaskTimer(ActionsTriggers.getInstance(), 0L, interval);
    }

    public StageInfo getCurrentStage(double progress) {
        if (!customStages.isEmpty()) {
            StageInfo current = customStages.get(0);
            for (StageInfo s : customStages) {
                if (progress >= s.getThreshold()) {
                    current = s;
                } else {
                    break;
                }
            }
            return current;
        }

        // Этапы по умолчанию для криогенного станка
        if (progress >= 1.0) return new StageInfo(1.0, "Завершено", "-273°C", "<green>Готово к извлечению</green>");
        if (progress >= 0.75) return new StageInfo(0.75, "Крио-закалка и стабилизация", "-270°C", "<light_purple>Полировка граней</light_purple>");
        if (progress >= 0.50) return new StageInfo(0.50, "Глубокая компрессия структуры", "-210°C", "<blue>Сжатие решётки</blue>");
        if (progress >= 0.25) return new StageInfo(0.25, "Молекулярная кристаллизация", "-120°C", "<aqua>Реакция катализатора</aqua>");
        return new StageInfo(0.0, "Закачка воды и первичное охлаждение", "-40°C", "<yellow>Впрыск сырья</yellow>");
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

    public void addCustomStage(double threshold, String stage, String temp, String status) {
        customStages.add(new StageInfo(threshold, stage, temp, status));
        customStages.sort(Comparator.comparingDouble(StageInfo::getThreshold));
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
