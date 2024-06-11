package net.stone_labs.workinggraves;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;

public class PermissionManager {
    private final Map<Permission, Integer> map = new EnumMap<>(Permission.class);

    public static PermissionManager instance() {
        return new PermissionManager();
    }

    public PermissionManager() {
        map.put(Permission.USE, 0);
        map.put(Permission.NEW, 0);
        map.put(Permission.NEW_PUBLIC, 0);
        map.put(Permission.NEW_PRIVATE, 0);
        map.put(Permission.COMMAND_LIST, 2);
        map.put(Permission.COMMAND_FIND, 2);
        map.put(Permission.COMMAND_KEY, 2);
        map.put(Permission.COMMAND_DEBUG, 2);
        map.put(Permission.INTERDIMENSIONAL, 0);
    }

    public int defaultPermissionLevel(Permission permission) {
        return map.getOrDefault(permission, 0);
    }

    public void setPermissionLevel(Permission permission, int level) {
        map.put(permission, level);
    }

    public @NotNull Predicate<net.minecraft.server.command.ServerCommandSource> require(Permission permission) {
        return Permissions.require(permission.getKey(), defaultPermissionLevel(permission));
    }

    public boolean check(CommandSource source, Permission permission) {
        return Permissions.check(source, permission.getKey(), defaultPermissionLevel(permission));
    }

    public boolean check(Entity entity, Permission permission) {
        return Permissions.check(entity, permission.getKey(), defaultPermissionLevel(permission));
    }

    public enum Permission {
        USE("graves.use"),
        NEW("graves.new"),
        NEW_PUBLIC("graves.new.public"),
        NEW_PRIVATE("graves.new.private"),
        COMMAND_LIST("graves.command.list"),
        COMMAND_FIND("graves.command.find"),
        COMMAND_KEY("graves.command.key"),
        COMMAND_DEBUG("graves.command.debug"),
        INTERDIMENSIONAL("graves.interdimensional");

        private final String key;

        Permission(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
