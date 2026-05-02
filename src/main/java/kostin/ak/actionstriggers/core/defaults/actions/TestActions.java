package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ActionParameters;
import kostin.ak.actionstriggers.api.action.ConfigAction;
import kostin.ak.actionstriggers.api.action.IActionParsers;
import org.bukkit.Bukkit;

import java.util.Map;

public class TestActions implements IActionParsers {

    @ConfigAction("core:test")
    public static Action parseTest(Map<String, Object> params) {
        // Вызывается один раз при парсинге конфига
        Bukkit.getLogger().info("[TestActions] Парсинг экшена core:test прошел успешно!");

        return context -> {
            // Вызывается каждый раз при срабатывании
            ActionParameters actionParams = new ActionParameters(params, context);
            String testMessage = actionParams.getString("test_message", "Дефолтное сообщение");

            Bukkit.getLogger().info("====================================");
            Bukkit.getLogger().info("[TestActions] Сработал экшен core:test!");
            Bukkit.getLogger().info("[TestActions] test_message = " + testMessage);
            Bukkit.getLogger().info("[TestActions] Контекст: " + context.dump());
            Bukkit.getLogger().info("====================================");

            return true;
        };
    }
}