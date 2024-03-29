package com.ebicep.warlords.pve.bountysystem.bounties;

import com.ebicep.warlords.pve.bountysystem.AbstractBounty;
import com.ebicep.warlords.pve.bountysystem.Bounty;
import com.ebicep.warlords.pve.bountysystem.costs.DailyCost;
import com.ebicep.warlords.pve.bountysystem.rewards.DailyRewardSpendable4;
import com.ebicep.warlords.pve.bountysystem.trackers.TracksOutsideGame;

public class RecoupI extends AbstractBounty implements TracksOutsideGame, DailyCost, DailyRewardSpendable4 {

    @Override
    public void onSupplyDropCall(long amount) {
        value += amount;
    }

    @Override
    public String getName() {
        return "Recoup";
    }

    @Override
    public String getDescription() {
        return "Call " + getTarget() + " Supply Drops.";
    }

    @Override
    public int getTarget() {
        return 10;
    }

    @Override
    public Bounty getBounty() {
        return Bounty.RECOUP_I;
    }


}
