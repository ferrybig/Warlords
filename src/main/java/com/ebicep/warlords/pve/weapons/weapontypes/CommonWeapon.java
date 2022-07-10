package com.ebicep.warlords.pve.weapons.weapontypes;

import com.ebicep.warlords.pve.weapons.AbstractWeapon;
import com.ebicep.warlords.util.java.Utils;
import org.bukkit.ChatColor;
import org.springframework.data.annotation.Transient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.ebicep.warlords.pve.weapons.weapontypes.WeaponScore.getAverageValue;

public class CommonWeapon extends AbstractWeapon implements Salvageable, WeaponScore {

    @Transient
    public static final int MELEE_DAMAGE_MIN = 80;
    @Transient
    public static final int MELEE_DAMAGE_MAX = 120;
    @Transient
    public static final int CRIT_CHANCE_MIN = 8;
    @Transient
    public static final int CRIT_CHANCE_MAX = 12;
    @Transient
    public static final int CRIT_MULTIPLIER_MIN = 125;
    @Transient
    public static final int CRIT_MULTIPLIER_MAX = 150;
    @Transient
    public static final int HEALTH_BONUS_MIN = 50;
    @Transient
    public static final int HEALTH_BONUS_MAX = 200;

    public CommonWeapon() {
    }

    public CommonWeapon(UUID uuid) {
        super(uuid);
    }

    @Override
    public ChatColor getChatColor() {
        return ChatColor.GREEN;
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList(
                "",
                getWeaponScoreString()
        );
    }

    @Override
    public void generateStats() {
        this.meleeDamage = Utils.generateRandomValueBetweenInclusive(MELEE_DAMAGE_MIN, MELEE_DAMAGE_MAX);
        this.critChance = Utils.generateRandomValueBetweenInclusive(CRIT_CHANCE_MIN, CRIT_CHANCE_MAX);
        this.critMultiplier = Utils.generateRandomValueBetweenInclusive(CRIT_MULTIPLIER_MIN, CRIT_MULTIPLIER_MAX);
        this.healthBonus = Utils.generateRandomValueBetweenInclusive(HEALTH_BONUS_MIN, HEALTH_BONUS_MAX);
    }

    @Override
    public List<Double> getWeaponScoreAverageValues() {
        return Arrays.asList(
                getAverageValue(MELEE_DAMAGE_MIN, MELEE_DAMAGE_MAX, meleeDamage),
                getAverageValue(CRIT_CHANCE_MIN, CRIT_CHANCE_MAX, critChance),
                getAverageValue(CRIT_MULTIPLIER_MIN, CRIT_MULTIPLIER_MAX, critMultiplier),
                getAverageValue(HEALTH_BONUS_MIN, HEALTH_BONUS_MAX, healthBonus)
        );
    }

    @Override
    public int getMinSalvageAmount() {
        return 1;
    }

    @Override
    public int getMaxSalvageAmount() {
        return 2;
    }
}