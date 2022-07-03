package com.ebicep.warlords.pve.upgrades.avenger;

import com.ebicep.warlords.abilties.Consecrate;
import com.ebicep.warlords.pve.upgrades.AbilityTree;
import com.ebicep.warlords.pve.upgrades.AbstractUpgradeBranch;
import com.ebicep.warlords.pve.upgrades.Upgrade;

public class ConsecrateBranch extends AbstractUpgradeBranch<Consecrate> {

    public ConsecrateBranch(AbilityTree abilityTree, Consecrate ability) {
        super(abilityTree, ability);
        treeA.add(new Upgrade("Damage - Tier I", "+20% Damage", 5000));
        treeA.add(new Upgrade("Damage - Tier II", "+40% Damage", 10000));
        treeA.add(new Upgrade("Damage - Tier III", "+80% Damage", 20000));

        treeB.add(new Upgrade("Range - Tier I", "+1 Block radius", 5000));
        treeB.add(new Upgrade("Range - Tier II", "+2 Block radius", 10000));
        treeB.add(new Upgrade("Range - Tier III", "+3 Block radius", 20000));

        treeC.add(new Upgrade("Cooldown - Tier I", "-10% Cooldown reduction", 5000));
        treeC.add(new Upgrade("Cooldown - Tier II", "-20% Cooldown reduction", 10000));
        treeC.add(new Upgrade("Cooldown - Tier III", "-40% Cooldown reduction", 20000));

        masterUpgrade = new Upgrade(
                "Master Upgrade",
                "Remove energy cost\n\nIncrease the damage dealt by Avenger's\nStrike by 20% when within the Consecrate.",
                50000
        );
    }

    float minDamage = ability.getMinDamageHeal();
    float maxDamage = ability.getMaxDamageHeal();

    @Override
    public void a1() {
        ability.setMinDamageHeal(minDamage * 1.2f);
        ability.setMaxDamageHeal(maxDamage * 1.2f);
    }

    @Override
    public void a2() {
        ability.setMinDamageHeal(minDamage * 1.4f);
        ability.setMaxDamageHeal(maxDamage * 1.4f);
    }

    @Override
    public void a3() {
        ability.setMinDamageHeal(minDamage * 1.8f);
        ability.setMaxDamageHeal(maxDamage * 1.8f);
    }

    @Override
    public void b1() {
        ability.setRadius(6);
    }

    @Override
    public void b2() {
        ability.setRadius(7);
    }

    @Override
    public void b3() {
        ability.setRadius(8);
    }

    float cooldown = ability.getCooldown();

    @Override
    public void c1() {
        ability.setCooldown(cooldown * 0.9f);
    }

    @Override
    public void c2() {
        ability.setCooldown(cooldown * 0.8f);
    }

    @Override
    public void c3() {
        ability.setCooldown(cooldown * 0.6f);
    }

    @Override
    public void master() {
        ability.setEnergyCost(0);
    }
}
