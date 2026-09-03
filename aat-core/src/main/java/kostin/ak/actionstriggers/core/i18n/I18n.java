package kostin.ak.actionstriggers.core.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Централизованный сервис интернационализации и локализации (i18n).
 * Обеспечивает строгую дехардкодизацию системных сообщений, потокобезопасное кэширование
 * и интеграцию с Kyori Adventure MiniMessage.
 */
public final class I18n {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");

    private static final Map<String, String> TRANSLATIONS = new ConcurrentHashMap<>();
    private static final Map<String, Component> STATIC_CACHE = new ConcurrentHashMap<>();

    private static JavaPlugin plugin;
    private static String currentLanguage = "ru";

    private I18n() {}

    /**
     * Инициализирует сервис локализации, копирует языковые бандлы по умолчанию
     * и загружает активный словарь.
     *
     * @param pluginInstance Экземпляр главного плагина
     */
    public static void init(@NotNull JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        reload();
    }

    /**
     * Перезагружает активную конфигурацию языка и очищает кэш.
     */
    public static synchronized void reload() {
        if (plugin == null) return;

        TRANSLATIONS.clear();
        STATIC_CACHE.clear();

        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        saveDefaultLanguageFile(langFolder, "messages_ru.yml");
        saveDefaultLanguageFile(langFolder, "messages_en.yml");

        currentLanguage = plugin.getConfig().getString("language", "ru");
        String fileName = "messages_" + currentLanguage.toLowerCase() + ".yml";
        File langFile = new File(langFolder, fileName);

        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file " + fileName + " not found! Falling back to messages_ru.yml");
            langFile = new File(langFolder, "messages_ru.yml");
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        loadRecursive("", config);

        plugin.getLogger().info("Loaded " + TRANSLATIONS.size() + " localization keys for language [" + currentLanguage + "].");
    }

    private static void saveDefaultLanguageFile(@NotNull File folder, @NotNull String resourceName) {
        File targetFile = new File(folder, resourceName);
        if (!targetFile.exists()) {
            try (InputStream in = plugin.getResource("lang/" + resourceName)) {
                if (in != null) {
                    Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to extract default language file: " + resourceName, e);
            }
        }
    }

    private static void loadRecursive(@NotNull String prefix, @NotNull YamlConfiguration config) {
        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                TRANSLATIONS.put(key, config.getString(key, key));
            }
        }
    }

    /**
     * Возвращает сырую строку локализации без подстановки.
     */
    @NotNull
    public static String get(@NotNull String key) {
        return TRANSLATIONS.getOrDefault(key, key);
    }

    /**
     * Возвращает локализованную строку с подстановкой параметров {param}.
     */
    @NotNull
    public static String get(@NotNull String key, @Nullable Map<String, ?> params) {
        String raw = TRANSLATIONS.get(key);
        if (raw == null) return key;
        if (params == null || params.isEmpty() || !raw.contains("{")) return raw;

        Matcher matcher = PARAM_PATTERN.matcher(raw);
        StringBuilder sb = new StringBuilder(raw.length() + 32);
        while (matcher.find()) {
            String paramKey = matcher.group(1);
            Object value = params.get(paramKey);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value != null ? String.valueOf(value) : ""));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Возвращает десериализованный Adventure Component для статического ключа (с кэшированием).
     */
    @NotNull
    public static Component component(@NotNull String key) {
        return STATIC_CACHE.computeIfAbsent(key, k -> MINI_MESSAGE.deserialize(get(k)));
    }

    /**
     * Возвращает десериализованный Adventure Component с подстановкой параметров.
     */
    @NotNull
    public static Component component(@NotNull String key, @Nullable Map<String, ?> params) {
        if (params == null || params.isEmpty()) {
            return component(key);
        }
        return MINI_MESSAGE.deserialize(get(key, params));
    }

    /**
     * Возвращает компонент с добавлением системного префикса плагина.
     */
    @NotNull
    public static Component prefixed(@NotNull String key) {
        return component("prefix").append(component(key));
    }

    /**
     * Возвращает компонент с добавлением системного префикса плагина и подстановкой параметров.
     */
    @NotNull
    public static Component prefixed(@NotNull String key, @Nullable Map<String, ?> params) {
        return component("prefix").append(component(key, params));
    }
}
