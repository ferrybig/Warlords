package com.ebicep.warlords.pve.items.types.specialitems.tome.gamma;

import com.ebicep.warlords.player.general.Specializations;
import com.ebicep.warlords.pve.items.statpool.BasicStatPool;
import com.ebicep.warlords.pve.items.types.specialitems.CraftsInto;

import java.util.Set;

public class BookOfActs extends SpecialGammaTome implements CraftsInto.CraftsThePresentTestament {

    public BookOfActs() {

    }

    public BookOfActs(Set<BasicStatPool> basicStatPools) {
        super(basicStatPools);
    }

    @Override
    public String getName() {
        return "Book of Acts";
    }

    @Override
    public String getBonus() {
        return "+5% Cooldown Reduction but -20% Crit Chance.";
    }

    @Override
    public String getDescription() {
        return "A tale of twelve men.";
    }


    @Override
    public Specializations getSpec() {
        return Specializations.CRUSADER;
    }
}
