package kostin.ak.actionstriggers.api.provider;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    private final Map<String, ItemProvider> providers = new HashMap<>();

    public void register(@NotNull ItemProvider provider) {
        providers.put(provider.getNamespace().toLowerCase(), provider);
    }

    /**
     * Превращает строку "namespace:id" (или просто "id") в ItemStack.
     */
    @Nullable
    public ItemStack resolveItem(@NotNull String fullId) {
        String namespace = "minecraft"; // По умолчанию ванилла
        String localId = fullId;

        if (fullId.contains(":")) {
            String[] parts = fullId.split(":", 2);
            namespace = parts[0].toLowerCase();
            localId = parts[1];
        }

        ItemProvider provider = providers.get(namespace);
        if (provider != null) {
            return provider.getItem(localId);
        }
        return null;
    }

    /**
     * Получает полный ID "namespace:id" из ItemStack.
     */
    @NotNull
    public String getFullId(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return "minecraft:air";

        // Сначала опрашиваем кастомные провайдеры
        for (ItemProvider provider : providers.values()) {
            if (provider.getNamespace().equals("minecraft")) continue;

            String id = provider.getId(item);
            if (id != null) {
                return provider.getNamespace() + ":" + id;
            }
        }

        // Фолбэк на ваниллу
        ItemProvider vanilla = providers.get("minecraft");
        if (vanilla != null) {
            String vanillaId = vanilla.getId(item);
            if (vanillaId != null) return "minecraft:" + vanillaId;
        }

        return "minecraft:" + item.getType().name().toLowerCase();
    }

    public java.util.Collection<ItemProvider> getProviders() {
        return providers.values();
    }

    public ItemProvider getProvider(String namespace) {
        return providers.get(namespace.toLowerCase());
    }
}
