package kostin.ak.actionstriggers.core.defaults.filters;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.filter.ConfigFilter;
import kostin.ak.actionstriggers.api.filter.Filter;
import kostin.ak.actionstriggers.api.filter.Filters;
import kostin.ak.actionstriggers.api.filter.IFilterParsers;
import kostin.ak.actionstriggers.core.CoreActionParams;
import kostin.ak.actionstriggers.core.CoreFilterKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kostin.ak.actionstriggers.api.parser.AATParser;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Класс, содержащий методы-парсеры для стандартных фильтров.
 * Этот класс будет автоматически отсканирован в FilterRegistry.
 */
public final class DefaultFilterParsers implements IFilterParsers {

    private DefaultFilterParsers() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ========================================================================
    // БАЗОВЫЕ ФИЛЬТРЫ
    // ========================================================================

    @ConfigFilter("core:always_true")
    public static Filter parseAlwaysTrue(Map<String, Object> params) {
        return Filters.alwaysTrue();
    }

    @ConfigFilter("core:in_combat")
    public static Filter parseInCombat(Map<String, Object> params) {
        return context -> {
            Player p = context.get(CoreKeys.PLAYER);
            if (p == null) return false;
            return kostin.ak.actionstriggers.ActionsTriggers.getCombatTracker().isInCombat(p);
        };
    }

    @ConfigFilter("core:not_in_combat")
    public static Filter parseNotInCombat(Map<String, Object> params) {
        return context -> {
            Player p = context.get(CoreKeys.PLAYER);
            if (p == null) return true;
            return !kostin.ak.actionstriggers.ActionsTriggers.getCombatTracker().isInCombat(p);
        };
    }

    @ConfigFilter("core:on_cooldown")
    public static Filter parseOnCooldown(Map<String, Object> params) {
        String key = String.valueOf(params.getOrDefault("key", "global"));
        return context -> {
            Player p = context.get(CoreKeys.PLAYER);
            if (p == null) return false;
            return ActionTriggerAPI.getScheduler().isOnCooldown(p.getUniqueId(), key);
        };
    }

    @ConfigFilter("core:not_on_cooldown")
    public static Filter parseNotOnCooldown(Map<String, Object> params) {
        String key = String.valueOf(params.getOrDefault("key", "global"));
        return context -> {
            Player p = context.get(CoreKeys.PLAYER);
            if (p == null) return true;
            return !ActionTriggerAPI.getScheduler().isOnCooldown(p.getUniqueId(), key);
        };
    }

    @ConfigFilter("core:permission")
    public static Filter parsePermission(Map<String, Object> params) {
        String perm = String.valueOf(params.getOrDefault("permission", ""));
        return Filters.permission(perm);
    }

    @ConfigFilter("core:chance")
    public static Filter parseChance(Map<String, Object> params) {
        double chance = 1.0;
        if (params.containsKey("chance")) {
            try {
                chance = Double.parseDouble(params.get("chance").toString());
            } catch (NumberFormatException ignored) {}
        }
        return Filters.chance(chance);
    }

    @ConfigFilter("core:looking_at")
    public static Filter parseLookingAt(Map<String, Object> params) {
        String matName = String.valueOf(params.getOrDefault("material", "STONE")).toUpperCase();
        Material material = Material.matchMaterial(matName);
        if (material == null) material = Material.AIR;

        int distance = 5;
        if (params.containsKey("distance")) {
            try {
                distance = Integer.parseInt(params.get("distance").toString());
            } catch (NumberFormatException ignored) {}
        }
        return Filters.lookingAt(material, distance);
    }

    @ConfigFilter("core:looking_at_water")
    public static Filter parseLookingAtWater(Map<String, Object> params) {
        return context -> {
            Player p = context.get(CoreKeys.PLAYER);
            if (p == null) return false;

            // FluidCollisionMode.ALWAYS позволяет "увидеть" воду
            Block b = p.getTargetBlockExact(5, FluidCollisionMode.ALWAYS);
            return b != null && b.getType() == Material.WATER;
        };
    }

    // ========================================================================
    // ФИЛЬТРЫ КОНТЕКСТА (Требуют работы с ContextKey)
    // ========================================================================

    @ConfigFilter("core:has")
    public static Filter parseHas(Map<String, Object> params) {
        String keyName = String.valueOf(params.get("key"));
        return Filters.has(ContextKey.of(keyName, Object.class));
    }

    @ConfigFilter("core:eq")
    public static Filter parseEq(Map<String, Object> params) {
        String keyName = String.valueOf(params.get("key"));
        Object expected = params.get("value");
        return Filters.eq(ContextKey.of(keyName, Object.class), expected);
    }

    @ConfigFilter("core:in")
    @SuppressWarnings("unchecked")
    public static Filter parseIn(Map<String, Object> params) {
        String keyName = String.valueOf(params.get("key"));
        Collection<Object> allowed = (Collection<Object>) params.get("values");
        return Filters.in(ContextKey.of(keyName, Object.class), allowed);
    }

    @ConfigFilter("core:gt")
    public static Filter parseGt(Map<String, Object> params) {
        String keyName = String.valueOf(params.get("key"));
        double min = Double.parseDouble(String.valueOf(params.get("value")));
        return Filters.gt(ContextKey.of(keyName, Double.class), min);
    }

    @ConfigFilter("core:lt")
    public static Filter parseLt(Map<String, Object> params) {
        String keyName = String.valueOf(params.get("key"));
        double max = Double.parseDouble(String.valueOf(params.get("value")));
        return Filters.lt(ContextKey.of(keyName, Double.class), max);
    }

