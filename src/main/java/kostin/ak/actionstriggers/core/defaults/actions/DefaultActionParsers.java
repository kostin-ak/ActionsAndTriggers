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
import org.bukkit.NamespacedKey;

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
        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            ActionParameters actionParams = new ActionParameters(params, context);
            // Заметь: теперь дефолтное значение с неймспейсом
            String materialStr = actionParams.getString(CoreActionParams.MATERIAL, "minecraft:stone");
            int amount = actionParams.getInt(CoreActionParams.AMOUNT, 1);

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
}