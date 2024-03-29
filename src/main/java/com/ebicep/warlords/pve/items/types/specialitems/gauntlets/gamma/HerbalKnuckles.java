package com.ebicep.warlords.pve.items.types.specialitems.gauntlets.gamma;

import com.ebicep.warlords.player.general.Specializations;
import com.ebicep.warlords.pve.items.statpool.BasicStatPool;
import com.ebicep.warlords.pve.items.types.specialitems.CraftsInto;

import java.util.Set;

public class HerbalKnuckles extends SpecialGammaGauntlet implements CraftsInto.CraftsMultipurposeKnuckles {

    public HerbalKnuckles() {
    }

    public HerbalKnuckles(Set<BasicStatPool> basicStatPools) {
        super(basicStatPools);
    }


    @Override
    public String getName() {
        return "Herbal Knuckles";
    }

    @Override
    public String getBonus() {
        return "+5 EPS but -20% EPH";
    }

    @Override
    public String getDescription() {
        return "Cuts like a rose.";
    }

    @Override
    public Specializations getSpec() {
        return Specializations.APOTHECARY;
    }
}