    @ConfigFilter("core:regex")
    public static Filter parseRegex(Map<String, Object> params) {
        String keyName = String.valueOf(params.get("key"));
        String pattern = String.valueOf(params.get("pattern"));
        return Filters.regex(ContextKey.of(keyName, String.class), pattern);
    }

    @ConfigFilter("core:match")
    public static Filter parseMatch(Map<String, Object> params) {
        String template = String.valueOf(params.getOrDefault("template", ""));
        String expected = String.valueOf(params.getOrDefault("value", ""));
        boolean ignoreCase = Boolean.parseBoolean(String.valueOf(params.getOrDefault("ignore_case", "true")));
        return Filters.match(template, expected, ignoreCase);
    }

    @ConfigFilter("core:mismatch")
    public static Filter parseMismatch(Map<String, Object> params) {
        Filter match = parseMatch(params);
        return context -> !match.test(context);
    }

    // ========================================================================
    // ЛОГИЧЕСКИЕ ОПЕРАТОРЫ
    // ========================================================================
    // Примечание: Для их работы потребуется статический доступ к главному AATParser,
    // чтобы парсить вложенные списки условий.

   @ConfigFilter("core:and")
    @SuppressWarnings("unchecked")
    public static Filter parseAnd(Map<String, Object> params) {
        List<Map<String, Object>> subConditions = (List<Map<String, Object>>) params.get("conditions");
        // AATParser.parseConditions - это метод, который мы напишем на следующем этапе
        // Он превратит List<Map> в List<Filter>
        AATParser parser = new AATParser();
        Filter[] filters = subConditions.stream().map(parser::parseCondition).toArray(Filter[]::new);
        return Filters.and(filters);
    }

    @ConfigFilter("core:or")
    @SuppressWarnings("unchecked")
    public static Filter parseOr(Map<String, Object> params) {
        List<Map<String, Object>> subConditions = (List<Map<String, Object>>) params.get("conditions");
        AATParser parser = new AATParser();
        Filter[] filters = subConditions.stream().map(parser::parseCondition).toArray(Filter[]::new);
        return Filters.or(filters);
    }

    @ConfigFilter("core:not")
    @SuppressWarnings("unchecked")
    public static Filter parseNot(Map<String, Object> params) {
        Map<String, Object> subCondition = (Map<String, Object>) params.get("condition");
        AATParser parser = new AATParser();
        Filter filter = parser.parseCondition(subCondition);
        return Filters.not(filter);
    }

    @ConfigFilter(CoreFilterKeys.CHECK_ITEM)
    public static Filter checkItem(Map<String, Object> map) {
        String contextKeyStr = (String) map.getOrDefault(CoreActionParams.CONTEXT_KEY, "item_in_hand_id");
        ContextKey<String> contextKey = ContextKey.of(contextKeyStr, String.class);

        // Получаем список валидных ID
        List<String> targetMaterials = extractStringList(map.get(CoreActionParams.MATERIAL));

        return context -> {
            String currentId = context.get(contextKey);
            if (currentId == null) return false;

            // Проверяем, есть ли наш предмет в списке (игнорируя регистр)
            return targetMaterials.contains(currentId.toLowerCase());
        };
    }

    @ConfigFilter(CoreFilterKeys.CHECK_BLOCK)
    public static Filter checkBlock(Map<String, Object> map) {
        String contextKeyStr = (String) map.getOrDefault(CoreActionParams.CONTEXT_KEY, "block_id");
        ContextKey<String> contextKey = ContextKey.of(contextKeyStr, String.class);

        // Получаем список валидных ID
        List<String> targetMaterials = extractStringList(map.get(CoreActionParams.MATERIAL));

        return context -> {
            String currentId = context.get(contextKey);
            if (currentId == null) return false;

            return targetMaterials.contains(currentId.toLowerCase());
        };
    }

    @ConfigFilter("core:has_item")
    public static Filter parseHasItem(Map<String, Object> map) {
        Object rawMat = map.containsKey(CoreActionParams.MATERIAL) ? map.get(CoreActionParams.MATERIAL) : map.get("item");
        List<String> targetMaterials = extractStringList(rawMat);
        int amount = 1;
        if (map.containsKey("amount")) {
            try {
                amount = Integer.parseInt(map.get("amount").toString());
            } catch (NumberFormatException ignored) {}
        }
        final int reqAmount = amount;

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            int count = 0;
            for (ItemStack is : player.getInventory().getContents()) {
                if (is == null || is.getType() == Material.AIR) continue;
                String fullId = ActionTriggerAPI.getItems().getFullId(is);
                if (fullId != null && targetMaterials.contains(fullId.toLowerCase())) {
                    count += is.getAmount();
                }
            }
            return count >= reqAmount;
        };
    }

    @ConfigFilter("core:has_not_item")
    public static Filter parseHasNotItem(Map<String, Object> map) {
        Filter hasItem = parseHasItem(map);
        return context -> !hasItem.test(context);
    }

    /**
     * Вспомогательный метод. Превращает и строку ("oraxen:ruby"),
     * и список (["oraxen:ruby", "oraxen:sapphire"]) в List<String> в нижнем регистре.
     */
    private static List<String> extractStringList(Object obj) {
        if (obj instanceof String) {
            return Collections.singletonList(((String) obj).toLowerCase());
        } else if (obj instanceof List) {
            return ((List<?>) obj).stream()
                    .map(Object::toString)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}