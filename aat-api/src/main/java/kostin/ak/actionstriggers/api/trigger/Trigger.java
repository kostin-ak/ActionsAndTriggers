package kostin.ak.actionstriggers.api.trigger;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import org.bukkit.NamespacedKey;

import java.util.Collections;
import java.util.List;

public abstract class Trigger {
    public void dispatch(ExecutionContext context){
        ActionTriggerAPI.getTriggers().dispatch(getKey(), context);
    }
    public abstract NamespacedKey getKey();
    public List<ContextKey<?>> getProvidedContext() {
        return Collections.emptyList();
    }
}
