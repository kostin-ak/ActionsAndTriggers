package kostin.ak.actionstriggers.core;

import kostin.ak.actionstriggers.api.context.ContextKey;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Стандартные ключи ядра для использования в ExecutionContext.
 */
public final class CoreKeys {

    private CoreKeys() { throw new UnsupportedOperationException(); }

    public static final ContextKey<Player> PLAYER = ContextKey.of("player", Player.class);
    public static final ContextKey<Location> LOCATION = ContextKey.of("location", Location.class);
    public static final ContextKey<Block> BLOCK = ContextKey.of("block", Block.class);
    public static final ContextKey<Material> BLOCK_MATERIAL = ContextKey.of("block_material", Material.class);
    public static final ContextKey<Entity> ENTITY = ContextKey.of("entity", Entity.class);
    public static final ContextKey<Double> DAMAGE = ContextKey.of("damage", Double.class);
    public static final ContextKey<Action> ACTION = ContextKey.of("action", Action.class);
    public static final ContextKey<ButtonType> BUTTON_TYPE = ContextKey.of("button", ButtonType.class);
    public static final ContextKey<ItemStack> ITEM = ContextKey.of("item", ItemStack.class);
    public static final ContextKey<String> MESSAGE = ContextKey.of("message", String.class);
    public static final ContextKey<DamageSource> DAMAGE_SOURCE = ContextKey.of("damage_source", DamageSource.class);
    @SuppressWarnings("unchecked")
    public static final ContextKey<List<ItemStack>> ITEM_DROPS = (ContextKey<List<ItemStack>>) (ContextKey<?>) ContextKey.of("item_drops", List.class);
    public static final ContextKey<ItemStack> ITEM_IN_HAND = ContextKey.of("item_in_hand", ItemStack.class);
    public static final ContextKey<EntityDamageEvent.DamageCause> DAMAGE_CAUSE = ContextKey.of("damage_cause", EntityDamageEvent.DamageCause.class);
    public static final ContextKey<Entity> DAMAGER = ContextKey.of("damager", Entity.class);
    public static final ContextKey<Player> KILLER = ContextKey.of("killer", Player.class);
    public static final ContextKey<Integer> LEVEL = ContextKey.of("level", Integer.class);
    public static final ContextKey<String> WORLD_NAME = ContextKey.of("world_name", String.class);
    public static final ContextKey<Boolean> HAS_BLOCK = ContextKey.of("has_block", Boolean.class);
    public static final ContextKey<String> ADVANCEMENT_KEY = ContextKey.of("advancement_key", String.class);
    public static final ContextKey<Boolean> IS_FLYING = ContextKey.of("is_flying", Boolean.class);


    public static enum ButtonType{
        LEFT,
        RIGHT
    }
}

