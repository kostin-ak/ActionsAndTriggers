package kostin.ak.actionstriggers.core;

/**
 * Стандартные строковые ключи (ID) для Фильтров.
 */
public final class CoreFilterKeys {
    private CoreFilterKeys() { throw new UnsupportedOperationException(); }

    public static final String ALWAYS_TRUE = "core:always_true";
    public static final String AND = "core:and";
    public static final String OR = "core:or";
    public static final String NOT = "core:not";

    public static final String CHECK_ITEM = "core:check_item";
    public static final String CHECK_BLOCK = "core:check_block";
}