package kostin.ak.actionstriggers.core.gui;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.filter.Filter;
import kostin.ak.actionstriggers.api.gui.widget.Widget;
import kostin.ak.actionstriggers.api.gui.widget.impl.*;
import kostin.ak.actionstriggers.api.parser.AATParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Загрузчик GUI-конфигураций из YAML файлов.
 */
public class YamlGuiLoader {

    private final AATParser parser = new AATParser();
    private final GuiRegistry registry;
    private final Logger logger;

    public YamlGuiLoader(GuiRegistry registry, Logger logger) {
        this.registry = registry;
        this.logger = logger;
    }

    public int loadAll(Plugin plugin, String folderName) {
        File folder = new File(plugin.getDataFolder(), folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files == null) return 0;

        int loaded = 0;
        for (File file : files) {
            try {
                GuiDefinition def = loadFile(file);
                if (def != null) {
                    registry.register(def);
                    loaded++;
                }
            } catch (Exception e) {
                logger.log(java.util.logging.Level.SEVERE, kostin.ak.actionstriggers.core.i18n.I18n.get("log.gui_load_error", Map.of("file", file.getName(), "error", e.getMessage())), e);
            }
        }

        logger.info(kostin.ak.actionstriggers.core.i18n.I18n.get("log.guis_loaded", Map.of("count", loaded)));
        return loaded;
    }

    @SuppressWarnings("unchecked")
    public GuiDefinition loadFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String id = config.getString("id");
        if (id == null || id.isEmpty()) {
            id = file.getName().replace(".yml", "").replace(".yaml", "");
        }

        String title = config.getString("title", "Menu");
        int rows = config.getInt("rows", 3);
        String targetBlock = config.getString("target_block");

        GuiDefinition def = new GuiDefinition(id, title, rows);
        def.setTargetBlock(targetBlock);

        // 1. Колбеки on_open / on_close
        if (config.contains("on_open")) {
            def.setOnOpenActions(parser.parseActions(config.get("on_open")));
        }
        if (config.contains("on_close")) {
            def.setOnCloseActions(parser.parseActions(config.get("on_close")));
        }

        // 2. Шаблон маски (mask)
        if (config.contains("mask")) {
            ConfigurationSection maskSec = config.getConfigurationSection("mask");
            if (maskSec != null) {
                List<String> pattern = maskSec.getStringList("pattern");
                Map<Character, Widget> components = new HashMap<>();

                ConfigurationSection compSec = maskSec.getConfigurationSection("components");
                if (compSec != null) {
                    for (String key : compSec.getKeys(false)) {
                        if (key.isEmpty()) continue;
                        char ch = key.charAt(0);
                        ConfigurationSection itemSec = compSec.getConfigurationSection(key);
                        if (itemSec != null) {
                            Widget widget = parseWidget(itemSec);
                            if (widget != null) {
                                components.put(ch, widget);
                            }
                        }
                    }
                }

                MaskWidget maskWidget = new MaskWidget(pattern, components);
                def.getWidgets().add(maskWidget);
            }
        }

        // 3. Прямой список элементов (items / widgets)
        ConfigurationSection itemsSec = config.getConfigurationSection("items");
        if (itemsSec == null) {
            itemsSec = config.getConfigurationSection("widgets");
        }

        if (itemsSec != null) {
            for (String itemKey : itemsSec.getKeys(false)) {
                ConfigurationSection itemSec = itemsSec.getConfigurationSection(itemKey);
                if (itemSec != null) {
                    Widget widget = parseWidget(itemSec);
                    if (widget != null) {
                        def.getWidgets().add(widget);
                    }
                }
            }
        }

