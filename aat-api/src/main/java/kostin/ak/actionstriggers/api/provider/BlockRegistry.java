package kostin.ak.actionstriggers.api.provider;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {
    private final Map<String, BlockProvider> providers = new HashMap<>();

    public void register(@NotNull BlockProvider provider) {
        providers.put(provider.getNamespace().toLowerCase(), provider);
    }

    /**
     * Пытается установить блок по указанным координатам.
     */
    public boolean placeBlock(@NotNull Block block, @NotNull String fullId) {
        String namespace = "minecraft";
        String localId = fullId;

        if (fullId.contains(":")) {
            String[] parts = fullId.split(":", 2);
            namespace = parts[0].toLowerCase();
            localId = parts[1];
        }

        BlockProvider provider = providers.get(namespace);
        if (provider != null) {
            provider.setBlock(block, localId);
            return true;
        }
        return false;
    }

    /**
     * Получает полный ID "namespace:id" из существующего Блока.
     */
    @NotNull
    public String getFullId(@NotNull Block block) {
        // Сначала опрашиваем кастомные провайдеры
        for (BlockProvider provider : providers.values()) {
            if (provider.getNamespace().equals("minecraft")) continue;

            String id = provider.getId(block);
            if (id != null) {
                return provider.getNamespace() + ":" + id;
            }
        }

        // Фолбэк на ваниллу
        BlockProvider vanilla = providers.get("minecraft");
        if (vanilla != null) {
            String vanillaId = vanilla.getId(block);
            if (vanillaId != null) return "minecraft:" + vanillaId;
        }

        return "minecraft:" + block.getType().name().toLowerCase();
    }

    public java.util.Collection<BlockProvider> getProviders() {
        return providers.values();
    }

    public BlockProvider getProvider(String namespace) {
        return providers.get(namespace.toLowerCase());
    }
}
