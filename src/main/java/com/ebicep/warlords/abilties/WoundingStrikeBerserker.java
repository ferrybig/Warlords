package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractStrikeBase;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingFinalEvent;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownFilter;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.util.java.Pair;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WoundingStrikeBerserker extends AbstractStrikeBase {

    private int woundingDuration = 3;

    public WoundingStrikeBerserker() {
        super("Wounding Strike", 497, 632, 0, 100, 20, 175);
    }

    @Override
    public void updateDescription(Player player) {
        description = "Strike the targeted enemy player, causing" + formatRangeDamage(minDamageHeal, maxDamageHeal) +
                "damage and §cwounding §7them for §6" + woundingDuration +
                " §7seconds. A wounded player receives §c40% §7less healing for the duration of the effect.";
    }

    @Override
    public List<Pair<String, String>> getAbilityInfo() {
        List<Pair<String, String>> info = new ArrayList<>();
        info.add(new Pair<>("Players Struck", "" + timesUsed));

        return info;
    }

    @Override
    protected void playSoundAndEffect(Location location) {
        Utils.playGlobalSound(location, "warrior.mortalstrike.impact", 2, 1);
        randomHitEffect(location, 7, 255, 0, 0);
    }

    @Override
    protected boolean onHit(@Nonnull WarlordsEntity wp, @Nonnull Player player, @Nonnull WarlordsEntity nearPlayer) {
        boolean lustDamageBoost = pveUpgrade && wp.getCooldownManager().hasCooldown(BloodLust.class);
        Optional<WarlordsDamageHealingFinalEvent> finalEvent = nearPlayer.addDamageInstance(
                wp,
                name,
                minDamageHeal * (lustDamageBoost ? 1.5f : 1),
                maxDamageHeal * (lustDamageBoost ? 1.5f : 1),
                critChance,
                critMultiplier,
                false
        );
//        if(finalEvent.isPresent()) {
        if (pveUpgrade) {
            bleedOnHit(wp, nearPlayer);
        } else {
            if (!(nearPlayer.getCooldownManager().hasCooldown(WoundingStrikeBerserker.class) || nearPlayer.getCooldownManager()
                    .hasCooldown(WoundingStrikeDefender.class))) {
                nearPlayer.sendMessage(ChatColor.GRAY + "You are " + ChatColor.RED + "wounded" + ChatColor.GRAY + ".");
            }
            nearPlayer.getCooldownManager().removeCooldown(WoundingStrikeBerserker.class);
            nearPlayer.getCooldownManager().removeCooldown(WoundingStrikeDefender.class);
            nearPlayer.getCooldownManager().addCooldown(new RegularCooldown<WoundingStrikeBerserker>(
                    name,
                    "WND",
                    WoundingStrikeBerserker.class,
                    new WoundingStrikeBerserker(),
                    wp,
                    CooldownTypes.DEBUFF,
                    cooldownManager -> {
                        if (new CooldownFilter<>(cooldownManager, RegularCooldown.class).filterNameActionBar("WND").stream().count() == 1) {
                            nearPlayer.sendMessage(ChatColor.GRAY + "You are no longer " + ChatColor.RED + "wounded" + ChatColor.GRAY + ".");
                        }
                    },
                    woundingDuration * 20
            ) {
                @Override
                public float doBeforeHealFromSelf(WarlordsDamageHealingEvent event, float currentHealValue) {
                    return currentHealValue * .6f;
                }
            });
        }
//            return true;
//        } else {
//            return false;
//        }
        return true;
    }

    private void bleedOnHit(WarlordsEntity giver, WarlordsEntity hit) {
        hit.getCooldownManager().removeCooldown(WoundingStrikeBerserker.class);
        hit.getCooldownManager().addCooldown(new RegularCooldown<WoundingStrikeBerserker>(
                "Bleed",
                "BLEED",
                WoundingStrikeBerserker.class,
                new WoundingStrikeBerserker(),
                giver,
                CooldownTypes.DEBUFF,
                cooldownManager -> {
                },
                woundingDuration * 20,
                Collections.singletonList((cooldown, ticksLeft, ticksElapsed) -> {
                    if (ticksLeft % 20 == 0) {
                        float healthDamage = hit.getMaxHealth() * 0.0025f;
                        hit.addDamageInstance(
                                giver,
                                "Bleed",
                                healthDamage,
                                healthDamage,
                                -1,
                                100,
                                false
                        );
                    }
                })
        ) {
            @Override
            public float doBeforeHealFromSelf(WarlordsDamageHealingEvent event, float currentHealValue) {
                return currentHealValue * .3f;
            }
        });
    }

    public int getWoundingDuration() {
        return woundingDuration;
    }

    public void setWoundingDuration(int woundingDuration) {
        this.woundingDuration = woundingDuration;
    }


}