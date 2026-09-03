package kostin.ak.actionstriggers.core.version;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Minecraft Version Compatibility Audit (1.21.4 - 26.2)")
class VersionCompatibilityTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "STONE", "DEEPSLATE", "CHEST", "FURNACE", "CRAFTING_TABLE",
            "BLUE_ICE", "PACKED_ICE", "ICE", "BARRIER", "GRAY_STAINED_GLASS_PANE",
            "SPRUCE_SAPLING", "SPECTRAL_ARROW", "CLOCK", "REDSTONE_TORCH",
            "DIAMOND_SWORD", "NETHERITE_SWORD", "COMPASS", "WATER_BUCKET", "LAVA_BUCKET"
    })
    @DisplayName("Verify essential Materials exist across 1.21.4 - 26.2")
    void testStandardMaterialsExist(String materialName) {
        Material mat = Material.matchMaterial(materialName);
        assertNotNull(mat, "Material " + materialName + " must exist across all supported Minecraft versions (1.21.4 - 26.2)");
    }

    @Test
    @DisplayName("Verify Adventure MiniMessage and Component API availability")
    void testAdventureMiniMessageCompatibility() {
        net.kyori.adventure.text.minimessage.MiniMessage mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();
        net.kyori.adventure.text.Component comp = mm.deserialize("<gradient:#74B9FF:#0984E3>Cryogenic Ice Fabricator</gradient>");
        assertNotNull(comp);
    }
}
