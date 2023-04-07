package com.ebicep.warlords.pve.items.types.specialitems.gammagauntlet;

import com.ebicep.warlords.player.general.Classes;
import com.ebicep.warlords.pve.items.ItemTier;
import com.ebicep.warlords.pve.items.statpool.StatPool;

import java.util.HashMap;

public class ConquerorsFist extends SpecialGammaGauntlet implements EPSandSpeed {

    @Override
    public HashMap<StatPool, Integer> getBonusStats() {
        return EPSandSpeed.super.getBonusStats();
    }

    public ConquerorsFist(ItemTier tier) {
        super(tier);
    }

    @Override
    public Classes getClasses() {
        return Classes.PALADIN;
    }

    @Override
    public String getName() {
        return "Conqueror's Fist";
    }

    @Override
    public String getBonus() {
        return "+5 EPS but -20% Speed.";
    }

    @Override
    public String getDescription() {
        return "Kneel before your ruler, Godfrey of Boullion!";
    }

}
