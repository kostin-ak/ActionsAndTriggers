package kostin.ak.actionstriggers.api.provider;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockProvider {
    @NotNull String getNamespace();

    /**
     * Устанавливает кастомный блок по координатам существующего блока.
     */
    void setBlock(@NotNull Block block, @NotNull String id);

    /**
     * Возвращает локальный ID кастомного блока или null.
     */
    @Nullable String getId(@NotNull Block block);

    @NotNull
    default java.util.List<String> getAvailableIds() {
        return java.util.Collections.emptyList();
    }

}
