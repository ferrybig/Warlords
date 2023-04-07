package com.ebicep.warlords.pve.items.types.specialitems.gammagauntlet;

import com.ebicep.warlords.player.general.Classes;
import com.ebicep.warlords.pve.items.ItemTier;
import com.ebicep.warlords.pve.items.statpool.StatPool;

import java.util.HashMap;

public class ShadowGauntlet extends SpecialGammaGauntlet implements EPSandSpeed {

    @Override
    public HashMap<StatPool, Integer> getBonusStats() {
        return EPSandSpeed.super.getBonusStats();
    }

    public ShadowGauntlet(ItemTier tier) {
        super(tier);
    }

    @Override
    public Classes getClasses() {
        return Classes.SHAMAN;
    }

    @Override
    public String getName() {
        return "Shadow Gauntlet";
    }

    @Override
    public String getBonus() {
        return "+5 EPS but -20% Speed";
    }

    @Override
    public String getDescription() {
        return "One touch and you're floating.";
    }

}
