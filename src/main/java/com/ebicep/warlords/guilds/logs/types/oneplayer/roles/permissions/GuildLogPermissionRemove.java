package com.ebicep.warlords.guilds.logs.types.oneplayer.roles.permissions;

import com.ebicep.warlords.guilds.GuildPermissions;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class GuildLogPermissionRemove extends AbstractGuildLogRolePermission {

    public GuildLogPermissionRemove(UUID sender, String role, GuildPermissions permission) {
        super(sender, role, permission);
    }

    @Override
    public Component getAction() {
        return Component.text("removed");
    }
}
