package com.ebicep.warlords.abilties.pve;

import com.ebicep.warlords.abilties.OrderOfEviscerate;
import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.effects.ParticleEffect;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.game.option.marker.FlagHolder;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.util.java.Pair;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PveOrderOfEviscerate extends AbstractAbility {
    protected int numberOfFullResets = 0;
    protected int numberOfHalfResets = 0;
    protected int numberOfBackstabs = 0;

    private int duration = 16;
    private float damageThreshold = 0;
    private WarlordsEntity markedPlayer;

    public PveOrderOfEviscerate() {
        super("Order of Eviscerate", 0, 0, 50, 60, -1, 100);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Cloak yourself for §6" + duration + " §7seconds, granting\n" +
                "§7you §e40% §7movement speed and making you §einvisible\n" +
                "§7to the enemy for the duration. However, taking up to\n" +
                "§c600 §7fall damage or any type of ability damage will\n" +
                "§7end your invisibility." +
                "\n\n" +
                "§7All your attacks against an enemy will mark them vulnerable.\n" +
                "§7Vulnerable enemies take §c130% §7more damage. Additionally,\n" +
                "§7enemies hit from behind take an additional §c140% §7more damage." +
                "\n\n" +
                "§7Successfully killing your mark will §ereset §7both your\n" +
                "§7Shadow Step and Order of Eviscerate's cooldown\n" +
                "§7and refund the energy cost. Assisting in killing your\n" +
                "§7mark will only refund half the cooldown.";
    }

    @Override
    public List<Pair<String, String>> getAbilityInfo() {
        List<Pair<String, String>> info = new ArrayList<>();
        info.add(new Pair<>("Times Used", "" + timesUsed));
        info.add(new Pair<>("Number of Full Resets", "" + numberOfFullResets));
        info.add(new Pair<>("Number of Half Resets", "" + numberOfHalfResets));
        info.add(new Pair<>("Number of Backstabs", "" + numberOfBackstabs));

        return info;
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsEntity wp, @Nonnull Player player) {
        wp.subtractEnergy(energyCost, false);
        Utils.playGlobalSound(player.getLocation(), Sound.GHAST_FIREBALL, 1.5f, 0.7f);
        Runnable cancelSpeed = wp.addSpeedModifier("Order of Eviscerate", 40, duration * 20, "BASE");

        wp.getCooldownManager().removeCooldown(OrderOfEviscerate.class);
        wp.getCooldownManager().addCooldown(new RegularCooldown<PveOrderOfEviscerate>(
                "Order of Eviscerate",
                "ORDER",
                PveOrderOfEviscerate.class,
                new PveOrderOfEviscerate(),
                wp,
                CooldownTypes.ABILITY,
                cooldownManager -> {
                    cancelSpeed.run();
                    removeCloak(wp, true);
                },
                duration * 20,
                Collections.singletonList((cooldown, ticksLeft, ticksElapsed) -> {
                    Utils.playGlobalSound(wp.getLocation(), Sound.AMBIENCE_CAVE, 0.4f, 2);
                    ParticleEffect.SMOKE_NORMAL.display(0, 0.2f, 0, 0.05f, 4, wp.getLocation(), 500);
                    if (ticksElapsed % 10 == 0) {
                        ParticleEffect.FOOTSTEP.display(0, 0, 0, 1, 1, wp.getLocation().add(0, 0.1, 0), 500);
                    }
                })
        ) {
            @Override
            public void doBeforeReductionFromAttacker(WarlordsDamageHealingEvent event) {
                //mark message here so it displays before damage
                WarlordsEntity victim = event.getPlayer();
                if (victim != wp) {
                    if (!Objects.equals(this.getCooldownObject().getMarkedPlayer(), victim)) {
                        wp.sendMessage(WarlordsEntity.GIVE_ARROW_GREEN + ChatColor.GRAY + " You have marked §e" + victim.getName());
                    }
                    this.getCooldownObject().setMarkedPlayer(victim);
                }
            }

            @Override
            public float modifyDamageBeforeInterveneFromAttacker(WarlordsDamageHealingEvent event, float currentDamageValue) {
                if (
                        Objects.equals(this.getCooldownObject().getMarkedPlayer(), event.getPlayer()) &&
                                !Utils.isLineOfSightAssassin(event.getPlayer().getEntity(), event.getAttacker().getEntity())
                ) {
                    numberOfBackstabs++;
                    return currentDamageValue * 1.6f;
                } else {
                    return currentDamageValue * 1.5f;
                }
            }

            @Override
            public void onDamageFromSelf(WarlordsDamageHealingEvent event, float currentDamageValue, boolean isCrit) {
                PveOrderOfEviscerate pveOrderOfEviscerate = this.getCooldownObject();
                pveOrderOfEviscerate.addAndCheckDamageThreshold(currentDamageValue, wp);
            }

            @Override
            public void onDeathFromEnemies(WarlordsDamageHealingEvent event, float currentDamageValue, boolean isCrit, boolean isKiller) {
                if (!Objects.equals(event.getPlayer(), this.getCooldownObject().getMarkedPlayer())) {
                    return;
                }
                this.setTicksLeft(0);
                if (isKiller) {
                    numberOfFullResets++;

                    new GameRunnable(wp.getGame()) {
                        @Override
                        public void run() {
                            wp.sendMessage(WarlordsEntity.GIVE_ARROW_GREEN +
                                    ChatColor.GRAY + " You killed your mark," +
                                    ChatColor.YELLOW + " your cooldowns have been reset" +
                                    ChatColor.GRAY + "!"
                            );

                            wp.setOrangeCurrentCooldown(getCurrentCooldown() / 2);
                            wp.updateOrangeItem();
                            wp.addEnergy(wp, name, energyCost);
                        }
                    }.runTaskLater(2);
                } else {
                    numberOfHalfResets++;

                    new GameRunnable(wp.getGame()) {
                        @Override
                        public void run() {
                            wp.sendMessage(WarlordsEntity.GIVE_ARROW_GREEN +
                                    ChatColor.GRAY + " You assisted in killing your mark," +
                                    ChatColor.YELLOW + " your cooldowns have been reduced by half" +
                                    ChatColor.GRAY + "!"
                            );

                            wp.setPurpleCurrentCooldown(wp.getPurpleAbility().getCurrentCooldown() / 2);
                            wp.setOrangeCurrentCooldown(wp.getOrangeAbility().getCurrentCooldown() / 2);
                            wp.updatePurpleItem();
                            wp.updateOrangeItem();
                            wp.addEnergy(wp, name, energyCost / 2f);
                        }
                    }.runTaskLater(2);
                }

                wp.playSound(wp.getLocation(), Sound.AMBIENCE_THUNDER, 1, 2);
            }
        });

        if (!FlagHolder.isPlayerHolderFlag(wp)) {
            wp.getCooldownManager().removeCooldownByName("Cloaked");
            wp.getCooldownManager().addRegularCooldown("Cloaked",
                    "INVIS",
                    PveOrderOfEviscerate.class,
                    null,
                    wp,
                    CooldownTypes.BUFF,
                    cooldownManager -> {
                        wp.getEntity().removePotionEffect(PotionEffectType.INVISIBILITY);

                        LivingEntity wpEntity = wp.getEntity();
                        if (wpEntity instanceof Player) {
                            PlayerFilter.playingGame(wp.getGame())
                                    .enemiesOf(wp)
                                    .stream().map(WarlordsEntity::getEntity)
                                    .filter(Player.class::isInstance)
                                    .map(Player.class::cast)
                                    .forEach(enemyPlayer -> enemyPlayer.showPlayer((Player) wpEntity));
                        }
                    },
                    duration * 20,
                    Collections.singletonList((cooldown, ticksLeft, ticksElapsed) -> {
                        if (ticksElapsed % 5 == 0) {
                            wp.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, ticksLeft, 0, true, false));

                            LivingEntity wpEntity = wp.getEntity();
                            if (wpEntity instanceof Player) {
                                ((Player) wpEntity).getInventory().setArmorContents(new ItemStack[]{null, null, null, null});
                                PlayerFilter.playingGame(wp.getGame())
                                        .enemiesOf(wp)
                                        .stream().map(WarlordsEntity::getEntity)
                                        .filter(Player.class::isInstance)
                                        .map(Player.class::cast)
                                        .forEach(enemyPlayer -> enemyPlayer.hidePlayer((Player) wpEntity));
                            }
                        }
                    })
            );
        }

        return true;
    }

    public static void removeCloak(WarlordsEntity warlordsPlayer, boolean forceRemove) {
        if (warlordsPlayer.getCooldownManager().hasCooldownFromName("Cloaked") || forceRemove) {
            warlordsPlayer.getCooldownManager().removeCooldownByName("Cloaked");
            warlordsPlayer.getEntity().removePotionEffect(PotionEffectType.INVISIBILITY);
            warlordsPlayer.updateArmor();
        }
    }

    public WarlordsEntity getMarkedPlayer() {
        return markedPlayer;
    }

    public void addAndCheckDamageThreshold(float damageValue, WarlordsEntity warlordsPlayer) {
        addToDamageThreshold(damageValue);
        if (getDamageThreshold() >= 600) {
            OrderOfEviscerate.removeCloak(warlordsPlayer, false);
        }
    }

    public void addToDamageThreshold(float damageThreshold) {
        this.damageThreshold += damageThreshold;
    }

    public float getDamageThreshold() {
        return damageThreshold;
    }

    public void setMarkedPlayer(WarlordsEntity markedPlayer) {
        this.markedPlayer = markedPlayer;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}


