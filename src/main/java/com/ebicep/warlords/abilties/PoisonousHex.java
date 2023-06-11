package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractPiercingProjectile;
import com.ebicep.warlords.abilties.internal.Duration;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownFilter;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.util.bukkit.LocationBuilder;
import com.ebicep.warlords.util.java.Pair;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class PoisonousHex extends AbstractPiercingProjectile implements Duration {

    private void givePoisonousHex(WarlordsEntity from, WarlordsEntity to) {
        List<RegularCooldown> currentPoisonousHexes = new CooldownFilter<>(to, RegularCooldown.class)
                .filterCooldownClass(PoisonousHex.class)
                .stream().toList();
        if (currentPoisonousHexes.size() >= 3) {
            to.getCooldownManager().removeCooldown(currentPoisonousHexes.get(0));
        }
        to.getCooldownManager().addCooldown(new RegularCooldown<>(
                "Poisonous Hex",
                "PHEX",
                PoisonousHex.class,
                new PoisonousHex(),
                from,
                CooldownTypes.DEBUFF,
                cooldownManager -> {

                },
                tickDuration + 20,
                Collections.singletonList((cooldown, ticksLeft, ticksElapsed) -> {
                    if (ticksElapsed % 20 == 0 && ticksElapsed != 0) {
                        to.addDamageInstance(from, name, dotMinDamage, dotMaxDamage, 0, 100, false);
                    }
                })
        ));
    }

    private int dotMinDamage = 42;
    private int dotMaxDamage = 57;
    private double hitBox = 3.5;
    private int tickDuration = 80;

    public PoisonousHex() {
        super("Poisonous Hex", 359, 485, 0, 80, 25, 175, 2, 30, false);
        this.shotsFiredAtATime = 2;
        this.forwardTeleportAmount = 1.6f;
    }

    @Override
    public void updateDescription(Player player) {
        description = Component.text("Throw 2 Hex Fangs in front of you, dealing ")
                               .append(formatRangeDamage(minDamageHeal, maxDamageHeal))
                               .append(Component.text(" damage to up to 2 enemies. Additionally, hit targets receive 1 stack of Poisonous Hex. Dealing "))
                               .append(formatRangeDamage(dotMinDamage, dotMaxDamage))
                               .append(Component.text(" damage every second for "))
                               .append(Component.text("4", NamedTextColor.GOLD))
                               .append(Component.text(" seconds. Stacks up to 3 times."))
                               .append(Component.text("\n\nHas an optimal range of "))
                               .append(Component.text(maxDistance, NamedTextColor.YELLOW))
                               .append(Component.text("blocks."));
    }

    @Override
    public List<Pair<String, String>> getAbilityInfo() {
        return null;
    }

    @Override
    protected void playEffect(@Nonnull Location currentLocation, int ticksLived) {

    }

    @Override
    protected int onHit(@Nonnull InternalProjectile projectile, @Nullable WarlordsEntity hit) {
        if (projectile.getHit().size() > 2) {
            return 0;
        }
        WarlordsEntity wp = projectile.getShooter();
        Location currentLocation = projectile.getCurrentLocation();

        Utils.playGlobalSound(currentLocation, "shaman.lightningbolt.impact", 2, 1);

        int playersHit = 0;
        for (WarlordsEntity enemy : PlayerFilter
                .entitiesAround(currentLocation, hitBox, hitBox, hitBox)
                .aliveEnemiesOf(wp)
                .excluding(projectile.getHit())
        ) {
            getProjectiles(projectile).forEach(p -> p.getHit().add(enemy));
            playersHit++;
            if (enemy.onHorse()) {
                numberOfDismounts++;
            }
            enemy.addDamageInstance(wp, name, minDamageHeal, maxDamageHeal, critChance, critMultiplier, false);
            givePoisonousHex(wp, enemy);
        }

        return playersHit;
    }


    @Override
    protected boolean shouldEndProjectileOnHit(@Nonnull InternalProjectile projectile, WarlordsEntity wp) {
        return false;
    }

    @Override
    protected boolean shouldEndProjectileOnHit(@Nonnull InternalProjectile projectile, Block block) {
        return true;
    }

    @Override
    protected void onNonCancellingHit(@Nonnull InternalProjectile projectile, @Nonnull WarlordsEntity hit, @Nonnull Location impactLocation) {
        if (projectile.getHit().size() > 2) {
            return;
        }
        WarlordsEntity wp = projectile.getShooter();
        Location currentLocation = projectile.getCurrentLocation();

        Utils.playGlobalSound(currentLocation, "shaman.lightningbolt.impact", 2, 1);

        for (WarlordsEntity enemy : PlayerFilter
                .entitiesAround(currentLocation, hitBox, hitBox, hitBox)
                .aliveEnemiesOf(wp)
                .excluding(projectile.getHit())
        ) {
            getProjectiles(projectile).forEach(p -> p.getHit().add(enemy));
            if (enemy.onHorse()) {
                numberOfDismounts++;
            }
            enemy.addDamageInstance(wp, name, minDamageHeal, maxDamageHeal, critChance, critMultiplier, false);
            givePoisonousHex(wp, enemy);
        }
    }

    @Override
    protected Location getProjectileStartingLocation(WarlordsEntity shooter, Location startingLocation) {
        return new LocationBuilder(startingLocation.clone()).addY(-.5).backward(0f);
    }

    @Override
    protected void onSpawn(@Nonnull InternalProjectile projectile) {
        super.onSpawn(projectile);
        ArmorStand fallenSoul = Utils.spawnArmorStand(projectile.getStartingLocation().clone().add(0, -1.7, 0), armorStand -> {
            armorStand.setMarker(true);
            armorStand.getEquipment().setHelmet(new ItemStack(Material.ACACIA_FENCE_GATE));
            armorStand.setHeadPose(new EulerAngle(-Math.atan2(
                    projectile.getSpeed().getY(),
                    Math.sqrt(
                            Math.pow(projectile.getSpeed().getX(), 2) +
                                    Math.pow(projectile.getSpeed().getZ(), 2)
                    )
            ), 0, 0));
        });

        projectile.addTask(new InternalProjectileTask() {
            @Override
            public void run(InternalProjectile projectile) {
                fallenSoul.teleport(projectile.getCurrentLocation().clone().add(0, -1.7, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
                projectile.getCurrentLocation().getWorld().spawnParticle(
                        Particle.SPELL_WITCH,
                        projectile.getCurrentLocation().clone().add(0, 0, 0),
                        1,
                        0,
                        0,
                        0,
                        0,
                        null,
                        true
                );
            }

            @Override
            public void onDestroy(InternalProjectile projectile) {
                fallenSoul.remove();
                projectile.getCurrentLocation().getWorld().spawnParticle(
                        Particle.SPELL_WITCH,
                        projectile.getCurrentLocation(),
                        1,
                        0,
                        0,
                        0,
                        0.7f,
                        null,
                        true
                );
            }
        });
    }

    @Nullable
    @Override
    protected String getActivationSound() {
        return "shaman.lightningbolt.impact";
    }

    @Override
    protected float getSoundVolume() {
        return 2;
    }

    @Override
    protected float getSoundPitch() {
        return 1.5f;
    }

    @Override
    public int getTickDuration() {
        return tickDuration;
    }

    @Override
    public void setTickDuration(int tickDuration) {
        this.tickDuration = tickDuration;
    }
}
