package com.ebicep.warlords.game.option;

import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class ApplySkillBoostOption implements Option {

    @Override
    public void onWarlordsEntityCreated(@Nonnull WarlordsEntity wp) {
        if (wp instanceof WarlordsPlayer && wp.getEntity() instanceof Player) {
            wp.applySkillBoost((Player) wp.getEntity());
        }
    }

    @Override
    public void onSpecChange(@Nonnull WarlordsEntity wp) {
        if (wp instanceof WarlordsPlayer && wp.getEntity() instanceof Player) {
            wp.applySkillBoost((Player) wp.getEntity());
        }
    }
}
