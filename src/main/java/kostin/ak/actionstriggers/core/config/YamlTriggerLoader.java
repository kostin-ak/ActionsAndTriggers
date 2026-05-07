package kostin.ak.actionstriggers.core.config;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.parser.AATParser;
import kostin.ak.actionstriggers.api.parser.AATParser.ParsedTrigger;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.Map;

public class YamlTriggerLoader {

    public static void load(Plugin plugin, String folderName) {
        File folder = new File(plugin.getDataFolder(), folderName);

        // Если папки нет или это не папка — просто игнорируем, не засоряем файлы
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }

        AATParser parser = new AATParser();

        // Ищем все .yml файлы в папке
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) return;

        ActionTriggerAPI.getScripts().clear();

        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<Map<?, ?>> rawTriggers = config.getMapList("triggers");

            if (rawTriggers.isEmpty()) continue;

            boolean hasLoadedSomething = false; // Флаг успешной загрузки

            for (Map<?, ?> rawMap : rawTriggers) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) rawMap;

                    // 1. Парсим список экшенов.
                    // Это то, что будет выполняться, когда триггер сработает
                    List<Action> actions = parser.parseActions(map.get("actions"));

                    // 2. Парсим сам триггер, передавая выполнение экшенов как developerCallback
                    ParsedTrigger parsedTrigger = parser.parseTrigger(plugin, map, context -> {
                        for (Action action : actions) {
                            action.execute(context);
                        }
                    });

                    try {
                        // Достаем нужные компоненты из подписки и передаем в реестр
                        ActionTriggerAPI.getTriggers().subscribe(
                                parsedTrigger.triggerKey(),
                                parsedTrigger.subscription().plugin(),
                                parsedTrigger.subscription().filter(),
                                parsedTrigger.subscription().callback()
                        );

                        plugin.getLogger().info("Успешно загружен триггер " + parsedTrigger.triggerKey() + " из " + file.getName());
                    } catch (Exception e) {
                        plugin.getLogger().warning("Ошибка при подписке на триггер " + parsedTrigger.triggerKey() + ": " + e.getMessage());
                    }
                    hasLoadedSomething = true;
                } catch (Exception e) {
                    plugin.getLogger().severe("Ошибка при загрузке триггера из файла " + file.getName() + ": " + e.getMessage());
                }
            }
            if (hasLoadedSomething) {
                ActionTriggerAPI.getScripts().register(file.getName());
            }
        }
    }
}