package com.ebicep.warlords.pve.items.types.specialitems.tome.delta;

import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.game.option.pve.PveOption;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.PermanentCooldown;
import com.ebicep.warlords.pve.items.statpool.BasicStatPool;
import com.ebicep.warlords.pve.items.types.AbstractItem;
import com.ebicep.warlords.pve.items.types.specialitems.CraftsInto;
import com.ebicep.warlords.pve.items.types.specialitems.tome.omega.FlemingAlmanac;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class FirewaterAlmanac extends SpecialDeltaTome implements CraftsInto {

    public FirewaterAlmanac() {

    }

    public FirewaterAlmanac(Set<BasicStatPool> statPool) {
        super(statPool);
    }

    @Override
    public String getDescription() {
        return "Now Including Ice Spells!";
    }

    @Override
    public String getBonus() {
        return "+5% chance to not deal damage, but also +25% chance to reduce damage taken by 10%.";
    }

    @Override
    public String getName() {
        return "Firewater Grimiore";
    }

    @Override
    public void applyToWarlordsPlayer(WarlordsPlayer warlordsPlayer, PveOption pveOption) {
        warlordsPlayer.getCooldownManager().addCooldown(new PermanentCooldown<>(
                getName(),
                null,
                FirewaterAlmanac.class,
                null,
                warlordsPlayer,
                CooldownTypes.ITEM,
                cooldownManager -> {

                },
                false
        ) {
            @Override
            public float modifyDamageBeforeInterveneFromAttacker(WarlordsDamageHealingEvent event, float currentDamageValue) {
                return ThreadLocalRandom.current().nextDouble() < .05 ? 0 : currentDamageValue;
            }

            @Override
            public float modifyDamageAfterInterveneFromSelf(WarlordsDamageHealingEvent event, float currentDamageValue) {
                return ThreadLocalRandom.current().nextDouble() < .25 ? currentDamageValue * .9f : currentDamageValue;
            }
        });
    }

    @Override
    public AbstractItem getCraftsInto(Set<BasicStatPool> statPool) {
        return new FlemingAlmanac(statPool);
    }
}
