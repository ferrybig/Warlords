package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.player.cooldowns.CooldownTypes;
import com.ebicep.warlords.util.GameRunnable;
import com.ebicep.warlords.util.ParticleEffect;
import com.ebicep.warlords.util.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Berserk extends AbstractAbility {

    private final int duration = 18;
    private final int speedBuff = 30;

    public Berserk() {
        super("Berserk", 0, 0, 46.98f, 30, 0, 0);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7You go into a berserker rage,\n" +
                "§7increasing your damage by §c30% §7and\n" +
                "§7movement speed by §e" + speedBuff + "%§7. While active,\n" +
                "§7you also take §c10% §7more damage.\n" + "§7Lasts §6" + duration + " §7seconds.";
    }

    @Override
    public boolean onActivate(WarlordsPlayer wp, Player player) {
        Berserk tempBerserk = new Berserk();
        wp.subtractEnergy(energyCost);
        wp.getSpeed().addSpeedModifier("Berserk", speedBuff, duration * 20, "BASE");
        wp.getCooldownManager().addRegularCooldown(name, "BERS", Berserk.class, tempBerserk, wp, CooldownTypes.BUFF, cooldownManager -> {
        }, duration * 20);

        Utils.playGlobalSound(player.getLocation(), "warrior.berserk.activation", 2, 1);


        new GameRunnable(wp.getGame()) {
            @Override
            public void run() {
                if (wp.getCooldownManager().hasCooldown(tempBerserk)) {
                    Location location = wp.getLocation();
                    location.add(0, 2.1, 0);
                    ParticleEffect.VILLAGER_ANGRY.display(0, 0, 0, 0.1F, 1, location, 500);
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(0, 3);

        return true;
    }
}
