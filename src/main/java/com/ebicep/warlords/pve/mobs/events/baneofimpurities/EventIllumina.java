package com.ebicep.warlords.pve.mobs.events.baneofimpurities;

import com.ebicep.warlords.abilities.PrismGuard;
import com.ebicep.warlords.abilities.internal.DamageCheck;
import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.effects.FireWorkEffectPlayer;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.game.option.pve.PveOption;
import com.ebicep.warlords.player.general.Weapons;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.PermanentCooldown;
import com.ebicep.warlords.player.ingame.cooldowns.instances.InstanceFlags;
import com.ebicep.warlords.pve.DifficultyIndex;
import com.ebicep.warlords.pve.mobs.MobTier;
import com.ebicep.warlords.pve.mobs.Mobs;
import com.ebicep.warlords.pve.mobs.bosses.Illumina;
import com.ebicep.warlords.pve.mobs.irongolem.IronGolem;
import com.ebicep.warlords.pve.mobs.mobtypes.BossMob;
import com.ebicep.warlords.pve.mobs.zombie.AbstractZombie;
import com.ebicep.warlords.util.chat.ChatUtils;
import com.ebicep.warlords.util.java.RandomCollection;
import com.ebicep.warlords.util.pve.SkullID;
import com.ebicep.warlords.util.pve.SkullUtils;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.*;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

public class EventIllumina extends AbstractZombie implements BossMob {

    private final RandomCollection<Mobs> summonList = new RandomCollection<Mobs>()
            .add(0.1, Mobs.EXTREME_ZEALOT)
            .add(0.3, Mobs.EXILED_SKELETON)
            .add(0.2, Mobs.FORGOTTEN_LANCER)
            .add(0.1, Mobs.EXILED_ZOMBIE_RIFT)
            .add(0.05, Mobs.FORGOTTEN_ZOMBIE)
            .add(0.1, Mobs.SLIME_ZOMBIE)
            .add(0.1, Mobs.ENVOY_BERSERKER_ZOMBIE)
            .add(0.05, Mobs.EXILED_VOID_LANCER)
            .add(0.1, Mobs.EXILED_ZOMBIE_LAVA);
    private boolean phaseOneTriggered = false;
    private boolean phaseTwoTriggered = false;
    private boolean phaseThreeTriggered = false;
    private AtomicInteger damageToDeal = new AtomicInteger(0);
    private PrismGuard prismGuard = new PrismGuard() {{
        setTickDuration(200);
    }};

    public EventIllumina(Location spawnLocation) {
        super(spawnLocation,
                "Illumina",
                MobTier.BOSS,
                new Utils.SimpleEntityEquipment(
                        SkullUtils.getSkullFrom(SkullID.DEEP_DARK_WORM),
                        Utils.applyColorTo(Material.LEATHER_CHESTPLATE, 120, 120, 200),
                        Utils.applyColorTo(Material.LEATHER_LEGGINGS, 120, 120, 200),
                        Utils.applyColorTo(Material.LEATHER_BOOTS, 120, 120, 200),
                        Weapons.NEW_LEAF_SCYTHE.getItem()
                ),
                350000,
                0.33f,
                0,
                950,
                1350,
                new Illumina.BrambleSlowness()
        );
    }

    @Override
    public Component getDescription() {
        return Component.text("", NamedTextColor.DARK_GRAY);
    }

    @Override
    public NamedTextColor getColor() {
        return NamedTextColor.BLUE;
    }

    @Override
    public void onSpawn(PveOption option) {
        super.onSpawn(option);

        for (int i = 0; i < (2 * option.getGame().warlordsPlayers().count()); i++) {
            option.spawnNewMob(new IronGolem(spawnLocation));
        }

        warlordsNPC.getCooldownManager().removeCooldown(DamageCheck.class, false);
        warlordsNPC.getCooldownManager().addCooldown(new PermanentCooldown<>(
                "Damage Check",
                null,
                DamageCheck.class,
                DamageCheck.DAMAGE_CHECK,
                warlordsNPC,
                CooldownTypes.ABILITY,
                cooldownManager -> {
                },
                true
        ) {
            @Override
            public float modifyDamageAfterInterveneFromSelf(WarlordsDamageHealingEvent event, float currentDamageValue) {
                damageToDeal.set((int) (damageToDeal.get() - currentDamageValue));
                return currentDamageValue;
            }

            @Override
            public void multiplyKB(Vector currentVector) {
                // immune to KB
                currentVector.multiply(0.05);
            }
        });
    }

    @Override
    public void whileAlive(int ticksElapsed, PveOption option) {
        // immune to slowness
        warlordsNPC.getSpeed().removeSlownessModifiers();

        long playerCount = option.getGame().warlordsPlayers().count();
        Location loc = warlordsNPC.getLocation();
        DifficultyIndex difficulty = option.getDifficulty();

        if (!phaseOneTriggered && warlordsNPC.getHealth() < (warlordsNPC.getMaxHealth() * .7f)) {
            phaseOneTriggered = true;
            timedDamage(option, playerCount, 7500, 11);
        } else if (!phaseTwoTriggered && warlordsNPC.getHealth() < (warlordsNPC.getMaxHealth() * .4f)) {
            phaseTwoTriggered = true;
            timedDamage(option, playerCount, 10000, 11);
        } else if (!phaseThreeTriggered && warlordsNPC.getHealth() < (warlordsNPC.getMaxHealth() * .1f)) {
            phaseThreeTriggered = true;
            timedDamage(option, playerCount, 14500, 11);
        }

        if (ticksElapsed % 200 == 0) {
            for (int i = 0; i < 5; i++) {
                option.spawnNewMob(summonList.next().createMob.apply(loc));
            }
        }
    }

