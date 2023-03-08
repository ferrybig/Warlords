package com.ebicep.warlords.pve.weapons.weapontypes.legendaries.titles;

import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.PermanentCooldown;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.pve.weapons.weapontypes.legendaries.AbstractLegendaryWeapon;
import com.ebicep.warlords.pve.weapons.weapontypes.legendaries.LegendaryTitles;
import com.ebicep.warlords.util.java.Pair;
import org.bukkit.ChatColor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class LegendaryStalwart extends AbstractLegendaryWeapon {

    public static final int UNDER_HP_CHECK = 80;
    public static final int UNDER_HP_CHECK_INCREASE_PER_UPGRADE = 5;
    public static final int EVERY_HP_PERCENT = 10;
    public static final float EVERY_HP_PERCENT_DECREASE_PER_UPGRADE = .5f;

    public static final int REDUCTION_DURATION = 5;
    public static final int COOLDOWN = 30;

    public LegendaryStalwart() {
    }

    public LegendaryStalwart(UUID uuid) {
        super(uuid);
    }

    public LegendaryStalwart(AbstractLegendaryWeapon legendaryWeapon) {
        super(legendaryWeapon);
    }

    @Override
    public String getPassiveEffect() {
        return "For every " + formatTitleUpgrade(getEveryHpPercent(), "%") +
                " of HP under " + formatTitleUpgrade(getUnderHpCheck(), "%") +
                ", gain an additional 7.5% damage reduction. Maximum 80% Damage Reduction." +
                "\n\nIf your HP is currently higher than 80% and you will die from the next source of damage, your " +
                "health will be set to 5% of your max HP and gain 99% damage reduction for 5 seconds. Can be triggered every 30 seconds.";
    }

    @Override
    public List<Pair<String, String>> getPassiveEffectUpgrade() {
        return Arrays.asList(
                new Pair<>(
                        formatTitleUpgrade(EVERY_HP_PERCENT - EVERY_HP_PERCENT_DECREASE_PER_UPGRADE * getTitleLevel(), "%"),
                        formatTitleUpgrade(EVERY_HP_PERCENT - EVERY_HP_PERCENT_DECREASE_PER_UPGRADE * getTitleLevelUpgraded(), "%")
                ),
                new Pair<>(
                        formatTitleUpgrade(UNDER_HP_CHECK + UNDER_HP_CHECK_INCREASE_PER_UPGRADE * getTitleLevel(), "%"),
                        formatTitleUpgrade(UNDER_HP_CHECK + UNDER_HP_CHECK_INCREASE_PER_UPGRADE * getTitleLevelUpgraded(), "%")
                )
        );
    }

    @Override
    protected float getMeleeDamageMaxValue() {
        return 160;
    }

    @Override
    public void applyToWarlordsPlayer(WarlordsPlayer player) {
        super.applyToWarlordsPlayer(player);

        // 80 - 10 = skip +70% hp = .7
        // 85 - 9.5 = skip +75.5% hp = .75.5
        float upperBoundHP = (getUnderHpCheck() - getEveryHpPercent()) / 100;
        AtomicReference<Instant> lastActivated = new AtomicReference<>(Instant.now().minus(COOLDOWN, ChronoUnit.SECONDS));

        player.getCooldownManager().addCooldown(
                new PermanentCooldown<>(
                        "Stalwart",
                        null,
                        LegendaryStalwart.class,
                        null,
                        player,
                        CooldownTypes.WEAPON,
                        cooldownManager -> {

                        },
                        false
                ) {
                    @Override
                    public float modifyDamageAfterInterveneFromSelf(WarlordsDamageHealingEvent event, float currentDamageValue) {
                        if (player.getHealth() >= player.getMaxHealth() * upperBoundHP) {
                            return currentDamageValue;
                        }
                        float currentHpPercent = player.getHealth() / player.getMaxHealth();
                        int timesToReduce = (int) ((getUnderHpCheck() - currentHpPercent) / getEveryHpPercent());
                        float reduction = Math.min(timesToReduce * .075f, .8f);
                        return currentDamageValue * (1 - reduction);
                    }

                    @Override
                    public float modifyDamageAfterAllFromSelf(WarlordsDamageHealingEvent event, float currentDamageValue, boolean isCrit) {
                        if (player.getHealth() <= player.getMaxHealth() * .8) {
                            return currentDamageValue;
                        }
                        if (player.getHealth() - currentDamageValue > 0) {
                            return currentDamageValue;
                        }
                        if (Instant.now().isBefore(lastActivated.get())) {
                            return currentDamageValue;
                        }
                        lastActivated.set(Instant.now().plus(COOLDOWN, ChronoUnit.SECONDS));
                        player.setHealth(player.getMaxBaseHealth() * .05f);
                        player.getCooldownManager().addCooldown(new RegularCooldown<>(
                                "Stalwart",
                                "STALWART",
                                LegendaryDivine.class,
                                null,
                                player,
                                CooldownTypes.WEAPON,
                                cooldownManager -> {
                                },
                                REDUCTION_DURATION * 20
                        ) {
                            @Override
                            public float modifyDamageAfterInterveneFromSelf(WarlordsDamageHealingEvent event, float currentDamageValue) {
                                return currentDamageValue * .01f;
                            }
                        });
                        player.sendMessage(ChatColor.GREEN + "Triggered Stalwart! +99% damage reduction for 5s.");
                        return 0;
                    }
                }
        );

    }

    @Override
    public LegendaryTitles getTitle() {
        return LegendaryTitles.STALWART;
    }

    @Override
    protected float getMeleeDamageMinValue() {
        return 140;
    }

    @Override
    protected float getCritChanceValue() {
        return 20;
    }

    @Override
    protected float getCritMultiplierValue() {
        return 160;
    }

    @Override
    protected float getHealthBonusValue() {
        return 1000;
    }

    @Override
    protected float getSpeedBonusValue() {
        return 7;
    }

    private float getEveryHpPercent() {
        return EVERY_HP_PERCENT - EVERY_HP_PERCENT_DECREASE_PER_UPGRADE * getTitleLevel();
    }

    private int getUnderHpCheck() {
        return UNDER_HP_CHECK + UNDER_HP_CHECK_INCREASE_PER_UPGRADE * getTitleLevel();
    }
}
