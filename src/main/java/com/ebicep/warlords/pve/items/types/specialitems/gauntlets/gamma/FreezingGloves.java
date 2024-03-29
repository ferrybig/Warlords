package com.ebicep.warlords.pve.items.types.specialitems.gauntlets.gamma;

import com.ebicep.warlords.player.general.Specializations;
import com.ebicep.warlords.pve.items.statpool.BasicStatPool;
import com.ebicep.warlords.pve.items.types.specialitems.CraftsInto;

import java.util.Set;

public class FreezingGloves extends SpecialGammaGauntlet implements CraftsInto.CraftsPalmOfTheSoothsayer {

    public FreezingGloves() {
    }

    public FreezingGloves(Set<BasicStatPool> basicStatPools) {
        super(basicStatPools);
    }

    @Override
    public String getName() {
        return "Freezing Gloves";
    }

    @Override
    public String getBonus() {
        return "+5 EPS but -20% Speed.";
    }

    @Override
    public String getDescription() {
        return "It seems you have been frozen into place.";
    }


    @Override
    public Specializations getSpec() {
        return Specializations.CRYOMANCER;
    }
}
