package com.ebicep.warlords.pve.bountysystem.rewards;

import com.ebicep.warlords.guilds.GuildSpendable;
import com.ebicep.warlords.pve.Currencies;
import com.ebicep.warlords.pve.Spendable;
import com.ebicep.warlords.pve.items.types.SpendableRandomItem;

import java.util.LinkedHashMap;

public interface LifetimeRewardSpendable1 extends RewardSpendable {

    LinkedHashMap<Spendable, Long> REWARD = new LinkedHashMap<>() {{
        put(Currencies.SYNTHETIC_SHARD, 8000L);
        put(Currencies.LEGEND_FRAGMENTS, 3500L);
        put(SpendableRandomItem.DELTA, 1L);
        put(GuildSpendable.GUILD_COIN, 6000L);
        put(GuildSpendable.GUILD_EXPERIENCE, 6000L);
    }};

    @Override
    default LinkedHashMap<Spendable, Long> getCurrencyReward() {
        return REWARD;
    }

}
