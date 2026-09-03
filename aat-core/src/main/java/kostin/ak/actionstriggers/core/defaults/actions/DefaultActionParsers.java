package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.*;
import kostin.ak.actionstriggers.api.meta.ActionParam;
import kostin.ak.actionstriggers.core.CoreActionParams;
import kostin.ak.actionstriggers.core.CoreKeys;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.Map;

/**
 * Класс, содержащий методы-парсеры для стандартных экшенов.
 * Этот класс автоматически сканируется в ActionRegistry.
 */
public final class DefaultActionParsers implements IActionParsers {

    // Единый инстанс для всего класса (экономия памяти)
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private DefaultActionParsers() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ========================================================================
    // 1. КОМАНДЫ И ВЗАИМОДЕЙСТВИЕ (Command, Message, Sound)
    // ========================================================================

    @ConfigAction("core:command")
    @ActionParam(key = CoreActionParams.COMMAND, type = String.class, required = true, description = "Текст выполняемой команды. По умолчанию: say Привет")
    @ActionParam(key = CoreActionParams.AS_CONSOLE, type = Boolean.class, description = "Выполнять от имени консоли (true) или от игрока (false). По умолчанию: true")
    public static Action parseCommand(Map<String, Object> params) {
        String defaultCmd = String.valueOf(params.getOrDefault(CoreActionParams.COMMAND, "say Привет"));
        boolean asConsole = Boolean.parseBoolean(String.valueOf(params.getOrDefault(CoreActionParams.AS_CONSOLE, "true")));

        return context -> {
            ActionParameters actionParams = new ActionParameters(params, context);
            String finalCmd = actionParams.getString(CoreActionParams.COMMAND, defaultCmd);
            Player player = context.get(CoreKeys.PLAYER);

            if (asConsole) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                return true;
            } else if (player != null && player.isOnline()) {
                player.performCommand(finalCmd);
                return true;
            }
            return false;
        };
    }

    @ConfigAction("core:message")
    @ActionParam(key = CoreActionParams.TEXT, type = String.class, required = true, description = "Основной текст сообщения (поддерживает MiniMessage). По умолчанию: <red>Текст не задан</red>")
    @ActionParam(key = CoreActionParams.TYPE, type = String.class, description = "Тип вывода сообщения: chat, actionbar или title. По умолчанию: chat")
    @ActionParam(key = CoreActionParams.SUBTITLE, type = String.class, description = "Подзаголовок (используется только если тип установлен на title). По умолчанию пустая строка")
    public static Action parseMessage(Map<String, Object> params) {
        String defaultText = String.valueOf(params.getOrDefault(CoreActionParams.TEXT, "<red>Текст не задан</red>"));
        String defaultSubtitle = String.valueOf(params.getOrDefault(CoreActionParams.SUBTITLE, ""));
        String type = String.valueOf(params.getOrDefault(CoreActionParams.TYPE, "chat")).toLowerCase();

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            ActionParameters actionParams = new ActionParameters(params, context);
            String text = actionParams.getString(CoreActionParams.TEXT, defaultText);
            String subtitle = actionParams.getString(CoreActionParams.SUBTITLE, defaultSubtitle);

            var component = MINI_MESSAGE.deserialize(text);

            switch (type) {
                case CoreActionParams.MessageTypes.ACTIONBAR -> player.sendActionBar(component);
                case CoreActionParams.MessageTypes.TITLE ->
                        player.showTitle(Title.title(component, MINI_MESSAGE.deserialize(subtitle)));
                default -> player.sendMessage(component);
            }
            return true;
        };
    }

    @ConfigAction("core:sound")
    @ActionParam(key = CoreActionParams.SOUND, type = String.class, required = true, description = "Ключ звука (namespace:id или старый Bukkit формат). По умолчанию: minecraft:entity.player.levelup")
    @ActionParam(key = CoreActionParams.VOLUME, type = Float.class, description = "Громкость воспроизведения звука. По умолчанию: 1.0")
    @ActionParam(key = CoreActionParams.PITCH, type = Float.class, description = "Высота/тональность звука. По умолчанию: 1.0")
    public static Action parseSound(Map<String, Object> params) {
        String defaultSound = String.valueOf(params.getOrDefault(CoreActionParams.SOUND, "minecraft:entity.player.levelup"));

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            ActionParameters actionParams = new ActionParameters(params, context);
            String sound = actionParams.getString(CoreActionParams.SOUND, defaultSound);
            float volume = actionParams.getFloat(CoreActionParams.VOLUME, 1.0f);
            float pitch = actionParams.getFloat(CoreActionParams.PITCH, 1.0f);

            // Фикс старого Bukkit формата
            if (!sound.contains(":") && sound.contains("_") && sound.toUpperCase().equals(sound)) {
                sound = "minecraft:" + sound.toLowerCase().replace("_", ".");
            }

            player.playSound(player.getLocation(), sound, volume, pitch);
            return true;
        };
    }

    // ========================================================================
    // 2. БОЕВАЯ СИСТЕМА И ЭФФЕКТЫ (Damage, Kill, PotionEffect)
    // ========================================================================

    @ConfigAction("core:damage")
    @ActionParam(key = CoreActionParams.AMOUNT, type = Double.class, required = true, description = "Количество наносимого игроку урона в полусердцах. По умолчанию: 1.0")
    public static Action parseDamage(Map<String, Object> params) {
        double defaultAmount = Double.parseDouble(String.valueOf(params.getOrDefault(CoreActionParams.AMOUNT, "1.0")));

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player != null && player.isOnline() && !player.isDead()) {
                ActionParameters actionParams = new ActionParameters(params, context);
                player.damage(actionParams.getDouble(CoreActionParams.AMOUNT, defaultAmount));
                return true;
            }
            return false;
        };
    }

    @ConfigAction("core:kill")
    public static Action parseKill(Map<String, Object> params) {
        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player != null && player.isOnline() && !player.isDead()) {
                player.setHealth(0.0);
                return true;
            }
            return false;
        };
    }
    @ConfigAction("core:potion_effect")
    @ActionParam(key = CoreActionParams.EFFECT, type = String.class, required = true, description = "Название эффекта зелья (например, speed, strength). По умолчанию: speed")
    @ActionParam(key = CoreActionParams.DURATION, type = Integer.class, description = "Длительность эффекта в тиках (20 тиков = 1 секунда). По умолчанию: 200")
    @ActionParam(key = CoreActionParams.AMPLIFIER, type = Integer.class, description = "Уровень/сила эффекта (0 равен I уровню, 1 равен II уровню). По умолчанию: 0")
    @ActionParam(key = CoreActionParams.PARTICLES, type = Boolean.class, description = "Отображать ли видимые частицы вокруг игрока. По умолчанию: true")
    public static Action parsePotionEffect(Map<String, Object> params) {
        String defaultEffect = String.valueOf(params.getOrDefault(CoreActionParams.EFFECT, "speed")).toLowerCase();

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            ActionParameters actionParams = new ActionParameters(params, context);
            String effectName = actionParams.getString(CoreActionParams.EFFECT, defaultEffect).toLowerCase();
            int duration = actionParams.getInt(CoreActionParams.DURATION, 200);
            int amplifier = actionParams.getInt(CoreActionParams.AMPLIFIER, 0);
            boolean particles = actionParams.getBoolean(CoreActionParams.PARTICLES, true);

            PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(effectName));
            if (type != null) {
                player.addPotionEffect(new PotionEffect(type, duration, amplifier, false, particles));
                return true;
            }
            return false;
        };
    }

    // ========================================================================
    // 3. ВИЗУАЛЫ И ПРЕДМЕТЫ (Particle, Firework, GiveItem, Teleport)
    // ========================================================================

    @ConfigAction("core:give_item")
    @ActionParam(key = CoreActionParams.MATERIAL, type = String.class, required = true, description = "Материал или ID предмета для выдачи (namespace:id). По умолчанию: minecraft:stone")
    @ActionParam(key = CoreActionParams.AMOUNT, type = Integer.class, description = "Количество выдаваемых предметов. По умолчанию: 1")
    public static Action parseGiveItem(Map<String, Object> params) {
        boolean ifAbsent = Boolean.parseBoolean(String.valueOf(params.getOrDefault("if_absent", params.getOrDefault("unique", "false"))));

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            ActionParameters actionParams = new ActionParameters(params, context);
            String rawMatKey = params.containsKey("item") ? "item" : CoreActionParams.MATERIAL;
            String materialStr = actionParams.getString(rawMatKey, "minecraft:stone");
            int amount = actionParams.getInt(CoreActionParams.AMOUNT, 1);

            if (ifAbsent) {
                for (ItemStack is : player.getInventory().getContents()) {
                    if (is == null || is.getType() == Material.AIR) continue;
                    String fullId = ActionTriggerAPI.getItems().getFullId(is);
                    if (fullId != null && fullId.equalsIgnoreCase(materialStr)) {
                        return false; // Уже есть в инвентаре, пропускаем выдачу
                    }
                }
            }

            // Магия! Используем наш новый Реестр. Он сам разберется, какой плагин дергать.
            ItemStack item = ActionTriggerAPI.getItems().resolveItem(materialStr);

            if (item != null) {
                item.setAmount(amount);
                player.getInventory().addItem(item);
                return true;
            } else {
                // Предмет не найден ни в одном провайдере
                Bukkit.getLogger().warning("[A&T] Попытка выдать неизвестный предмет: " + materialStr);
                return false;
            }
        };
    }

    @ConfigAction("core:particle")
    @ActionParam(key = CoreActionParams.PARTICLE, type = String.class, description = "Тип партикла (например, FLAME). По умолчанию: FLAME")
    @ActionParam(key = CoreActionParams.COUNT, type = Integer.class, description = "Количество частиц. По умолчанию: 10")
    @ActionParam(key = CoreActionParams.dx, type = Double.class, description = "Смещение по оси X от игрока. По умолчанию: 0.0")
    @ActionParam(key = CoreActionParams.dy, type = Double.class, description = "Смещение по оси Y от игрока. По умолчанию: 1.0")
    @ActionParam(key = CoreActionParams.dz, type = Double.class, description = "Смещение по оси Z от игрока. По умолчанию: 0.0")
    @ActionParam(key = CoreActionParams.SPREAD_X, type = Double.class, description = "Радиус разлета частиц по оси X. По умолчанию: 0.5")
    @ActionParam(key = CoreActionParams.SPREAD_Y, type = Double.class, description = "Радиус разлета частиц по оси Y. По умолчанию: 0.5")
    @ActionParam(key = CoreActionParams.SPREAD_Z, type = Double.class, description = "Радиус разлета частиц по оси Z. По умолчанию: 0.5")
    @ActionParam(key = CoreActionParams.SPEED, type = Double.class, description = "Скорость движения частиц. По умолчанию: 0.0")
    public static Action parseParticle(Map<String, Object> params) {
        String defaultParticle = String.valueOf(params.getOrDefault(CoreActionParams.PARTICLE, "FLAME")).toUpperCase();

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            ActionParameters actionParams = new ActionParameters(params, context);
            String particleName = actionParams.getString(CoreActionParams.PARTICLE, defaultParticle).toUpperCase();

            try {
                Particle particle = Particle.valueOf(particleName);

                int count = actionParams.getInt(CoreActionParams.COUNT, 10);
                double dx = actionParams.getDouble(CoreActionParams.dx, 0.0);
                double dy = actionParams.getDouble(CoreActionParams.dy, 1.0);
                double dz = actionParams.getDouble(CoreActionParams.dz, 0.0);
                double spreadX = actionParams.getDouble(CoreActionParams.SPREAD_X, 0.5);
                double spreadY = actionParams.getDouble(CoreActionParams.SPREAD_Y, 0.5);
                double spreadZ = actionParams.getDouble(CoreActionParams.SPREAD_Z, 0.5);
                double speed = actionParams.getDouble(CoreActionParams.SPEED, 0.0);

                Location targetLoc = player.getLocation().add(dx, dy, dz);
                player.getWorld().spawnParticle(particle, targetLoc, count, spreadX, spreadY, spreadZ, speed);
                return true;
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        };
    }

    @ConfigAction("core:firework")
    @ActionParam(key = CoreActionParams.POWER, type = Integer.class, description = "Высота полёта/сила фейерверка. По умолчанию: 1")
    public static Action parseFirework(Map<String, Object> params) {
        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            ActionParameters actionParams = new ActionParameters(params, context);
            int power = actionParams.getInt(CoreActionParams.POWER, 1);

            Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK_ROCKET);
            FireworkMeta meta = firework.getFireworkMeta();

            meta.addEffect(FireworkEffect.builder()
                    .withColor(Color.RED, Color.YELLOW)
                    .withFade(Color.ORANGE)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .trail(true)
                    .build());
            meta.setPower(power);
            firework.setFireworkMeta(meta);
            return true;
        };
    }

    @ConfigAction("core:teleport")
    @ActionParam(key = CoreActionParams.X, type = Double.class, description = "Координата X. По умолчанию: текущий X игрока")
    @ActionParam(key = CoreActionParams.Y, type = Double.class, description = "Координата Y. По умолчанию: текущий Y игрока")
    @ActionParam(key = CoreActionParams.Z, type = Double.class, description = "Координата Z. По умолчанию: текущий Z игрока")
    @ActionParam(key = CoreActionParams.YAW, type = Float.class, description = "Поворот головы по горизонтали. По умолчанию: текущий yaw игрока")
    @ActionParam(key = CoreActionParams.PITCH, type = Float.class, description = "Поворот головы по вертикали. По умолчанию: текущий pitch игрока")
    @ActionParam(key = CoreActionParams.WORLD, type = String.class, description = "Название целевого мира. По умолчанию: текущий мир игрока")
    public static Action parseTeleport(Map<String, Object> params) {
        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            ActionParameters actionParams = new ActionParameters(params, context);
            try {
                // Если координаты не указаны, берем текущие координаты игрока
                double x = actionParams.getDouble(CoreActionParams.X, player.getLocation().getX());
                double y = actionParams.getDouble(CoreActionParams.Y, player.getLocation().getY());
                double z = actionParams.getDouble(CoreActionParams.Z, player.getLocation().getZ());

                float yaw = actionParams.getFloat(CoreActionParams.YAW, player.getLocation().getYaw());
                float pitch = actionParams.getFloat(CoreActionParams.PITCH, player.getLocation().getPitch());

                // Парсим мир (если не указан, берем текущий мир игрока)
                String worldName = actionParams.getString(CoreActionParams.WORLD, player.getWorld().getName());
                org.bukkit.World targetWorld = Bukkit.getWorld(worldName);

                if (targetWorld == null) {
                    targetWorld = player.getWorld(); // Фолбэк, если мир не найден
                }

                Location targetLoc = new Location(targetWorld, x, y, z, yaw, pitch);
                player.teleportAsync(targetLoc);
                return true;
            } catch (Exception e) {
                return false;
            }
        };
    }
    @ConfigAction("core:cancel_event")
    public static Action parseCancelEvent(Map<String, Object> params) {
        return context -> {
            context.cancel();
            return true;
        };
    }
    @ConfigAction("core:push")
    @ActionParam(key = CoreActionParams.X, type = Double.class, description = "Сила импульса по оси X. По умолчанию: 0.0")
    @ActionParam(key = CoreActionParams.Y, type = Double.class, description = "Сила импульса по оси Y. По умолчанию: 0.5")
    @ActionParam(key = CoreActionParams.Z, type = Double.class, description = "Сила импульса по оси Z. По умолчанию: 0.0")
    @ActionParam(key = CoreActionParams.ADD, type = Boolean.class, description = "Если true, прибавляет скорость к текущей. Иначе перезаписывает. По умолчанию: false")
    public static Action parsePush(Map<String, Object> params) {
        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            ActionParameters actionParams = new ActionParameters(params, context);
            double x = actionParams.getDouble(CoreActionParams.X, 0.0);
            double y = actionParams.getDouble(CoreActionParams.Y, 0.5);
            double z = actionParams.getDouble(CoreActionParams.Z, 0.0);
            boolean add = actionParams.getBoolean(CoreActionParams.ADD, false);

            org.bukkit.util.Vector velocity = new org.bukkit.util.Vector(x, y, z);
            if (add) {
                player.setVelocity(player.getVelocity().add(velocity));
            } else {
                player.setVelocity(velocity);
            }
            return true;
        };
    }
    @ConfigAction("core:spawn_entity")
    @ActionParam(key = CoreActionParams.ENTITY, type = String.class, description = "Тип призываемого существа (EntityType). По умолчанию: ZOMBIE")
    @ActionParam(key = CoreActionParams.X, type = Double.class, description = "Координата X для спавна. По умолчанию: текущий X игрока")
    @ActionParam(key = CoreActionParams.Y, type = Double.class, description = "Координата Y для спавна. По умолчанию: текущий Y игрока")
    @ActionParam(key = CoreActionParams.Z, type = Double.class, description = "Координата Z для спавна. По умолчанию: текущий Z игрока")
    public static Action parseSpawnEntity(Map<String, Object> params) {
        String defaultEntity = String.valueOf(params.getOrDefault(CoreActionParams.ENTITY, "ZOMBIE")).toUpperCase();

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            ActionParameters actionParams = new ActionParameters(params, context);
            String entityName = actionParams.getString(CoreActionParams.ENTITY, defaultEntity).toUpperCase();

            double x = actionParams.getDouble(CoreActionParams.X, player.getLocation().getX());
            double y = actionParams.getDouble(CoreActionParams.Y, player.getLocation().getY());
            double z = actionParams.getDouble(CoreActionParams.Z, player.getLocation().getZ());

            try {
                EntityType type = EntityType.valueOf(entityName);
                Location spawnLoc = new Location(player.getWorld(), x, y, z);

                if (type == EntityType.LIGHTNING_BOLT) {
                    player.getWorld().strikeLightning(spawnLoc);
                } else {
                    player.getWorld().spawnEntity(spawnLoc, type);
                }
                return true;
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        };
    }
    @ConfigAction("core:grant_advancement")
    @ActionParam(key = CoreActionParams.ADVANCEMENT, type = String.class, required = true, description = "Ключ достижения (namespace:id). Обязательный параметр")
    public static Action parseGrantAdvancement(Map<String, Object> params) {
        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            ActionParameters actionParams = new ActionParameters(params, context);
            String advancementKeyStr = actionParams.getString(CoreActionParams.ADVANCEMENT, "");
            if (advancementKeyStr.isEmpty()) return false;

            NamespacedKey advKey = NamespacedKey.fromString(advancementKeyStr);
            if (advKey == null) return false;

            org.bukkit.advancement.Advancement adv = Bukkit.getAdvancement(advKey);
            if (adv != null) {
                org.bukkit.advancement.AdvancementProgress progress = player.getAdvancementProgress(adv);
                for (String criteria : progress.getRemainingCriteria()) {
                    progress.awardCriteria(criteria);
                }
                return true;
            }
            return false;
        };
    }

    @ConfigAction("core:open_gui")
    @ActionParam(key = "gui", type = String.class, required = true, description = "Идентификатор GUI для открытия игроку")
    public static Action parseOpenGui(Map<String, Object> params) {
        String guiId = String.valueOf(params.getOrDefault("gui", params.getOrDefault("id", "")));
        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            if ("astral_atlas".equalsIgnoreCase(guiId) && kostin.ak.actionstriggers.ActionsTriggers.getCombatTracker().isInCombat(player)) {
                int remaining = kostin.ak.actionstriggers.ActionsTriggers.getCombatTracker().getRemainingSeconds(player);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 0.9f);
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 1.5f);
                player.sendActionBar(kostin.ak.actionstriggers.core.i18n.I18n.component("combat.atlas_blocked", Map.of("seconds", remaining)));
                return false;
            }

            org.bukkit.block.Block block = context.get(CoreKeys.BLOCK);
            return kostin.ak.actionstriggers.ActionsTriggers.getGuiRegistry().openGui(player, guiId, block);
        };
    }

    @ConfigAction("core:close_gui")
    public static Action parseCloseGui(Map<String, Object> params) {
        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;
            Bukkit.getScheduler().runTask(kostin.ak.actionstriggers.ActionsTriggers.getInstance(), (Runnable) player::closeInventory);
            return true;
        };
    }

    @ConfigAction("core:tag_combat")
    public static Action parseTagCombat(Map<String, Object> params) {
        int seconds = 15;
        if (params.containsKey("seconds")) {
            try {
                seconds = Integer.parseInt(params.get("seconds").toString());
            } catch (NumberFormatException ignored) {}
        }
        final int finalSeconds = seconds;
        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;
            kostin.ak.actionstriggers.ActionsTriggers.getCombatTracker().tag(player, finalSeconds);
            return true;
        };
    }

    @ConfigAction("core:untag_combat")
    public static Action parseUntagCombat(Map<String, Object> params) {
        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;
            kostin.ak.actionstriggers.ActionsTriggers.getCombatTracker().untag(player);
            return true;
        };
    }

    @ConfigAction("core:cryo_freeze")
    public static Action parseCryoFreeze(Map<String, Object> params) {
        int waterSlot = params.containsKey("water_slot") ? Integer.parseInt(params.get("water_slot").toString()) : 10;
        int crystalSlot = params.containsKey("crystal_slot") ? Integer.parseInt(params.get("crystal_slot").toString()) : 12;
        int outputSlot = params.containsKey("output_slot") ? Integer.parseInt(params.get("output_slot").toString()) : 16;
        int upgradeSlot = params.containsKey("upgrade_slot") ? Integer.parseInt(params.get("upgrade_slot").toString()) : 24;

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            org.bukkit.inventory.InventoryView openView = player.getOpenInventory();
            org.bukkit.inventory.Inventory topInv = openView.getTopInventory();
            if (!(topInv.getHolder() instanceof kostin.ak.actionstriggers.api.gui.AATGuiHolder holder)) {
                return false;
            }

            ItemStack waterItem = topInv.getItem(waterSlot);
            if (waterItem == null || waterItem.getType() != Material.WATER_BUCKET) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendActionBar(kostin.ak.actionstriggers.core.i18n.I18n.component("machine.ice_fabricator.insufficient_ingredients"));
                return false;
            }

            ItemStack crystalItem = topInv.getItem(crystalSlot);
            String crystalId = crystalItem != null ? ActionTriggerAPI.getItems().getFullId(crystalItem) : "";
            boolean isCrystal = crystalItem != null && (crystalId.contains("frost_crystal") || crystalItem.getType() == Material.AMETHYST_SHARD || crystalItem.getType() == Material.PRISMARINE_CRYSTALS);
            if (!isCrystal) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendActionBar(kostin.ak.actionstriggers.core.i18n.I18n.component("machine.ice_fabricator.insufficient_ingredients"));
                return false;
            }

            if (Boolean.TRUE.equals(holder.getSessionState().get("progress_running"))) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendActionBar(kostin.ak.actionstriggers.core.i18n.I18n.component("machine.ice_fabricator.insufficient_ingredients"));
                return false;
            }

            // Проверяем выбранный режим и длительность
            String mode = holder.getSessionState().getOrDefault("fabricator_mode", "blue_ice").toString();
            Material iceMat = Material.BLUE_ICE;
            int iceAmount = 4;
            int durationTicks = 160; // 8 секунд для Blue Ice

            if (mode.equalsIgnoreCase("packed_ice")) {
                iceMat = Material.PACKED_ICE;
                iceAmount = 16;
                durationTicks = 100; // 5 секунд для Packed Ice
            } else if (mode.equalsIgnoreCase("ice")) {
                iceMat = Material.ICE;
                iceAmount = 32;
                durationTicks = 60; // 3 секунды для обычного Ice
            }

            // Проверяем наличие ускорителя
            if (upgradeSlot >= 0 && upgradeSlot < topInv.getSize()) {
                ItemStack upItem = topInv.getItem(upgradeSlot);
                if (upItem != null && upItem.getType() != Material.AIR) {
                    kostin.ak.actionstriggers.api.gui.widget.Widget upWidget = holder.getSlotWidgets().get(upgradeSlot);
                    boolean isUpPl = (upWidget instanceof kostin.ak.actionstriggers.api.gui.widget.impl.InputSlotWidget isw && isw.isPlaceholder(upItem));
                    if (!isUpPl) {
                        String fullId = ActionTriggerAPI.getItems().getFullId(upItem).toLowerCase();
                        double speedMultiplier = 1.0;
                        if (fullId.contains("cryo_accelerator_t3") || fullId.contains("netherite") || fullId.contains("diamond")) {
                            speedMultiplier = 0.25; // Tier 3 (-75%)
                        } else if (fullId.contains("cryo_accelerator_t2") || fullId.contains("gold") || fullId.contains("emerald")) {
                            speedMultiplier = 0.50; // Tier 2 (-50%)
                        } else if (fullId.contains("cryo_accelerator_t1") || fullId.contains("iron") || fullId.contains("copper")) {
                            speedMultiplier = 0.75; // Tier 1 (-25%)
                        }
                        durationTicks = Math.max(20, (int) (durationTicks * speedMultiplier));
                    }
                }
            }

            ItemStack currentOut = topInv.getItem(outputSlot);
            if (currentOut != null && currentOut.getType() != Material.AIR) {
                kostin.ak.actionstriggers.api.gui.widget.Widget outWidget = holder.getSlotWidgets().get(outputSlot);
                boolean isPl = (outWidget instanceof kostin.ak.actionstriggers.api.gui.widget.impl.OutputSlotWidget osw && osw.isPlaceholder(currentOut));
                if (!isPl) {
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendActionBar(kostin.ak.actionstriggers.core.i18n.I18n.component("machine.ice_fabricator.output_occupied"));
                    return false;
                }
            }

            // 1. Потребляем ингредиенты СРАЗУ (защита от дюпа)
            if (waterItem.getAmount() > 1) {
                waterItem.setAmount(waterItem.getAmount() - 1);
                topInv.setItem(waterSlot, waterItem);
                player.getInventory().addItem(new ItemStack(Material.BUCKET));
            } else {
                topInv.setItem(waterSlot, new ItemStack(Material.BUCKET));
            }

            if (crystalItem.getAmount() > 1) {
                crystalItem.setAmount(crystalItem.getAmount() - 1);
                topInv.setItem(crystalSlot, crystalItem);
            } else {
                topInv.setItem(crystalSlot, null);
            }

            // Стартовые звуки активации
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.6f);
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 0.9f);
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<gradient:#74B9FF:#0984E3>⚙ Криогенный цикл запущен...</gradient>"));

            final Material finalIceMat = iceMat;
            final int finalIceAmount = iceAmount;

            Runnable onFinish = () -> {
                // Выдаем лед в слот выдачи
                if (player.isOnline() && player.getOpenInventory().getTopInventory().equals(topInv)) {
                    topInv.setItem(outputSlot, new ItemStack(finalIceMat, finalIceAmount));
                } else if (player.isOnline()) {
                    var leftovers = player.getInventory().addItem(new ItemStack(finalIceMat, finalIceAmount));
                    for (var drop : leftovers.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                } else if (holder.getBoundBlock() != null) {
                    holder.getBoundBlock().getWorld().dropItemNaturally(
                            holder.getBoundBlock().getLocation().add(0.5, 1.0, 0.5),
                            new ItemStack(finalIceMat, finalIceAmount)
                    );
                }

                // Эффекты завершения
                if (player.isOnline()) {
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.8f);
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<gradient:#74B9FF:#0984E3>❄ Крио-заморозка завершена! Заберите лед.</gradient>"));
                }
                if (holder.getBoundBlock() != null) {
                    holder.getBoundBlock().getWorld().spawnParticle(
                            org.bukkit.Particle.SNOWFLAKE,
                            holder.getBoundBlock().getLocation().add(0.5, 1.1, 0.5),
                            30, 0.5, 0.5, 0.5, 0.05
                    );
                }
            };

            // Ищем ProgressBarWidget
            kostin.ak.actionstriggers.api.gui.widget.impl.ProgressBarWidget pb = null;
            for (kostin.ak.actionstriggers.api.gui.widget.Widget w : holder.getSlotWidgets().values()) {
                if (w instanceof kostin.ak.actionstriggers.api.gui.widget.impl.ProgressBarWidget candidate) {
                    pb = candidate;
                    break;
                }
            }

            if (pb != null) {
                pb.startProcess(holder, durationTicks, onFinish);
            } else {
                onFinish.run();
            }

            return true;
        };
    }

    @ConfigAction("core:magma_smelt")
    public static Action parseMagmaSmelt(Map<String, Object> params) {
        int oraxSlot = params.containsKey("orax_slot") ? Integer.parseInt(params.get("orax_slot").toString()) : 10;
        int onyxSlot = params.containsKey("onyx_slot") ? Integer.parseInt(params.get("onyx_slot").toString()) : 12;
        int fuelSlot = params.containsKey("fuel_slot") ? Integer.parseInt(params.get("fuel_slot").toString()) : 14;
        int outputSlot = params.containsKey("output_slot") ? Integer.parseInt(params.get("output_slot").toString()) : 16;
        int durationTicks = params.containsKey("duration") ? Integer.parseInt(params.get("duration").toString()) : 100;

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            org.bukkit.inventory.InventoryView openView = player.getOpenInventory();
            org.bukkit.inventory.Inventory topInv = openView.getTopInventory();
            if (!(topInv.getHolder() instanceof kostin.ak.actionstriggers.api.gui.AATGuiHolder holder)) {
                return false;
            }

            ItemStack oraxItem = topInv.getItem(oraxSlot);
            String oraxId = oraxItem != null ? ActionTriggerAPI.getItems().getFullId(oraxItem).toLowerCase() : "";
            boolean hasOrax = oraxItem != null && (oraxId.contains("orax") || oraxItem.getType() == Material.IRON_INGOT);

            ItemStack onyxItem = topInv.getItem(onyxSlot);
            String onyxId = onyxItem != null ? ActionTriggerAPI.getItems().getFullId(onyxItem).toLowerCase() : "";
            boolean hasOnyx = onyxItem != null && (onyxId.contains("onyx") || onyxItem.getType() == Material.DIAMOND);

            ItemStack fuelItem = topInv.getItem(fuelSlot);
            boolean hasFuel = fuelItem != null && (fuelItem.getType() == Material.LAVA_BUCKET || fuelItem.getType() == Material.BLAZE_POWDER || fuelItem.getType() == Material.BLAZE_ROD);

            if (!hasOrax || !hasOnyx || !hasFuel) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>❌ Недостаточно ингредиентов для выплавки сплава!</red>"));
                return false;
            }

            if (Boolean.TRUE.equals(holder.getSessionState().get("progress_running"))) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return false;
            }

            ItemStack currentOut = topInv.getItem(outputSlot);
            if (currentOut != null && currentOut.getType() != Material.AIR) {
                kostin.ak.actionstriggers.api.gui.widget.Widget outWidget = holder.getSlotWidgets().get(outputSlot);
                boolean isPl = (outWidget instanceof kostin.ak.actionstriggers.api.gui.widget.impl.OutputSlotWidget osw && osw.isPlaceholder(currentOut));
                if (!isPl) {
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>❌ Слот выдачи уже занят!</red>"));
                    return false;
                }
            }

            // Потребляем ингредиенты
            if (oraxItem.getAmount() > 1) {
                oraxItem.setAmount(oraxItem.getAmount() - 1);
                topInv.setItem(oraxSlot, oraxItem);
            } else {
                topInv.setItem(oraxSlot, null);
            }

            if (onyxItem.getAmount() > 1) {
                onyxItem.setAmount(onyxItem.getAmount() - 1);
                topInv.setItem(onyxSlot, onyxItem);
            } else {
                topInv.setItem(onyxSlot, null);
            }

            if (fuelItem.getType() == Material.LAVA_BUCKET) {
                topInv.setItem(fuelSlot, new ItemStack(Material.BUCKET));
            } else if (fuelItem.getAmount() > 1) {
                fuelItem.setAmount(fuelItem.getAmount() - 1);
                topInv.setItem(fuelSlot, fuelItem);
            } else {
                topInv.setItem(fuelSlot, null);
            }

            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE, 1.0f, 1.0f);
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_LAVA_POP, 1.0f, 1.2f);
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<gradient:#FF4500:#FFA500>🔥 Термоплавка сплава запущена...</gradient>"));

            Runnable onFinish = () -> {
                ItemStack result = ActionTriggerAPI.getItems().resolveItem("oraxen:pyro_onyx_ingot");
                if (result == null) {
                    result = new ItemStack(Material.NETHERITE_INGOT);
                    var meta = result.getItemMeta();
                    meta.displayName(MiniMessage.miniMessage().deserialize("<gradient:#FF4500:#2C3E50><bold>Пиро-Ониксовый Слиток</bold></gradient>"));
                    result.setItemMeta(meta);
                }

                if (player.isOnline() && player.getOpenInventory().getTopInventory().equals(topInv)) {
                    topInv.setItem(outputSlot, result);
                } else if (player.isOnline()) {
                    var leftovers = player.getInventory().addItem(result);
                    for (var drop : leftovers.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                } else if (holder.getBoundBlock() != null) {
                    holder.getBoundBlock().getWorld().dropItemNaturally(
                            holder.getBoundBlock().getLocation().add(0.5, 1.0, 0.5), result);
                }

                if (player.isOnline()) {
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 0.9f, 1.2f);
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.6f);
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<gradient:#FF4500:#2C3E50>🔥 Пиро-Ониксовый сплав готов! Заберите результат.</gradient>"));
                }
                if (holder.getBoundBlock() != null) {
                    holder.getBoundBlock().getWorld().spawnParticle(
                            org.bukkit.Particle.FLAME,
                            holder.getBoundBlock().getLocation().add(0.5, 1.1, 0.5),
                            25, 0.3, 0.3, 0.3, 0.05
                    );
                }
            };

            kostin.ak.actionstriggers.api.gui.widget.impl.ProgressBarWidget pb = null;
            for (kostin.ak.actionstriggers.api.gui.widget.Widget w : holder.getSlotWidgets().values()) {
                if (w instanceof kostin.ak.actionstriggers.api.gui.widget.impl.ProgressBarWidget candidate) {
                    pb = candidate;
                    break;
                }
            }

            if (pb != null) {
                pb.startProcess(holder, durationTicks, onFinish);
            } else {
                onFinish.run();
            }

            return true;
        };
    }

    @ConfigAction("core:alchemical_distill")
    public static Action parseAlchemicalDistill(Map<String, Object> params) {
        int ingredientSlot = params.containsKey("ingredient_slot") ? Integer.parseInt(params.get("ingredient_slot").toString()) : 10;
        int waterSlot = params.containsKey("water_slot") ? Integer.parseInt(params.get("water_slot").toString()) : 12;
        int outputSlot = params.containsKey("output_slot") ? Integer.parseInt(params.get("output_slot").toString()) : 16;
        int durationTicks = params.containsKey("duration") ? Integer.parseInt(params.get("duration").toString()) : 80;

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            org.bukkit.inventory.InventoryView openView = player.getOpenInventory();
            org.bukkit.inventory.Inventory topInv = openView.getTopInventory();
            if (!(topInv.getHolder() instanceof kostin.ak.actionstriggers.api.gui.AATGuiHolder holder)) {
                return false;
            }

            ItemStack ingItem = topInv.getItem(ingredientSlot);
            String ingId = ingItem != null ? ActionTriggerAPI.getItems().getFullId(ingItem).toLowerCase() : "";
            boolean hasIng = ingItem != null && (ingId.contains("berry") || ingId.contains("grape") || ingItem.getType() == Material.SWEET_BERRIES || ingItem.getType() == Material.GLOW_BERRIES || ingItem.getType() == Material.HONEYCOMB);

            ItemStack waterItem = topInv.getItem(waterSlot);
            boolean hasWater = waterItem != null && (waterItem.getType() == Material.POTION || waterItem.getType() == Material.HONEY_BOTTLE || waterItem.getType() == Material.WATER_BUCKET);

            if (!hasIng || !hasWater) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>❌ Недостаточно компонентов для дистилляции!</red>"));
                return false;
            }

            if (Boolean.TRUE.equals(holder.getSessionState().get("progress_running"))) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return false;
            }

            ItemStack currentOut = topInv.getItem(outputSlot);
            if (currentOut != null && currentOut.getType() != Material.AIR) {
                kostin.ak.actionstriggers.api.gui.widget.Widget outWidget = holder.getSlotWidgets().get(outputSlot);
                boolean isPl = (outWidget instanceof kostin.ak.actionstriggers.api.gui.widget.impl.OutputSlotWidget osw && osw.isPlaceholder(currentOut));
                if (!isPl) {
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>❌ Слот выдачи уже занят!</red>"));
                    return false;
                }
            }

            // Потребляем ингредиенты
            if (ingItem.getAmount() > 1) {
                ingItem.setAmount(ingItem.getAmount() - 1);
                topInv.setItem(ingredientSlot, ingItem);
            } else {
                topInv.setItem(ingredientSlot, null);
            }

            if (waterItem.getType() == Material.WATER_BUCKET) {
                topInv.setItem(waterSlot, new ItemStack(Material.BUCKET));
            } else if (waterItem.getAmount() > 1) {
                waterItem.setAmount(waterItem.getAmount() - 1);
                topInv.setItem(waterSlot, waterItem);
            } else {
                topInv.setItem(waterSlot, null);
            }

            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.2f);
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<gradient:#A29BFE:#6C5CE7>⚗ Дистилляция согревающего напитка запущена...</gradient>"));

            String outputItemId = ingId.contains("honey") || ingItem.getType() == Material.HONEYCOMB ? "oraxen:hot_sbiten" : "oraxen:berry_tea";

            Runnable onFinish = () -> {
                ItemStack result = ActionTriggerAPI.getItems().resolveItem(outputItemId);
                if (result == null) {
                    result = new ItemStack(Material.POTION);
                    var meta = result.getItemMeta();
                    meta.displayName(MiniMessage.miniMessage().deserialize("<gradient:#FF6B81:#FF4757><bold>Чай из морозных ягод</bold></gradient>"));
                    result.setItemMeta(meta);
                }

                if (player.isOnline() && player.getOpenInventory().getTopInventory().equals(topInv)) {
                    topInv.setItem(outputSlot, result);
                } else if (player.isOnline()) {
                    var leftovers = player.getInventory().addItem(result);
                    for (var drop : leftovers.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                } else if (holder.getBoundBlock() != null) {
                    holder.getBoundBlock().getWorld().dropItemNaturally(
                            holder.getBoundBlock().getLocation().add(0.5, 1.0, 0.5), result);
                }

                if (player.isOnline()) {
                    player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_BOTTLE_FILL, 1.0f, 1.2f);
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.8f);
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<gradient:#A29BFE:#6C5CE7>🍵 Напиток готов! Заберите результат.</gradient>"));
                }
            };

            kostin.ak.actionstriggers.api.gui.widget.impl.ProgressBarWidget pb = null;
            for (kostin.ak.actionstriggers.api.gui.widget.Widget w : holder.getSlotWidgets().values()) {
                if (w instanceof kostin.ak.actionstriggers.api.gui.widget.impl.ProgressBarWidget candidate) {
                    pb = candidate;
                    break;
                }
            }

            if (pb != null) {
                pb.startProcess(holder, durationTicks, onFinish);
            } else {
                onFinish.run();
            }

            return true;
        };
    }

    @ConfigAction("core:delay")
    public static Action parseDelay(Map<String, Object> params) {
        long ticks = 20L;
        if (params.containsKey("ticks")) {
            ticks = Long.parseLong(params.get("ticks").toString());
        } else if (params.containsKey("seconds")) {
            ticks = (long) (Double.parseDouble(params.get("seconds").toString()) * 20L);
        } else if (params.containsKey("delay")) {
            ticks = Long.parseLong(params.get("delay").toString());
        }

        kostin.ak.actionstriggers.api.parser.AATParser parser = new kostin.ak.actionstriggers.api.parser.AATParser();
        List<Action> actions = parser.parseActions(params.get("actions"));

        final long finalTicks = ticks;
        return context -> {
            ActionTriggerAPI.getScheduler().runLater(context, finalTicks, ctx -> {
                for (Action a : actions) {
                    a.execute(ctx);
                }
            });
            return true;
        };
    }

    @ConfigAction("core:repeat")
    public static Action parseRepeat(Map<String, Object> params) {
        int times = params.containsKey("times") ? Integer.parseInt(params.get("times").toString()) : 1;
        long interval = 20L;
        if (params.containsKey("interval")) {
            interval = Long.parseLong(params.get("interval").toString());
        } else if (params.containsKey("interval_seconds")) {
            interval = (long) (Double.parseDouble(params.get("interval_seconds").toString()) * 20L);
        }
        long delay = params.containsKey("delay") ? Long.parseLong(params.get("delay").toString()) : 0L;

        kostin.ak.actionstriggers.api.parser.AATParser parser = new kostin.ak.actionstriggers.api.parser.AATParser();
        List<Action> actions = parser.parseActions(params.get("actions"));

        final int finalTimes = times;
        final long finalInterval = interval;
        final long finalDelay = delay;

        return context -> {
            ActionTriggerAPI.getScheduler().runRepeating(context, finalDelay, finalInterval, finalTimes, (ctx, iter) -> {
                for (Action a : actions) {
                    a.execute(ctx);
                }
            });
            return true;
        };
    }

    @ConfigAction("core:schedule")
    public static Action parseSchedule(Map<String, Object> params) {
        String id = String.valueOf(params.getOrDefault("id", "task_" + System.currentTimeMillis()));
        long delay = 20L;
        if (params.containsKey("delay")) {
            delay = Long.parseLong(params.get("delay").toString());
        } else if (params.containsKey("seconds")) {
            delay = (long) (Double.parseDouble(params.get("seconds").toString()) * 20L);
        }

        kostin.ak.actionstriggers.api.parser.AATParser parser = new kostin.ak.actionstriggers.api.parser.AATParser();
        List<Action> actions = parser.parseActions(params.get("actions"));

        final long finalDelay = delay;
        final String finalId = id;

        return context -> {
            ActionTriggerAPI.getScheduler().scheduleNamed(finalId, context, finalDelay, ctx -> {
                for (Action a : actions) {
                    a.execute(ctx);
                }
            });
            return true;
        };
    }

    @ConfigAction("core:cancel_schedule")
    public static Action parseCancelSchedule(Map<String, Object> params) {
        String id = String.valueOf(params.getOrDefault("id", ""));
        return context -> ActionTriggerAPI.getScheduler().cancelNamed(id);
    }

    @ConfigAction("core:set_cooldown")
    public static Action parseSetCooldown(Map<String, Object> params) {
        String key = String.valueOf(params.getOrDefault("key", "global"));
        int duration = params.containsKey("duration") ? Integer.parseInt(params.get("duration").toString()) : 5;
        if (params.containsKey("seconds")) {
            duration = Integer.parseInt(params.get("seconds").toString());
        }

        final int finalDuration = duration;
        return context -> {
            Player p = context.get(CoreKeys.PLAYER);
            if (p == null) return false;
            ActionTriggerAPI.getScheduler().setCooldown(p.getUniqueId(), key, finalDuration);
            return true;
        };
    }

    @ConfigAction("core:add_permission")
    public static Action parseAddPermission(Map<String, Object> params) {
        String perm = String.valueOf(params.getOrDefault("permission", ""));
        return context -> {
            Player p = context.get(CoreKeys.PLAYER);
            if (p == null || perm.isEmpty()) return false;
            kostin.ak.actionstriggers.core.hook.LuckPermsHook.addPermission(p, perm);
            return true;
        };
    }

    @ConfigAction("core:remove_permission")
    public static Action parseRemovePermission(Map<String, Object> params) {
        String perm = String.valueOf(params.getOrDefault("permission", ""));
        return context -> {
            Player p = context.get(CoreKeys.PLAYER);
            if (p == null || perm.isEmpty()) return false;
            kostin.ak.actionstriggers.core.hook.LuckPermsHook.removePermission(p, perm);
            return true;
        };
    }

    @ConfigAction("core:set_group")
    public static Action parseSetGroup(Map<String, Object> params) {
        String group = String.valueOf(params.getOrDefault("group", "default"));
        return context -> {
            Player p = context.get(CoreKeys.PLAYER);
            if (p == null) return false;
            kostin.ak.actionstriggers.core.hook.LuckPermsHook.setGroup(p, group);
            return true;
        };
    }
}