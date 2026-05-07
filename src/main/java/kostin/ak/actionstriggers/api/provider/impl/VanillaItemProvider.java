package kostin.ak.actionstriggers.api.provider.impl;

import kostin.ak.actionstriggers.api.provider.ItemProvider;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Пример VanillaItemProvider.java
public class VanillaItemProvider implements ItemProvider {
    @Override
    public @NotNull String getNamespace() { return "minecraft"; }

    @Override
    public @Nullable ItemStack getItem(@NotNull String id) {
        Material mat = Material.matchMaterial(id);
        return mat != null ? new ItemStack(mat) : null;
    }

    @Override
    public @Nullable String getId(@NotNull ItemStack item) {
        return item.getType().name().toLowerCase();
    }
}