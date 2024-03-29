package com.ebicep.warlords.guilds.logs.types.oneplayer.upgrades;

import com.ebicep.warlords.guilds.logs.types.oneplayer.AbstractGuildLogOnePlayer;
import com.ebicep.warlords.guilds.upgrades.GuildUpgrade;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public abstract class AbstractGuildLogUpgrade<T extends Enum<T> & GuildUpgrade> extends AbstractGuildLogOnePlayer {

    private T upgrade;
    private int tier;

    public AbstractGuildLogUpgrade(UUID sender, T upgrade, int tier) {
        super(sender);
        this.upgrade = upgrade;
        this.tier = tier;
    }

    @Override
    public Component append() {
        return Component.text(upgrade.getName() + " Tier " + tier);
    }

    public T getUpgrade() {
        return upgrade;
    }

    public int getTier() {
        return tier;
    }
}