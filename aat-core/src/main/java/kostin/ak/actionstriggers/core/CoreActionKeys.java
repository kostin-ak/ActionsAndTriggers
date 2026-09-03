package kostin.ak.actionstriggers.core;

import org.bukkit.NamespacedKey;

/**
 * Стандартные ключи Экшенов.
 */
public final class CoreActionKeys {
    private CoreActionKeys() { throw new UnsupportedOperationException(); }

    public static final NamespacedKey MESSAGE = NamespacedKey.fromString("core:message");
    public static final NamespacedKey SOUND = NamespacedKey.fromString("core:sound");
    public static final NamespacedKey TELEPORT = NamespacedKey.fromString("core:teleport");
    public static final NamespacedKey GIVE_ITEM = NamespacedKey.fromString("core:give_item");
    public static final NamespacedKey POTION_EFFECT = NamespacedKey.fromString("core:potion_effect");
    public static final NamespacedKey DAMAGE = NamespacedKey.fromString("core:damage");
    public static final NamespacedKey KILL = NamespacedKey.fromString("core:kill");
    public static final NamespacedKey COMMAND = NamespacedKey.fromString("core:command");
    public static final NamespacedKey PARTICLE = NamespacedKey.fromString("core:particle");
    public static final NamespacedKey FIREWORK = NamespacedKey.fromString("core:firework");
}