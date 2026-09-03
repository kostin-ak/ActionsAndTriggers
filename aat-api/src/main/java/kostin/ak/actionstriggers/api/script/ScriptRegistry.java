package kostin.ak.actionstriggers.api.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр загруженных скриптов (конфигурационных файлов).
 */
public class ScriptRegistry {
    // Используем потокобезопасный сет для хранения имен файлов
    private final Set<String> loadedScripts = ConcurrentHashMap.newKeySet();

    /**
     * Регистрирует успешно загруженный файл.
     */
    public void register(String scriptName) {
        loadedScripts.add(scriptName);
    }

    /**
     * Очищает реестр (полезно при команде /reload).
     */
    public void clear() {
        loadedScripts.clear();
    }

    /**
     * Возвращает список имен загруженных скриптов.
     */
    public List<String> getLoadedScripts() {
        return new ArrayList<>(loadedScripts);
    }
}
