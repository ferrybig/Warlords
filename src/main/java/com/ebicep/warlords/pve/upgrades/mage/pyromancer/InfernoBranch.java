package com.ebicep.warlords.pve.upgrades.mage.pyromancer;

import com.ebicep.warlords.abilities.Inferno;
import com.ebicep.warlords.pve.upgrades.*;

public class InfernoBranch extends AbstractUpgradeBranch<Inferno> {

    int critMultiplierIncrease = ability.getCritMultiplierIncrease();

    public InfernoBranch(AbilityTree abilityTree, Inferno ability) {
        super(abilityTree, ability);

        UpgradeTreeBuilder
                .create()
                .addUpgrade(new UpgradeTypes.DamageUpgradeType() {

                    @Override
                    public String getDescription0(String value) {
                        return "+" + value + "% Crit Multiplier";
                    }

                    @Override
                    public void run(float value) {
                        ability.setCritMultiplierIncrease((int) (critMultiplierIncrease + value));
                    }
                }, 15f)
                .addTo(treeA);

        UpgradeTreeBuilder
                .create()
                .addUpgradeDuration(ability)
                .addTo(treeB);

        masterUpgrade = new Upgrade(
                "Dante’s Inferno",
                "Inferno - Master Upgrade",
                "Inferno's cooldown gets reduced by 0.5 seconds and duration gets increased by 0.25 seconds for each critical hit (max 40 hits)",
                50000,
                () -> {

                }
        );
        masterUpgrade2 = new Upgrade(
                "Promethean Gaze",
                "Inferno - Master Upgrade",
                """
                        Damage is increased by 10% while Inferno is active. Critical hits are guaranteed on enemies with less than 30% health.
                        """,
                50000,
                () -> {

                }
        );
    }

}
