package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.effects.ParticleEffect;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.util.bukkit.Matrix4d;
import com.ebicep.warlords.util.java.Pair;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class HeartToHeart extends AbstractAbility {

    public int timesUsedWithFlag = 0;

    private int radius = 15;
    private int verticalRadius = 15;
    private int vindDuration = 6;
    private float healthRestore = 600;

    public HeartToHeart() {
        super("Heart To Heart", 0, 0, 12, 20, -1, 100);
    }

    @Override
    public void updateDescription(Player player) {
        description = "Throw a chain towards an ally in a §e15 §7block radius, grappling the Vindicator towards the ally. You and the targeted ally gain " +
                "§6VIND §7for §6" + vindDuration + " §7seconds, granting immunity to de-buffs. You are healed for §a" + format(healthRestore) +
                " §7health after reaching your ally.\nHeart To Heart's range is greatly reduced when holding a flag.";
    }

    @Override
    public List<Pair<String, String>> getAbilityInfo() {
        List<Pair<String, String>> info = new ArrayList<>();
        info.add(new Pair<>("Times Used", "" + timesUsed));
        info.add(new Pair<>("Times Used With Flag", "" + timesUsedWithFlag));

        return info;
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsEntity wp, @Nonnull Player player) {
        if (wp.hasFlag()) {
            radius = 10;
            verticalRadius = 2;
        } else {
            wp.setFlagPickCooldown(2);
            radius = getRadius();
            verticalRadius = getVerticalRadius();
        }

        if (wp.isInPve()) {
            for (WarlordsEntity heartTarget : PlayerFilter
                    .entitiesAround(wp, radius, verticalRadius, radius)
                    .requireLineOfSight(wp)
                    .lookingAtFirst(wp)
            ) {
                activateAbility(wp, heartTarget);
                return true;
            }
        } else {
            for (WarlordsEntity heartTarget : PlayerFilter
                    .entitiesAround(wp, radius, verticalRadius, radius)
                    .aliveTeammatesOfExcludingSelf(wp)
                    .requireLineOfSight(wp)
                    .lookingAtFirst(wp)
                    .limit(1)
            ) {
                activateAbility(wp, heartTarget);
                return true;
            }
        }

        return false;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getVerticalRadius() {
        return verticalRadius;
    }

    private void activateAbility(WarlordsEntity wp, WarlordsEntity heartTarget) {
        if (wp.hasFlag()) {
            timesUsedWithFlag++;
        }
        wp.subtractEnergy(energyCost, false);
        Utils.playGlobalSound(wp.getLocation(), "rogue.hearttoheart.activation", 2, 1);
        Utils.playGlobalSound(wp.getLocation(), "rogue.hearttoheart.activation.alt", 2, 1.2f);

        HeartToHeart tempHeartToHeart = new HeartToHeart();
        Vindicate.giveVindicateCooldown(wp, wp, HeartToHeart.class, tempHeartToHeart, vindDuration * 20);
        Vindicate.giveVindicateCooldown(wp, heartTarget, HeartToHeart.class, tempHeartToHeart, vindDuration * 20);

        List<WarlordsEntity> playersHit = new ArrayList<>();
        new GameRunnable(wp.getGame()) {
            final Location playerLoc = wp.getLocation();
            int timer = 0;

            @Override
            public void run() {
                timer++;

                if (timer >= 8 || (heartTarget.isDead() || wp.isDead())) {
                    this.cancel();
                }

                double target = timer / 8D;
                Location targetLoc = heartTarget.getLocation();
                Location newLocation = new Location(
                        playerLoc.getWorld(),
                        Utils.lerp(playerLoc.getX(), targetLoc.getX(), target),
                        Utils.lerp(playerLoc.getY(), targetLoc.getY(), target),
                        Utils.lerp(playerLoc.getZ(), targetLoc.getZ(), target),
                        targetLoc.getYaw(),
                        targetLoc.getPitch()
                );

                EffectUtils.playChainAnimation(wp, heartTarget, new ItemStack(Material.LEAVES, 1, (short) 1), timer);

                wp.teleportLocationOnly(newLocation);
                wp.setFallDistance(-5);
                newLocation.add(0, 1, 0);
                Matrix4d center = new Matrix4d(newLocation);
                for (float i = 0; i < 6; i++) {
                    double angle = Math.toRadians(i * 90) + timer * 0.6;
                    double width = 1.5D;
                    ParticleEffect.SPELL_WITCH.display(0, 0, 0, 0, 1,
                            center.translateVector(playerLoc.getWorld(), 0, Math.sin(angle) * width, Math.cos(angle) * width), 500
                    );
                }

                if (pveUpgrade) {
                    for (WarlordsEntity we : PlayerFilter
                            .entitiesAround(wp, 3, 3, 3)
                            .aliveEnemiesOf(wp)
                            .excluding(playersHit)
                    ) {
                        playersHit.add(we);
                        we.getSpeed().addSpeedModifier("Heart Slowness", -99, GameRunnable.SECOND, "BASE");
                        we.addDamageInstance(
                                wp,
                                name,
                                904,
                                1377,
                                -1,
                                100,
                                false
                        );
                    }
                }

                if (timer >= 8) {
                    wp.setVelocity(playerLoc.getDirection().multiply(0.4).setY(0.2), false);
                    wp.addHealingInstance(
                            wp,
                            name,
                            healthRestore,
                            healthRestore,
                            -1,
                            100,
                            false,
                            false
                    );
                }
            }
        }.runTaskTimer(0, 1);
    }

    public void setVerticalRadius(int verticalRadius) {
        this.verticalRadius = verticalRadius;
    }

    public void setVindDuration(int vindDuration) {
        this.vindDuration = vindDuration;
    }

    public float getHealthRestore() {
        return healthRestore;
    }

    public void setHealthRestore(float healthRestore) {
        this.healthRestore = healthRestore;
    }


}
