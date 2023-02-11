package com.ebicep.warlords.pve.weapons.weapontypes.legendaries.titles;

import com.ebicep.warlords.events.player.ingame.WarlordsAddCooldownEvent;
import com.ebicep.warlords.events.player.ingame.WarlordsAddSpeedModifierEvent;
import com.ebicep.warlords.events.player.ingame.WarlordsBlueAbilityTargetEvent;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.WarlordsNPC;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.player.ingame.cooldowns.AbstractCooldown;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.pve.weapons.weapontypes.legendaries.AbstractLegendaryWeapon;
import com.ebicep.warlords.pve.weapons.weapontypes.legendaries.LegendaryTitles;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class LegendaryEnhanced extends AbstractLegendaryWeapon {

    private static final int TICKS_TO_ADD = 40;
    private static final int TICKS_TO_ADD_PER_UPGRADE = 10;
    private static final List<String> EFFECTED_ABILITIES = new ArrayList<>() {{
        add("BRN");
        add("WND");
        add("BLEED");
        add("CRIP");
        add("SILENCE");
        add("LCH");
        add("AVE MARK");
    }};

    public LegendaryEnhanced() {
    }

    public LegendaryEnhanced(UUID uuid) {
        super(uuid);
    }

    public LegendaryEnhanced(AbstractLegendaryWeapon legendaryWeapon) {
        super(legendaryWeapon);
    }

    @Override
    public String getPassiveEffect() {
        String effectDuration = formatTitleUpgrade((TICKS_TO_ADD + TICKS_TO_ADD_PER_UPGRADE * getTitleLevel()) / 20f, "s");
        return "Increase the duration of negative effects to enemies by " + effectDuration + " and active abilities of allies by " + effectDuration + " whenever you target an ally with a blue rune (Slot 4).";
    }

    @Override
    protected float getMeleeDamageMaxValue() {
        return 180;
    }

    @Override
    public void applyToWarlordsPlayer(WarlordsPlayer player) {
        super.applyToWarlordsPlayer(player);

        player.getGame().registerEvents(new Listener() {

            @EventHandler
            public void onCooldownAdd(WarlordsAddCooldownEvent event) {
                WarlordsEntity eventPlayer = event.getPlayer();
                if (!(eventPlayer instanceof WarlordsNPC)) {
                    return;
                }
                if (eventPlayer.isTeammate(player)) {
                    return;
                }
                AbstractCooldown<?> cooldown = event.getAbstractCooldown();
                if (!Objects.equals(cooldown.getFrom(), player)) {
                    return;
                }
                if (!(cooldown instanceof RegularCooldown)) {
                    return;
                }
                RegularCooldown<?> regularCooldown = (RegularCooldown<?>) cooldown;
                if (regularCooldown.isEnhanced()) {
                    return;
                }
                if (EFFECTED_ABILITIES.contains(cooldown.getNameAbbreviation())) {
                    regularCooldown.setEnhanced(true);
                    regularCooldown.setTicksLeft(regularCooldown.getTicksLeft() + TICKS_TO_ADD + TICKS_TO_ADD_PER_UPGRADE * getTitleLevel());
                }
            }

            @EventHandler
            public void onSpeedModify(WarlordsAddSpeedModifierEvent event) {
                WarlordsEntity eventPlayer = event.getPlayer();
                if (!(eventPlayer instanceof WarlordsNPC)) {
                    return;
                }
                if (!event.getFrom().equals(player)) {
                    return;
                }
                if (eventPlayer.isTeammate(player)) {
                    return;
                }
                if (event.getModifier().get() > 0) {
                    return;
                }
                if (event.isEnhanced()) {
                    return;
                }
                event.setEnhanced(true);
                event.getDuration().set(event.getDuration().get() + TICKS_TO_ADD + TICKS_TO_ADD_PER_UPGRADE * getTitleLevel());
            }

            @EventHandler
            public void onBlueAbilityTarget(WarlordsBlueAbilityTargetEvent event) {
                if (!event.getPlayer().equals(player)) {
                    return;
                }
                HashSet<WarlordsEntity> warlordsEntities = new HashSet<>(event.getTargets());
                warlordsEntities.add(player);
                warlordsEntities.stream()
                                .filter(warlordsEntity -> warlordsEntity.isTeammate(player))
                                .forEach(warlordsEntity -> {
                                    List<AbstractCooldown<?>> abstractCooldowns = warlordsEntity.getCooldownManager().getCooldowns();
                                    abstractCooldowns.stream()
                                                     .filter(abstractCooldown -> abstractCooldown.getCooldownType() == CooldownTypes.ABILITY)
                                                     .filter(RegularCooldown.class::isInstance)
                                                     .map(RegularCooldown.class::cast)
                                                     .filter(regularCooldown -> !regularCooldown.isEnhanced())
                                                     .forEachOrdered(regularCooldown -> {
                                                         if (regularCooldown.getTicksLeft() <= 0) {
                                                             return;
                                                         }
                                                         regularCooldown.setEnhanced(true);
                                                         regularCooldown.setTicksLeft(regularCooldown.getTicksLeft() + TICKS_TO_ADD + TICKS_TO_ADD_PER_UPGRADE * getTitleLevel());
                                                     });
                                });
            }

        });
    }

    @Override
    public LegendaryTitles getTitle() {
        return LegendaryTitles.ENHANCED;
    }

    @Override
    protected float getMeleeDamageMinValue() {
        return 155;
    }

    @Override
    protected float getCritChanceValue() {
        return 20;
    }

    @Override
    protected float getCritMultiplierValue() {
        return 180;
    }

    @Override
    protected float getHealthBonusValue() {
        return 400;
    }

    @Override
    protected float getSpeedBonusValue() {
        return 5;
    }

    @Override
    protected float getEnergyPerSecondBonusValue() {
        return 3;
    }
}
