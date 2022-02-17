package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.player.cooldowns.CooldownTypes;
import com.ebicep.warlords.util.EffectUtils;
import com.ebicep.warlords.util.GameRunnable;
import com.ebicep.warlords.util.ParticleEffect;
import com.ebicep.warlords.util.PlayerFilter;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class RemedicChains extends AbstractAbility {

    private final int linkBreakRadius = 15;
    private final int duration = 8;
    private final int alliesAffected = 3;
    private final float healingMultiplier = 0.125f;

    public RemedicChains() {
        super("Remedic Chains", 643, 770, 16, 40, 20, 200);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Bind yourself to §e" + alliesAffected + " §7allies near you, causing them\n" +
                "§7them to regenerate §a3% §7max health (even when\n" +
                "§7taking damage) as long as the link is active.\n" +
                "Lasts §6" + duration + " §7seconds" +
                "\n\n" +
                "§7When the link expires you and the allies\n" +
                "§7are healed for §a" + format(minDamageHeal) + " §7- §a" + format(maxDamageHeal) + " §7health. Breaking\n" +
                "§7the link early will only heal the allies\n" +
                "§7for §a12.5% §7of the original amount for\n" +
                "each second they have been linked." +
                "\n\n" +
                "§7The link will break if you are §e" + linkBreakRadius + " §7blocks apart.";
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsPlayer wp, @Nonnull Player player) {
        RemedicChains tempRemedicChain = new RemedicChains();

        int targetHit = 0;
        for (WarlordsPlayer chainTarget : PlayerFilter
                .entitiesAround(player, 10, 10, 10)
                .aliveTeammatesOfExcludingSelf(wp)
                .closestFirst(wp)
                .limit(alliesAffected)
        ) {
            chainTarget.getCooldownManager().addRegularCooldown(
                    "Remedic Chains",
                    "REMEDIC",
                    RemedicChains.class,
                    tempRemedicChain,
                    wp,
                    CooldownTypes.ABILITY,
                    cooldownManager -> {},
                    duration * 20
            );

            player.sendMessage(
                    WarlordsPlayer.RECEIVE_ARROW +
                    ChatColor.GRAY + " Your Remedic Chains is now protecting " +
                    ChatColor.YELLOW + chainTarget.getName() +
                    ChatColor.GRAY + "!"
            );

            HashMap<WarlordsPlayer, Integer> timeLinked = new HashMap<>();

            targetHit++;
            new GameRunnable(wp.getGame()) {
                int counter = 0;
                @Override
                public void run() {
                    boolean outOfRange = wp.getLocation().distanceSquared(chainTarget.getLocation()) > linkBreakRadius * linkBreakRadius;

                    if (counter % 20 == 0 && !outOfRange) {
                        timeLinked.compute(chainTarget, (k, v) -> v == null ? 1 : v + 1);

                        float maxHealing = chainTarget.getMaxHealth() * 0.03f;
                        chainTarget.addHealingInstance(
                                wp,
                                name,
                                maxHealing,
                                maxHealing,
                                -1,
                                100,
                                false,
                                false
                        );

                        wp.addHealingInstance(
                                wp,
                                name,
                                maxHealing,
                                maxHealing,
                                -1,
                                100,
                                false,
                                false
                        );
                    }

                    if (counter % 8 == 0) {
                        if (wp.getCooldownManager().hasCooldown(tempRemedicChain)) {
                            EffectUtils.playParticleLinkAnimation(wp.getLocation(), chainTarget.getLocation(), 250, 200, 250);
                            // Ally is out of range, break link
                            if (outOfRange) {
                                for (Map.Entry<WarlordsPlayer, Integer> entry : timeLinked.entrySet()) {
                                    float totalHealingMultiplier = (healingMultiplier * entry.getValue());
                                    entry.getKey().addHealingInstance(
                                            wp,
                                            name,
                                            minDamageHeal * totalHealingMultiplier,
                                            maxDamageHeal * totalHealingMultiplier,
                                            -1,
                                            100,
                                            false,
                                            false
                                    );
                                }

                                chainTarget.getCooldownManager().removeCooldown(tempRemedicChain);
                                chainTarget.sendMessage(ChatColor.RED + "You left the link range early!");
                                this.cancel();
                            }

                            if (chainTarget.isDead()) {
                                this.cancel();
                            }
                        } else {
                            // Ally was in range, full healing
                            if (!outOfRange && chainTarget.isAlive()) {
                                chainTarget.addHealingInstance(
                                        wp,
                                        name,
                                        minDamageHeal,
                                        maxDamageHeal,
                                        critChance,
                                        critMultiplier,
                                        false,
                                        false
                                );
                            }

                            ParticleEffect.VILLAGER_HAPPY.display(0.5f, 0.5f, 0.5f, 1, 10, chainTarget.getLocation().add(0, 1, 0), 500);

                            for (Player player1 : wp.getWorld().getPlayers()) {
                                player1.playSound(chainTarget.getLocation(), "rogue.remedicchains.impact", 0.05f, 1.4f);
                            }
                            this.cancel();
                        }
                    }

                    counter++;
                }
            }.runTaskTimer(0, 0);
        }

        if (targetHit >= 1) {
            for (Player player1 : player.getWorld().getPlayers()) {
                player1.playSound(player.getLocation(), "rogue.remedicchains.activation", 2, 0.3f);
                player1.playSound(player.getLocation(), Sound.BLAZE_BREATH, 2, 0.3f);
            }

            wp.subtractEnergy(energyCost);
            wp.getCooldownManager().addRegularCooldown(
                    "Remedic Chains",
                    "REMEDIC",
                    RemedicChains.class,
                    tempRemedicChain,
                    wp,
                    CooldownTypes.ABILITY,
                    cooldownManager -> wp.addHealingInstance(wp, name, minDamageHeal, maxDamageHeal, critChance, critMultiplier, false, false),
                    duration * 20
            );
        }

        return targetHit >= 1;
    }

}
