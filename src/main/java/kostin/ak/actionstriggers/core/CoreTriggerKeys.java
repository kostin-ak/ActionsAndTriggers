package kostin.ak.actionstriggers.core;

import org.bukkit.NamespacedKey;

/**
 * Стандартные ключи триггеров.
 */
public final class CoreTriggerKeys {
    private CoreTriggerKeys() { throw new UnsupportedOperationException(); }

    public static final NamespacedKey BLOCK_BREAK = NamespacedKey.fromString("core:block_break");
    public static final NamespacedKey ENTITY_DEATH = NamespacedKey.fromString("core:entity_death");
    public static final NamespacedKey PLAYER_DAMAGE = NamespacedKey.fromString("core:player_damage");
    public static final NamespacedKey PLAYER_INTERACT = NamespacedKey.fromString("core:player_interact");
    public static final NamespacedKey PLAYER_JOIN = NamespacedKey.fromString("core:player_join");
    public static final NamespacedKey PLAYER_SNEAK = NamespacedKey.fromString("core:player_sneak");
    public static final NamespacedKey BLOCK_PLACE = NamespacedKey.fromString("core:block_place");
    public static final NamespacedKey PLAYER_CONSUME = NamespacedKey.fromString("core:player_consume");
    public static final NamespacedKey PLAYER_CHAT = NamespacedKey.fromString("core:player_chat");
    public static final NamespacedKey PLAYER_DEATH = NamespacedKey.fromString("core:player_death");
    public static final NamespacedKey PLAYER_QUIT = NamespacedKey.fromString("core:player_quit");
    public static final NamespacedKey PLAYER_LEVEL_CHANGE = NamespacedKey.fromString("core:player_level_change");
    public static final NamespacedKey PLAYER_WORLD_CHANGE = NamespacedKey.fromString("core:player_world_change");
}