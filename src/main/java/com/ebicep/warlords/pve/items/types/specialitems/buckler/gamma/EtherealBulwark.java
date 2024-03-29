package com.ebicep.warlords.pve.items.types.specialitems.buckler.gamma;

import com.ebicep.warlords.player.general.Specializations;
import com.ebicep.warlords.pve.items.statpool.BasicStatPool;
import com.ebicep.warlords.pve.items.types.specialitems.CraftsInto;

import java.util.Set;

public class EtherealBulwark extends SpecialGammaBuckler implements CraftsInto.CraftsAerialAegis {

    public EtherealBulwark() {
    }

    public EtherealBulwark(Set<BasicStatPool> basicStatPools) {
        super(basicStatPools);
    }

    @Override
    public String getName() {
        return "Ethereal Bulwark";
    }

    @Override
    public String getBonus() {
        return "+5% Damage Reduction but -5 Aggro Priority.";
    }

    @Override
    public String getDescription() {
        return "Basically nothing on a stick.";
    }


    @Override
    public Specializations getSpec() {
        return Specializations.SPIRITGUARD;
    }
}