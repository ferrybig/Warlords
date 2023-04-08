package com.ebicep.warlords.pve.items.types.specialitems.gauntlets.delta;

import com.ebicep.warlords.events.player.ingame.pve.WarlordsDropRewardEvent;
import com.ebicep.warlords.player.general.Classes;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.pve.items.types.AbstractItem;
import com.ebicep.warlords.pve.items.types.AppliesToWarlordsPlayer;
import com.ebicep.warlords.pve.items.types.specialitems.gauntlets.omega.NaturesClaws;
import com.ebicep.warlords.pve.mobs.AbstractMob;
import com.ebicep.warlords.pve.mobs.MobTier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GardeningGloves extends SpecialDeltaGauntlet implements AppliesToWarlordsPlayer {

    @Override
    public String getName() {
        return "Gardening Gloves";
    }

    @Override
    public String getBonus() {
        return "Increases your chances of finding Items when killing ELITE enemies.";
    }

    @Override
    public String getDescription() {
        return "Save the Earth, ride a... dolphin?";
    }

    @Override
    public Classes getClasses() {
        return Classes.SHAMAN;
    }

    @Override
    public void applyToWarlordsPlayer(WarlordsPlayer warlordsPlayer) {
        warlordsPlayer.getGame().registerEvents(new Listener() {

            @EventHandler
            public void onItemDrop(WarlordsDropRewardEvent event) {
                if (event.getRewardType() != WarlordsDropRewardEvent.RewardType.ITEM) {
                    return;
                }
                AbstractMob<?> deadMob = event.getDeadMob();
                if (deadMob.getMobTier() != MobTier.ELITE) {
                    return;
                }
                event.addModifier(.1);
            }
        });

    }

    @Override
    public AbstractItem getCraftsInto() {
        return new NaturesClaws();
    }
}
