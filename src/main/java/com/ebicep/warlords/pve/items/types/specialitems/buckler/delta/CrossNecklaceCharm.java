package com.ebicep.warlords.pve.items.types.specialitems.buckler.delta;

import com.ebicep.warlords.abilties.Consecrate;
import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.game.option.pve.PveOption;
import com.ebicep.warlords.player.general.Classes;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.pve.items.statpool.BasicStatPool;
import com.ebicep.warlords.pve.items.types.AbstractItem;
import com.ebicep.warlords.pve.items.types.specialitems.CraftsInto;
import com.ebicep.warlords.pve.items.types.specialitems.buckler.omega.BreastplateBuckler;

import java.util.Set;

public class CrossNecklaceCharm extends SpecialDeltaBuckler implements CraftsInto {

    public CrossNecklaceCharm(Set<BasicStatPool> statPool) {
        super(statPool);
    }

    public CrossNecklaceCharm() {

    }

    @Override
    public String getName() {
        return "Cross Necklace Chakram";
    }

    @Override
    public String getBonus() {
        return "Consecrate lasts 2 more seconds.";
    }

    @Override
    public String getDescription() {
        return "Exorcism on the go!";
    }

    @Override
    public Classes getClasses() {
        return Classes.PALADIN;
    }

    @Override
    public void applyToWarlordsPlayer(WarlordsPlayer warlordsPlayer, PveOption pveOption) {
        for (AbstractAbility ability : warlordsPlayer.getSpec().getAbilities()) {
            if (ability instanceof Consecrate) {
                Consecrate consecrate = (Consecrate) ability;
                consecrate.setTickDuration(consecrate.getTickDuration() + 40);
            }
        }
    }

    @Override
    public AbstractItem getCraftsInto(Set<BasicStatPool> statPool) {
        return new BreastplateBuckler(statPool);
    }
}
