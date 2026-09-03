package kostin.ak.actionstriggers.core.config;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.parser.AATParser;
import kostin.ak.actionstriggers.api.parser.AATParser.ParsedTrigger;
import kostin.ak.actionstriggers.core.i18n.I18n;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Загрузчик триггеров и скриптов из каталога triggers/.
 */
public final class YamlTriggerLoader {

    private YamlTriggerLoader() {}

    /**
     * Загружает все триггеры из указанной директории.
     *
     * @param plugin Плагин-владелец
     * @param folderName Имя папки
     * @return Количество успешно загруженных триггеров
     */
    public static int load(Plugin plugin, String folderName) {
        File folder = new File(plugin.getDataFolder(), folderName);
        if (!folder.exists() || !folder.isDirectory()) {
            return 0;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files == null || files.length == 0) {
            return 0;
        }

        AATParser parser = new AATParser();
        ActionTriggerAPI.getScripts().clear();
        ActionTriggerAPI.getTriggers().unsubscribeAll(plugin);

        int totalLoaded = 0;

        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<Map<?, ?>> rawTriggers = config.getMapList("triggers");
            if (rawTriggers.isEmpty()) {
                continue;
            }

            boolean hasLoadedSomething = false;

            for (Map<?, ?> rawMap : rawTriggers) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) rawMap;

                    List<Action> actions = parser.parseActions(map.get("actions"));
                    ParsedTrigger parsedTrigger = parser.parseTrigger(plugin, map, context -> {
                        for (Action action : actions) {
                            action.execute(context);
                        }
                    });

                    ActionTriggerAPI.getTriggers().subscribe(
                            parsedTrigger.triggerKey(),
                            parsedTrigger.subscription().plugin(),
                            parsedTrigger.subscription().filter(),
                            parsedTrigger.subscription().callback()
                    );

                    totalLoaded++;
                    hasLoadedSomething = true;
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to load trigger from file " + file.getName(), e);
                }
            }

            if (hasLoadedSomething) {
                ActionTriggerAPI.getScripts().register(file.getName());
            }
        }

        plugin.getLogger().info(I18n.get("log.triggers_loaded", Map.of("count", totalLoaded)));
        return totalLoaded;
    }
}