        return def;
    }

    @SuppressWarnings("unchecked")
    private Widget parseWidget(ConfigurationSection sec) {
        String type = sec.getString("type", "button").toLowerCase();

        int x = sec.getInt("x", 0);
        int y = sec.getInt("y", 0);

        if (sec.contains("slot")) {
            int slot = sec.getInt("slot");
            x = slot % 9;
            y = slot / 9;
        }

        Filter condition = null;
        if (sec.contains("condition") || sec.contains("conditions")) {
            Object condObj = sec.get("condition") != null ? sec.get("condition") : sec.get("conditions");
            condition = parser.parseConditions(condObj);
        }

        switch (type) {
            case "transparent_slot":
            case "invisible_slot":
            case "slot_cover":
            case "cover":
            case "blank": {
                kostin.ak.actionstriggers.api.gui.widget.impl.SlotCoverWidget cover =
                        new kostin.ak.actionstriggers.api.gui.widget.impl.SlotCoverWidget(x, y);
                cover.setCondition(condition);

                boolean isTrans = type.equals("transparent_slot") || type.equals("invisible_slot") || sec.getBoolean("transparent", false);
                cover.setTransparent(isTrans);

                if (sec.contains("material") || sec.contains("item")) {
                    cover.setMaterialStr(sec.getString("material", sec.getString("item", isTrans ? "oraxen:gui_transparent_slot" : "oraxen:gui_slot_cover")));
                }
                if (sec.contains("fallback")) {
                    cover.setFallbackMaterial(sec.getString("fallback"));
                }
                return cover;
            }

            case "filler": {
                FillerWidget filler = new FillerWidget();
                filler.setX(x);
                filler.setY(y);
                filler.setCondition(condition);
                filler.setMaterialStr(sec.getString("material", sec.getString("item", "minecraft:gray_stained_glass_pane")));
                filler.setName(sec.getString("name", " "));
                if (sec.contains("slots")) {
                    filler.setSlots(sec.getIntegerList("slots"));
                }
                return filler;
            }

            case "input_slot": {
                InputSlotWidget slot = new InputSlotWidget(x, y);
                slot.setCondition(condition);
                slot.setAllowedItems(sec.getStringList("allowed_items"));
                slot.setPlaceholderMaterial(sec.getString("placeholder_material"));
                slot.setPlaceholderName(sec.getString("placeholder_name"));
                if (sec.contains("placeholder_lore")) {
                    slot.setPlaceholderLore(sec.getStringList("placeholder_lore"));
                }
                if (sec.contains("on_insert")) {
                    slot.setOnInsert(parser.parseActions(sec.get("on_insert")));
                }
                if (sec.contains("on_extract")) {
                    slot.setOnExtract(parser.parseActions(sec.get("on_extract")));
                }
                return slot;
            }

            case "output_slot": {
                OutputSlotWidget slot = new OutputSlotWidget(x, y);
                slot.setCondition(condition);
                slot.setPlaceholderMaterial(sec.getString("placeholder_material"));
                slot.setPlaceholderName(sec.getString("placeholder_name"));
                if (sec.contains("placeholder_lore")) {
                    slot.setPlaceholderLore(sec.getStringList("placeholder_lore"));
                }
                if (sec.contains("on_take")) {
                    slot.setOnTake(parser.parseActions(sec.get("on_take")));
                }
                return slot;
            }

            case "progress_bar": {
                kostin.ak.actionstriggers.api.gui.widget.impl.ProgressBarWidget pb =
                        new kostin.ak.actionstriggers.api.gui.widget.impl.ProgressBarWidget(x, y);
                pb.setCondition(condition);

                if (sec.contains("idle_material")) pb.setIdleMaterial(sec.getString("idle_material"));
                else if (sec.contains("material")) pb.setIdleMaterial(sec.getString("material"));

                if (sec.contains("idle_name")) pb.setIdleName(sec.getString("idle_name"));
                else if (sec.contains("name")) pb.setIdleName(sec.getString("name"));

                if (sec.contains("idle_lore")) pb.setIdleLore(sec.getStringList("idle_lore"));
                else if (sec.contains("lore")) pb.setIdleLore(sec.getStringList("lore"));

                if (sec.contains("running_material")) pb.setRunningMaterial(sec.getString("running_material"));
                if (sec.contains("running_name")) pb.setRunningName(sec.getString("running_name"));
                if (sec.contains("running_lore")) pb.setRunningLoreTemplate(sec.getStringList("running_lore"));

                if (sec.contains("bar_length")) pb.setBarLength(sec.getInt("bar_length", 10));
                if (sec.contains("filled_color")) pb.setFilledColor(sec.getString("filled_color"));
                if (sec.contains("empty_color")) pb.setEmptyColor(sec.getString("empty_color"));

                if (sec.contains("on_click")) {
                    for (Action a : parser.parseActions(sec.get("on_click"))) {
                        pb.addClickAction(a);
                    }
                }
                return pb;
            }

            case "toggle": {
                ToggleWidget toggle = new ToggleWidget();
                toggle.setX(x);
                toggle.setY(y);
                toggle.setCondition(condition);
                toggle.setPersistentKey(sec.getString("persistent_key"));
                toggle.setSessionKey(sec.getString("session_key"));
                toggle.setDefaultState(sec.getBoolean("default", false));

                ConfigurationSection onSec = sec.getConfigurationSection("on_state");
                if (onSec != null) {
                    toggle.getOnState().setMaterialStr(onSec.getString("material", "minecraft:lime_dye"));
                    toggle.getOnState().setName(onSec.getString("name", "<green>ВКЛ</green>"));
                    toggle.getOnState().setLore(onSec.getStringList("lore"));
                }

                ConfigurationSection offSec = sec.getConfigurationSection("off_state");
                if (offSec != null) {
                    toggle.getOffState().setMaterialStr(offSec.getString("material", "minecraft:gray_dye"));
                    toggle.getOffState().setName(offSec.getString("name", "<gray>ВЫКЛ</gray>"));
                    toggle.getOffState().setLore(offSec.getStringList("lore"));
                }

                if (sec.contains("on_change")) {
                    toggle.setOnChange(parser.parseActions(sec.get("on_change")));
                }
                return toggle;
            }

            case "cycle_button": {
                CycleButtonWidget cycle = new CycleButtonWidget();
                cycle.setX(x);
                cycle.setY(y);
                cycle.setCondition(condition);
                cycle.setPersistentKey(sec.getString("persistent_key"));
                cycle.setSessionKey(sec.getString("session_key"));

                List<Map<?, ?>> rawStates = sec.getMapList("states");
                for (Map<?, ?> raw : rawStates) {
                    Map<String, Object> stateMap = (Map<String, Object>) raw;
                    CycleButtonWidget.StateEntry entry = new CycleButtonWidget.StateEntry();
                    entry.setId(stateMap.containsKey("id") ? String.valueOf(stateMap.get("id")) : "state");
                    Object mat = stateMap.containsKey("material") ? stateMap.get("material") : stateMap.get("item");
                    entry.setMaterialStr(mat != null ? String.valueOf(mat) : "minecraft:stone");
                    entry.setName(stateMap.containsKey("name") ? String.valueOf(stateMap.get("name")) : "");
                    if (stateMap.containsKey("lore")) {
                        entry.setLore((List<String>) stateMap.get("lore"));
                    }
                    if (stateMap.containsKey("actions")) {
                        entry.setActions(parser.parseActions(stateMap.get("actions")));
                    }
                    cycle.getStates().add(entry);
                }
                return cycle;
            }

            case "button":
            default: {
                ButtonWidget button = new ButtonWidget(x, y);
                button.setCondition(condition);
                button.setMaterialStr(sec.getString("material", sec.getString("item", "minecraft:stone")));
                button.setName(sec.getString("name"));
                button.setLore(sec.getStringList("lore"));
                button.setAmount(sec.getInt("amount", 1));
                button.setCustomModelData(sec.getInt("custom_model_data", 0));

                Object actionsObj = sec.get("actions") != null ? sec.get("actions") : sec.get("on_click");
                if (actionsObj != null) {
                    button.setActions(parser.parseActions(actionsObj));
                }
                return button;
            }
        }
    }
}
