package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractStrikeBase;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.WarlordsNPC;
import com.ebicep.warlords.util.java.Pair;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AvengersStrike extends AbstractStrikeBase {
    private boolean pveUpgrade = false;
    protected float energyStole = 0;
    private float energySteal = 10;

    public AvengersStrike() {
        super("Avenger's Strike", 359, 485, 0, 90, 25, 185);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Strike the targeted enemy player,\n" +
                "§7causing §c" + format(minDamageHeal) + " §7- §c" + format(maxDamageHeal) + " §7damage\n" +
                "§7and removing §e" + energySteal + " §7energy.";
    }

    @Override
    public List<Pair<String, String>> getAbilityInfo() {
        List<Pair<String, String>> info = new ArrayList<>();
        info.add(new Pair<>("Players Struck", "" + timesUsed));
        info.add(new Pair<>("Energy Removed", "" + Math.round(energyStole)));

        return info;
    }

    @Override
    protected void onHit(@Nonnull WarlordsEntity wp, @Nonnull Player player, @Nonnull WarlordsEntity nearPlayer) {
        float multiplier = 1;
        if (nearPlayer instanceof WarlordsNPC) {
            if (!pveUpgrade) return;
            switch (((WarlordsNPC) nearPlayer).getMobTier()) {
                case BASE:
                    multiplier = 1.4f;
                    break;
                case ELITE:
                    multiplier = 1.2f;
                    break;
                default:
                    multiplier = 1;
                    break;
            }
        }

        Optional<Consecrate> optionalConsecrate = getStandingOnConsecrate(wp, nearPlayer);
        if (optionalConsecrate.isPresent()) {
            wp.doOnStaticAbility(Consecrate.class, Consecrate::addStrikesBoosted);
            nearPlayer.addDamageInstance(
                    wp,
                    name,
                    (minDamageHeal * (1 + optionalConsecrate.get().getStrikeDamageBoost() / 100f)) * multiplier,
                    (maxDamageHeal * (1 + optionalConsecrate.get().getStrikeDamageBoost() / 100f)) * multiplier,
                    critChance,
                    critMultiplier,
                    false
            );
        } else {
            nearPlayer.addDamageInstance(
                    wp,
                    name,
                    minDamageHeal * multiplier,
                    maxDamageHeal * multiplier,
                    critChance,
                    critMultiplier,
                    false
            );
        }

        energyStole += nearPlayer.subtractEnergy(energySteal, true);
    }

    public boolean isPveUpgrade() {
        return pveUpgrade;
    }

    public void setPveUpgrade(boolean pveUpgrade) {
        this.pveUpgrade = pveUpgrade;
    }

    public float getEnergySteal() {
        return energySteal;
    }

    public void setEnergySteal(float energySteal) {
        this.energySteal = energySteal;
    }
}
