package com.ebicep.warlords.pve.mobs.events.gardenofhesperides;

import com.ebicep.customentities.nms.pve.pathfindergoals.PredictTargetFutureLocationGoal;
import com.ebicep.warlords.abilities.Fireball;
import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.game.option.pve.PveOption;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.pve.mobs.Mob;
import com.ebicep.warlords.pve.mobs.abilities.AbstractPveAbility;
import com.ebicep.warlords.pve.mobs.tiers.BossMinionMob;
import com.ebicep.warlords.pve.mobs.zombie.AbstractZombie;
import com.ebicep.warlords.util.bukkit.LocationUtils;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.Location;
import org.bukkit.Particle;

import javax.annotation.Nonnull;
import java.util.List;

public class EventPrometheus extends AbstractZombie implements BossMinionMob {

    private int barrageOfFlamesDelay = 0;
    private boolean halfHealthSpawned = false;

    public EventPrometheus(Location spawnLocation) {
        this(spawnLocation, "Prometheus", 30000, 0, 10, 730, 870);
    }

    public EventPrometheus(
            Location spawnLocation,
            String name,
            int maxHealth,
            float walkSpeed,
            int damageResistance,
            float minMeleeDamage,
            float maxMeleeDamage
    ) {
        super(
                spawnLocation,
                name,
                maxHealth,
                walkSpeed,
                damageResistance,
                minMeleeDamage,
                maxMeleeDamage,
                new BurstOfFlames(),
                new Fireball(350, 450, 1000)
        );
    }

    @Override
    public Mob getMobRegistry() {
        return Mob.EVENT_PROMETHEUS;
    }

    @Override
    public void onSpawn(PveOption option) {
        super.onSpawn(option);
        List<Location> spawnLocations = LocationUtils.getCircle(warlordsNPC.getLocation(), 3, option.playerCount() + 1);
        for (Location location : spawnLocations) {
            option.spawnNewMob(Mob.ILLUMINATION.createMob(location));
        }
    }

    @Override
    public void whileAlive(int ticksElapsed, PveOption option) {
        if (ticksElapsed % 20 == 0) {
            boolean anyPlayer20BlockAway = PlayerFilter
                    .playingGame(option.getGame())
                    .aliveEnemiesOf(warlordsNPC)
                    .stream()
                    .anyMatch(warlordsEntity -> warlordsEntity.getLocation().distanceSquared(warlordsNPC.getLocation()) > 400);
            if (anyPlayer20BlockAway && barrageOfFlamesDelay == 0) {
                Location loc = warlordsNPC.getLocation();

                Utils.playGlobalSound(loc, "mage.inferno.activation", 500, 0.5f);
                Utils.playGlobalSound(loc, "mage.inferno.activation", 500, 0.5f);
                new GameRunnable(warlordsNPC.getGame()) {
                    @Override
                    public void run() {
                        if (warlordsNPC.isDead()) {
                            this.cancel();
                        }

                        barrageOfFlames();
                    }
                }.runTaskLater(40);
                barrageOfFlamesDelay = 15;
            } else {
                if (barrageOfFlamesDelay > 0) {
                    barrageOfFlamesDelay--;
                }
            }
        }
    }

    private void barrageOfFlames() {
        new GameRunnable(warlordsNPC.getGame()) {
            int counter = 0;

            @Override
            public void run() {
                if (warlordsNPC.isDead()) {
                    this.cancel();
                    return;
                }

                counter++;
                List<WarlordsEntity> enemies = PlayerFilter.playingGame(warlordsNPC.getGame())
                                                           .aliveEnemiesOf(warlordsNPC)
                                                           .toList();
                for (Fireball fireball : warlordsNPC.getAbilitiesMatching(Fireball.class)) {
                    for (WarlordsEntity enemy : enemies) {
                        Location predictedLocation = PredictTargetFutureLocationGoal.lookAtLocation(
                                warlordsNPC.getEyeLocation(),
                                PredictTargetFutureLocationGoal.predictFutureLocation(warlordsNPC, enemy).add(0, 1, 0)
                        );
                        fireball.fire(warlordsNPC, predictedLocation);
                    }
                }

                if (counter == 4 * 5) {
                    this.cancel();
                }
            }
        }.runTaskTimer(0, 5);
    }


    @Override
    public void onAttack(WarlordsEntity attacker, WarlordsEntity receiver, WarlordsDamageHealingEvent event) {

    }

    @Override
    public void onDamageTaken(WarlordsEntity self, WarlordsEntity attacker, WarlordsDamageHealingEvent event) {
        if (self.getHealth() / self.getMaxHealth() < 0.5 && !halfHealthSpawned) {
            halfHealthSpawned = true;
            List<Location> spawnLocations = LocationUtils.getCircle(warlordsNPC.getLocation(), 3, pveOption.playerCount());
            for (Location location : spawnLocations) {
                pveOption.spawnNewMob(Mob.FIRE_SPLITTER.createMob(location));
            }
        }
    }

    private static class BurstOfFlames extends AbstractPveAbility {

        private float radius = 10;

        public BurstOfFlames() {
            super("Burst of Flames", 860, 940, 5, 100);
        }

        @Override
        public boolean onPveActivate(@Nonnull WarlordsEntity wp, PveOption pveOption) {
            wp.subtractEnergy(name, energyCost, false);
            EffectUtils.playSphereAnimation(
                    wp.getLocation(),
                    radius,
                    Particle.FLAME,
                    1,
                    3.5f
            );
            PlayerFilter.entitiesAround(wp, radius, radius, radius)
                        .aliveEnemiesOf(wp)
                        .forEach(warlordsEntity -> {
                            warlordsEntity.addDamageInstance(
                                    wp,
                                    name,
                                    minDamageHeal,
                                    maxDamageHeal,
                                    critChance,
                                    critMultiplier
                            );
                        });
            return true;
        }
    }
}
