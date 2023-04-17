package com.ebicep.warlords.pve.items.types.specialitems.tome.delta;

import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.player.general.Classes;
import com.ebicep.warlords.player.ingame.WarlordsNPC;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.pve.items.statpool.BasicStatPool;
import com.ebicep.warlords.pve.items.types.AbstractItem;
import com.ebicep.warlords.pve.items.types.specialitems.CraftsInto;
import com.ebicep.warlords.pve.items.types.specialitems.tome.omega.CommandmentNoEleven;
import com.ebicep.warlords.pve.mobs.MobTier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;
import java.util.Set;

public class ThePresentTestament extends SpecialDeltaTome implements CraftsInto {

    public ThePresentTestament(Set<BasicStatPool> statPool) {
        super(statPool);
    }

    public ThePresentTestament() {

    }

    @Override
    public String getName() {
        return "The Present Testament";
    }

    @Override
    public String getBonus() {
        return "ELITE mobs take true damage.";
    }

    @Override
    public String getDescription() {
        return "No longer Old nor New!";
    }

    @Override
    public Classes getClasses() {
        return Classes.PALADIN;
    }

    @Override
    public void applyToWarlordsPlayer(WarlordsPlayer warlordsPlayer) {
        warlordsPlayer.getGame().registerEvents(new Listener() {

            @EventHandler
            public void onDamageHeal(WarlordsDamageHealingEvent event) {
                if (!Objects.equals(event.getAttacker(), warlordsPlayer)) {
                    return;
                }
                if (event.getWarlordsEntity() instanceof WarlordsNPC) {
                    WarlordsNPC warlordsNPC = (WarlordsNPC) event.getWarlordsEntity();
                    if (warlordsNPC.getMobTier() == MobTier.ELITE) {
                        event.setIgnoreReduction(true);
                    }
                }
            }

        });

    }

    @Override
    public AbstractItem getCraftsInto(Set<BasicStatPool> statPool) {
        return new CommandmentNoEleven(statPool);
    }
}
