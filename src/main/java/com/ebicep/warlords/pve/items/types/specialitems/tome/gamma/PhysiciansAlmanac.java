package com.ebicep.warlords.pve.items.types.specialitems.tome.gamma;

import com.ebicep.warlords.player.general.Specializations;
import com.ebicep.warlords.pve.items.statpool.BasicStatPool;
import com.ebicep.warlords.pve.items.types.specialitems.CraftsInto;

import java.util.Set;

public class PhysiciansAlmanac extends SpecialGammaTome implements CraftsInto.CraftsScrollOfUncertainty {

    public PhysiciansAlmanac() {

    }

    public PhysiciansAlmanac(Set<BasicStatPool> basicStatPools) {
        super(basicStatPools);
    }

    @Override
    public String getName() {
        return "Physician's Almanac";
    }

    @Override
    public String getBonus() {
        return "+5% Cooldown Reduction but -20% Healing.";
    }

    @Override
    public String getDescription() {
        return "200+ bones and I still don't know whats wrong with you!";
    }


    @Override
    public Specializations getSpec() {
        return Specializations.APOTHECARY;
    }
}
