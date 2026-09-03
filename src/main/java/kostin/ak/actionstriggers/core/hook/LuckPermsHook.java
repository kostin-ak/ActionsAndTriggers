package kostin.ak.actionstriggers.core.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Мягкая интеграция с LuckPerms API (LuckPermsHook).
 * Использует изолированный вложенный делегат для предотвращения ошибок загрузчика классов (NoClassDefFoundError)
 * при отсутствии плагина LuckPerms на сервере.
 */
public final class LuckPermsHook {

    private static boolean enabled = false;

    private LuckPermsHook() {}

    public static void initialize() {
        try {
            if (Bukkit.getPluginManager() != null && Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
                enabled = LuckPermsDelegate.init();
            } else {
                enabled = false;
            }
        } catch (Throwable ignored) {
            enabled = false;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static @NotNull String getPrimaryGroup(@NotNull Player player) {
        if (!enabled) return "default";
        return LuckPermsDelegate.getPrimaryGroup(player);
    }

    public static @NotNull String getPrefix(@NotNull Player player) {
        if (!enabled) return "";
        return LuckPermsDelegate.getPrefix(player);
    }

    public static @NotNull String getSuffix(@NotNull Player player) {
        if (!enabled) return "";
        return LuckPermsDelegate.getSuffix(player);
    }

    public static int getWeight(@NotNull Player player) {
        if (!enabled) return 0;
        return LuckPermsDelegate.getWeight(player);
    }

    public static boolean inGroup(@NotNull Player player, @NotNull String groupName) {
        if (!enabled) return false;
        return LuckPermsDelegate.inGroup(player, groupName);
    }

    public static void addPermission(@NotNull Player player, @NotNull String permission) {
        if (!enabled) return;
        LuckPermsDelegate.addPermission(player, permission);
    }

    public static void removePermission(@NotNull Player player, @NotNull String permission) {
        if (!enabled) return;
        LuckPermsDelegate.removePermission(player, permission);
    }

    public static void setGroup(@NotNull Player player, @NotNull String groupName) {
        if (!enabled) return;
        LuckPermsDelegate.setGroup(player, groupName);
    }

    /**
     * Изолированный делегат, загружаемый JVM только при фактическом наличии LuckPerms на сервере.
     */
    private static final class LuckPermsDelegate {

        private static net.luckperms.api.LuckPerms api;

        static boolean init() {
            try {
                api = net.luckperms.api.LuckPermsProvider.get();
                return api != null;
            } catch (Throwable ignored) {
                return false;
            }
        }

        static @NotNull String getPrimaryGroup(@NotNull Player player) {
            try {
                if (api != null) {
                    net.luckperms.api.model.user.User user = api.getUserManager().getUser(player.getUniqueId());
                    if (user != null) return user.getPrimaryGroup();
                }
            } catch (Throwable ignored) {}
            return "default";
        }

        static @NotNull String getPrefix(@NotNull Player player) {
            try {
                if (api != null) {
                    net.luckperms.api.model.user.User user = api.getUserManager().getUser(player.getUniqueId());
                    if (user != null) {
                        String prefix = user.getCachedData().getMetaData().getPrefix();
                        return prefix != null ? prefix : "";
                    }
                }
            } catch (Throwable ignored) {}
            return "";
        }

        static @NotNull String getSuffix(@NotNull Player player) {
            try {
                if (api != null) {
                    net.luckperms.api.model.user.User user = api.getUserManager().getUser(player.getUniqueId());
                    if (user != null) {
                        String suffix = user.getCachedData().getMetaData().getSuffix();
                        return suffix != null ? suffix : "";
                    }
                }
            } catch (Throwable ignored) {}
            return "";
        }

        static int getWeight(@NotNull Player player) {
            try {
                if (api != null) {
                    net.luckperms.api.model.user.User user = api.getUserManager().getUser(player.getUniqueId());
                    if (user != null) {
                        net.luckperms.api.model.group.Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());
                        if (group != null && group.getWeight().isPresent()) {
                            return group.getWeight().getAsInt();
                        }
                    }
                }
            } catch (Throwable ignored) {}
            return 0;
        }

        static boolean inGroup(@NotNull Player player, @NotNull String groupName) {
            try {
                if (api != null) {
                    net.luckperms.api.model.user.User user = api.getUserManager().getUser(player.getUniqueId());
                    if (user != null) {
                        return user.getNodes().stream()
                                .filter(node -> node instanceof net.luckperms.api.node.types.InheritanceNode)
                                .map(node -> ((net.luckperms.api.node.types.InheritanceNode) node).getGroupName())
                                .anyMatch(name -> name.equalsIgnoreCase(groupName));
                    }
                }
            } catch (Throwable ignored) {}
            return false;
        }

        static void addPermission(@NotNull Player player, @NotNull String permission) {
            try {
                if (api != null) {
                    api.getUserManager().modifyUser(player.getUniqueId(), user -> {
                        user.data().add(net.luckperms.api.node.types.PermissionNode.builder(permission).build());
                    });
                }
            } catch (Throwable ignored) {}
        }

        static void removePermission(@NotNull Player player, @NotNull String permission) {
            try {
                if (api != null) {
                    api.getUserManager().modifyUser(player.getUniqueId(), user -> {
                        user.data().remove(net.luckperms.api.node.types.PermissionNode.builder(permission).build());
                    });
                }
            } catch (Throwable ignored) {}
        }

        static void setGroup(@NotNull Player player, @NotNull String groupName) {
            try {
                if (api != null) {
                    api.getUserManager().modifyUser(player.getUniqueId(), user -> {
                        user.setPrimaryGroup(groupName);
                        user.data().add(net.luckperms.api.node.types.InheritanceNode.builder(groupName).build());
                    });
                }
            } catch (Throwable ignored) {}
        }
    }
}
