package com.ebicep.warlords.pve.upgrades.shaman.thunderlord;

import com.ebicep.warlords.abilities.ChainLightning;
import com.ebicep.warlords.pve.upgrades.*;

public class ChainLightningBranch extends AbstractUpgradeBranch<ChainLightning> {

    float minDamage = ability.getMinDamageHeal();
    float maxDamage = ability.getMaxDamageHeal();
    int radius = ability.getRadius();
    int bounceRange = ability.getBounceRange();

    public ChainLightningBranch(AbilityTree abilityTree, ChainLightning ability) {
        super(abilityTree, ability);

        UpgradeTreeBuilder
                .create()
                .addUpgrade(new UpgradeTypes.DamageUpgradeType() {
                    @Override
                    public void run(float value) {
                        float v = 1 + value / 100;
                        ability.setMinDamageHeal(minDamage * v);
                        ability.setMaxDamageHeal(maxDamage * v);
                    }
                }, 7.5f)
                .addUpgrade(new UpgradeTypes.UpgradeType() {
                    @Override
                    public String getDescription0(String value) {
                        return "+" + value + " Blocks Cast and Bounce Range";
                    }

                    @Override
                    public void run(float value) {
                        ability.setRadius((int) (radius + value));
                        ability.setBounceRange((int) (bounceRange + value));
                    }
                }, 2f)
                .addTo(treeA);

        UpgradeTreeBuilder
                .create()
                .addUpgradeEnergy(ability, 2.5f)
                .addUpgrade(new UpgradeTypes.UpgradeType() {
                    @Override
                    public String getDescription0(String value) {
                        return "+" + value + " Chain Bounce";
                    }

                    @Override
                    public void run(float value) {
                        ability.setAdditionalBounces((int) (ability.getAdditionalBounces() + value));
                    }
                }, 1f, 4)
                .addTo(treeB);

        masterUpgrade = new Upgrade(
                "Electrifying Chains",
                "Chain Lightning - Master Upgrade",
                "Increase max damage reduction cap by 10%. Additionally, Chain Lightning now deals 10% more damage per bounce instead of less.",
                50000,
                () -> {
                    ability.setMaxDamageReduction(ability.getMaxDamageReduction() + 10);
                }
        );
        masterUpgrade2 = new Upgrade(
                "Aftershock",
                "Chain Lightning - Master Upgrade",
                """
                        Chain Lightning will now give enemies hit the SHOCKED status for 3s.
                                                
                        SHOCKED: Enemies that are shocked have their movement speed slowed by 25% and their incoming healing reduced by 50%.
                        """,
                50000,
                () -> {
                }
        );
    }

}
