package com.ebicep.warlords.pve.items.types.specialitems.gammagauntlet;

import com.ebicep.warlords.player.general.Classes;
import com.ebicep.warlords.pve.items.ItemTier;
import com.ebicep.warlords.pve.items.statpool.StatPool;

import java.util.HashMap;

public class BarbaricClaws extends SpecialGammaGauntlet implements EPSandMaxEnergy {

    @Override
    public HashMap<StatPool, Integer> getBonusStats() {
        return EPSandMaxEnergy.super.getBonusStats();
    }

    public BarbaricClaws(ItemTier tier) {
        super(tier);
    }

    @Override
    public Classes getClasses() {
        return Classes.WARRIOR;
    }

    @Override
    public String getName() {
        return "Barbaric Claws";
    }

    @Override
    public String getBonus() {
        return "+5 EPS but -20% Max NRG.";
    }

    @Override
    public String getDescription() {
        return "Only a madman would wear such an instrument.";
    }

}
