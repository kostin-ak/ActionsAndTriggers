package kostin.ak.actionstriggers.core.defaults.filters;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.filter.ConfigFilter;
import kostin.ak.actionstriggers.api.filter.Filter;
import kostin.ak.actionstriggers.api.filter.Filters;
import kostin.ak.actionstriggers.api.filter.IFilterParsers;
import org.bukkit.Material;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import kostin.ak.actionstriggers.api.parser.AATParser;
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

    // Допустим, это метод в вашем IFilterParsers
    @ConfigFilter("core:check_item")
    public static Filter checkItem(Map<String, Object> map) {
        // Обрати внимание: теперь мы по дефолту ищем строку item_in_hand_id, а не сам ItemStack
        String contextKeyStr = (String) map.getOrDefault("context_key", "item_in_hand_id");
        String targetMaterialStr = (String) map.get("material");

        ContextKey<String> contextKey = ContextKey.of(contextKeyStr, String.class);

        return context -> {
            // Берем из контекста уже готовую строку (например, "oraxen:magic_wand")
            String currentId = context.get(contextKey);
            if (currentId == null) return false;

            return currentId.equalsIgnoreCase(targetMaterialStr);
        };
    }

    @ConfigFilter("core:check_block")
    public static Filter checkBlock(Map<String, Object> map) {
        String contextKeyStr = (String) map.getOrDefault("context_key", "block_id");
        String targetMaterialStr = (String) map.get("material");

        ContextKey<String> contextKey = ContextKey.of(contextKeyStr, String.class);

        return context -> {
            String currentId = context.get(contextKey);
            if (currentId == null) return false;
            return currentId.equalsIgnoreCase(targetMaterialStr);
        };
    }
}