package com.ebicep.warlords.pve.bountysystem.bounties;

import com.ebicep.warlords.pve.bountysystem.AbstractBounty;
import com.ebicep.warlords.pve.bountysystem.Bounty;
import com.ebicep.warlords.pve.bountysystem.costs.EventCost;
import com.ebicep.warlords.pve.bountysystem.rewards.events.GardenOfHesperides2;

public class TartarusFlawlessI extends AbstractBounty implements EventCost, GardenOfHesperides2 {

    @Override
    public String getName() {
        return "Tartarus Flawless";
    }

    @Override
    public String getDescription() {
        return "Complete Tartarus without dying.";
    }

    @Override
    public int getTarget() {
        return 1;
    }

    @Override
    public Bounty getBounty() {
        return Bounty.TARTARUS_FLAWLESS_I;
    }

}
