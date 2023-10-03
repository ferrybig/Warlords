package com.ebicep.warlords.pve.items.types.specialitems.tome.gamma;

import com.ebicep.warlords.pve.items.statpool.BasicStatPool;
import com.ebicep.warlords.pve.items.statpool.SpecialStatPool;
import com.ebicep.warlords.pve.items.statpool.StatPool;
import com.ebicep.warlords.pve.items.types.BonusStats;

import java.util.HashMap;

@Deprecated
public interface CDRandCritChance extends BonusStats {

    HashMap<StatPool, Integer> BONUS_STATS = new HashMap<>() {{
        put(SpecialStatPool.COOLDOWN_REDUCTION, 5);
        put(BasicStatPool.CRIT_CHANCE, -200);
    }};

    @Override
    default HashMap<StatPool, Integer> getBonusStats() {
        return BONUS_STATS;
    }

}