    @Override
    public void onAttack(WarlordsEntity attacker, WarlordsEntity receiver, WarlordsDamageHealingEvent event) {

    }

    @Override
    public void onDamageTaken(WarlordsEntity self, WarlordsEntity attacker, WarlordsDamageHealingEvent event) {

    }

    @Override
    public void onDeath(WarlordsEntity killer, Location deathLocation, PveOption option) {
        super.onDeath(killer, deathLocation, option);
        FireWorkEffectPlayer.playFirework(deathLocation, FireworkEffect.builder()
                                                                       .withColor(Color.BLUE)
                                                                       .with(FireworkEffect.Type.BALL_LARGE)
                                                                       .build());
        EffectUtils.strikeLightning(deathLocation, false, 2);
    }

    private void timedDamage(PveOption option, long playerCount, int damageValue, int timeToDealDamage) {
        damageToDeal.set((int) (damageValue * playerCount));

        for (WarlordsEntity we : PlayerFilter
                .playingGame(getWarlordsNPC().getGame())
                .aliveEnemiesOf(warlordsNPC)
        ) {
            we.getEntity().showTitle(Title.title(
                    Component.empty(),
                    Component.text("Keep attacking Illumina to stop the draining!", NamedTextColor.RED),
                    Title.Times.times(Ticks.duration(10), Ticks.duration(35), Ticks.duration(0))
            ));

            Utils.addKnockback(name, warlordsNPC.getLocation(), we, -4, 0.35);
            Utils.playGlobalSound(warlordsNPC.getLocation(), Sound.ENTITY_WITHER_SPAWN, 500, 0.3f);
        }

        AtomicInteger countdown = new AtomicInteger(timeToDealDamage);
        new GameRunnable(warlordsNPC.getGame()) {
            int counter = 0;

            @Override
            public void run() {
                if (warlordsNPC.isDead()) {
                    this.cancel();
                    return;
                }

                if (damageToDeal.get() <= 0) {
                    FireWorkEffectPlayer.playFirework(warlordsNPC.getLocation(), FireworkEffect.builder()
                                                                                               .withColor(Color.WHITE)
                                                                                               .with(FireworkEffect.Type.BALL_LARGE)
                                                                                               .build());
                    prismGuard.onActivate(warlordsNPC, null);
                    this.cancel();
                    return;
                }

                if (counter++ % 20 == 0) {
                    countdown.getAndDecrement();
                    Utils.playGlobalSound(warlordsNPC.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 500, 0.4f);
                    Utils.playGlobalSound(warlordsNPC.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 500, 0.4f);
                    for (WarlordsEntity we : PlayerFilter
                            .entitiesAround(warlordsNPC, 100, 100, 100)
                            .aliveEnemiesOf(warlordsNPC)
                    ) {
                        EffectUtils.playParticleLinkAnimation(
                                we.getLocation(),
                                warlordsNPC.getLocation(),
                                255,
                                255,
                                255,
                                2
                        );

                        we.addDamageInstance(
                                warlordsNPC,
                                "Vampiric Leash",
                                600,
                                600,
                                -1,
                                100
                        );
                    }
                }

                if (countdown.get() <= 0 && damageToDeal.get() > 0) {
                    for (int i = 0; i < (2 * option.getGame().warlordsPlayers().count()); i++) {
                        option.spawnNewMob(new IronGolem(spawnLocation));
                    }

                    FireWorkEffectPlayer.playFirework(warlordsNPC.getLocation(), FireworkEffect.builder()
                                                                                               .withColor(Color.WHITE)
                                                                                               .with(FireworkEffect.Type.BALL_LARGE)
                                                                                               .build());
                    EffectUtils.strikeLightning(warlordsNPC.getLocation(), false, 10);
                    Utils.playGlobalSound(warlordsNPC.getLocation(), "shaman.earthlivingweapon.impact", 500, 0.5f);

                    for (WarlordsEntity we : PlayerFilter
                            .entitiesAround(warlordsNPC, 100, 100, 100)
                            .aliveEnemiesOf(warlordsNPC)
                    ) {
                        Utils.addKnockback(name, warlordsNPC.getLocation(), we, -2, 0.4);
                        EffectUtils.playParticleLinkAnimation(we.getLocation(), warlordsNPC.getLocation(), Particle.VILLAGER_HAPPY);
                        we.addDamageInstance(
                                warlordsNPC,
                                "Death Ray",
                                7500,
                                7500,
                                -1,
                                100,
                                EnumSet.of(InstanceFlags.TRUE_DAMAGE)
                        );

                        warlordsNPC.addHealingInstance(
                                warlordsNPC,
                                "Death Ray Healing",
                                we.getMaxHealth() * 2,
                                we.getMaxHealth() * 2,
                                -1,
                                100
                        );
                    }

                    this.cancel();
                }

                ChatUtils.sendTitleToGamePlayers(
                        getWarlordsNPC().getGame(),
                        Component.text(countdown.get(), NamedTextColor.YELLOW),
                        Component.text(damageToDeal.get(), NamedTextColor.RED),
                        0, 4, 0
                );
            }
        }.runTaskTimer(40, 0);
    }
}
