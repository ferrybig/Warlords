package com.ebicep.warlords.abilities;

import com.ebicep.warlords.abilities.internal.AbstractBeam;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownFilter;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.pve.upgrades.AbilityTree;
import com.ebicep.warlords.pve.upgrades.AbstractUpgradeBranch;
import com.ebicep.warlords.pve.upgrades.arcanist.conjurer.SoulfireBeamBranch;
import com.ebicep.warlords.util.java.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class SoulfireBeam extends AbstractBeam {

    private int speedBuff = 40;
    private int speedTickDuration = 60;

    public SoulfireBeam() {
        super("Soulfire Beam", 376, 508, 10, 10, 20, 175, 30, 30, false);
        this.maxTicks = 0;
    }

    @Override
    public void updateDescription(Player player) {
        description = Component.text("Unleash a concentrated beam of demonic power, dealing ")
                               .append(formatRangeDamage(minDamageHeal, maxDamageHeal))
                               .append(Component.text(" damage to all enemies hit. " +
                                       " If the target is affected by the max stacks of Poisonous Hex, remove all stacks, increase the damage dealt of " + name + " by "))
                               .append(Component.text("100%", NamedTextColor.RED))
                               .append(Component.text(". Gain"))
                               .append(Component.text(speedBuff + "%", NamedTextColor.YELLOW))
                               .append(Component.text(" speed for "))
                               .append(Component.text(format(speedTickDuration / 20f), NamedTextColor.GOLD))
                               .append(Component.text(" seconds.\n\nHas a maximum range of "))
                               .append(Component.text(format(maxDistance), NamedTextColor.YELLOW))
                               .append(Component.text(" blocks."));
    }

    @Override
    public List<Pair<String, String>> getAbilityInfo() {
        return null;
    }

    @Override
    protected void playEffect(@Nonnull Location currentLocation, int ticksLived) {

    }

    @Override
    protected void onNonCancellingHit(@Nonnull InternalProjectile projectile, @Nonnull WarlordsEntity hit, @Nonnull Location impactLocation) {
        WarlordsEntity wp = projectile.getShooter();
        if (!projectile.getHit().contains(hit)) {
            getProjectiles(projectile).forEach(p -> p.getHit().add(hit));
            float minDamage = minDamageHeal;
            float maxDamage = maxDamageHeal;
            int hexStacks = (int) new CooldownFilter<>(hit, RegularCooldown.class)
                    .filterCooldownClass(PoisonousHex.class)
                    .stream()
                    .count();
            boolean hasAstral = wp.getCooldownManager().hasCooldown(AstralPlague.class);
            if (hexStacks >= PoisonousHex.getFromHex(wp).getMaxStacks()) {
                if (!hasAstral) {
                    hit.getCooldownManager().removeCooldown(PoisonousHex.class, false);
                }
                minDamage *= 2;
                maxDamage *= 2;
            }
            hit.addDamageInstance(wp, name, minDamage, maxDamage, critChance, critMultiplier);
        }
    }

    @Override
    public AbstractUpgradeBranch<?> getUpgradeBranch(AbilityTree abilityTree) {
        return new SoulfireBeamBranch(abilityTree, this);
    }

    @Override
    public ItemStack getBeamItem() {
        return new ItemStack(Material.CRIMSON_FENCE_GATE);
    }

    @Nullable
    @Override
    protected String getActivationSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 0;
    }

    @Override
    protected float getSoundPitch() {
        return 0;
    }
}
