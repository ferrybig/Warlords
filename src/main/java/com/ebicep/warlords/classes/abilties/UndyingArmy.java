package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.effects.circle.CircleEffect;
import com.ebicep.warlords.effects.circle.CircumferenceEffect;
import com.ebicep.warlords.player.CooldownTypes;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.util.ItemBuilder;
import com.ebicep.warlords.util.Matrix4d;
import com.ebicep.warlords.util.ParticleEffect;
import com.ebicep.warlords.util.PlayerFilter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;


public class UndyingArmy extends AbstractAbility {

    public static final ItemStack BONE = new ItemBuilder(Material.BONE)
            .name(ChatColor.RED + "Instant Kill")
            .lore("§7Right-click this item to die\n§7instantly instead of waiting for\n§7the decay.")
            .get();

    private final int radius = 15;
    private final int duration = 10;

    private boolean armyDead = false;

    //dead = true - take 10% dmg
    //dead = false - heal
    public boolean isArmyDead() {
        return this.armyDead;
    }

    public void pop() {
        this.armyDead = true;
    }

    public UndyingArmy() {
        super("Undying Army", 0, 0, 62.64f, 20, 0, 0);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7You may chain up to §e5 §7allies in a §e" + radius + "\n" +
                "§7block radius to heal them for §a100 §7+\n" +
                "§7§a10% §7of their missing health every 2 seconds.\n" +
                "Lasts §6" + duration + " §7seconds." +
                "\n\n" +
                "§7Chained allies that take fatal damage\n" +
                "§7will be revived with §a100% §7of their max health\n" +
                "§7and §e50% §7max energy. Revived allies rapidly\n" +
                "§7take §c10% §7of their max health as damage every\n" +
                "§70.75 seconds." +
                "\n\n" +
                "§7Picking up orbs while being under the effect\n" +
                "§7of undying army will grant you §e5% §7damage\n" +
                "§7reduction for §63 §7seconds.";
    }

    @Override
    public void onActivate(WarlordsPlayer wp, Player player) {
        wp.subtractEnergy(energyCost);
        UndyingArmy tempUndyingArmy = new UndyingArmy();
        wp.getCooldownManager().addCooldown(name, UndyingArmy.this.getClass(), tempUndyingArmy, "ARMY", duration, wp, CooldownTypes.ABILITY);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (wp.getCooldownManager().getCooldown(tempUndyingArmy).isPresent()) {
                    if (!((UndyingArmy) wp.getCooldownManager().getCooldown(tempUndyingArmy).get().getCooldownObject()).isArmyDead()) {
                        float healAmount = 100 + (wp.getMaxHealth() - wp.getHealth()) / 10f;
                        wp.addHealth(wp, name, healAmount, healAmount, -1, 100);
                        player.playSound(wp.getLocation(), "paladin.holyradiance.activation", 0.25f, 0.8f);

                        // particles
                        Location playerLoc = player.getLocation();
                        playerLoc.add(0, 2.1, 0);
                        Location particleLoc = playerLoc.clone();
                        for (int i = 0; i < 1; i++) {
                            for (int j = 0; j < 10; j++) {
                                double angle = j / 10D * Math.PI * 2;
                                double width = 0.5;
                                particleLoc.setX(playerLoc.getX() + Math.sin(angle) * width);
                                particleLoc.setY(playerLoc.getY() + i / 5D);
                                particleLoc.setZ(playerLoc.getZ() + Math.cos(angle) * width);

                                ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(255, 255, 255), particleLoc, 500);
                            }
                        }
                    } else {
                        this.cancel();
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Warlords.getInstance(), 0, 40);

        int numberOfPlayersWithArmy = 0;
        for (WarlordsPlayer teammate : PlayerFilter.entitiesAround(wp, radius, radius, radius)
                .aliveTeammatesOfExcludingSelf(wp)
        ) {
            wp.sendMessage("§a\u00BB§7 " + ChatColor.GRAY + "Your Undying Army is now protecting " + teammate.getColoredName() + ChatColor.GRAY + ".");
            teammate.getCooldownManager().addCooldown(name, UndyingArmy.this.getClass(), tempUndyingArmy, "ARMY", duration, wp, CooldownTypes.ABILITY);
            teammate.sendMessage("§a\u00BB§7 " + ChatColor.GRAY + wp.getName() + "'s Undying Army protects you for " + ChatColor.GOLD + duration + ChatColor.GRAY + " seconds.");
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (teammate.getCooldownManager().getCooldown(tempUndyingArmy).isPresent()) {
                        if (!((UndyingArmy) teammate.getCooldownManager().getCooldown(tempUndyingArmy).get().getCooldownObject()).isArmyDead()) {
                            float healAmount = 100 + (teammate.getMaxHealth() - teammate.getHealth()) / 10f;
                            teammate.addHealth(wp, name, healAmount, healAmount, -1, 100);
                            player.playSound(teammate.getLocation(), "paladin.holyradiance.activation", 0.25f, 0.8f);

                            // particles
                            Location playerLoc = player.getLocation();
                            playerLoc.add(0, 2.1, 0);
                            Location particleLoc = playerLoc.clone();
                            for (int i = 0; i < 1; i++) {
                                for (int j = 0; j < 10; j++) {
                                    double angle = j / 10D * Math.PI * 2;
                                    double width = 0.5;
                                    particleLoc.setX(playerLoc.getX() + Math.sin(angle) * width);
                                    particleLoc.setY(playerLoc.getY() + i / 5D);
                                    particleLoc.setZ(playerLoc.getZ() + Math.cos(angle) * width);

                                    ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(255, 255, 255), particleLoc, 500);
                                }
                            }
                        } else {
                            this.cancel();
                        }
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Warlords.getInstance(), 0, 40);
            numberOfPlayersWithArmy++;

            if (numberOfPlayersWithArmy >= 5) {
                break;
            }
        }

        String allies = numberOfPlayersWithArmy == 1 ? "ally." : "allies.";
        wp.sendMessage("§a\u00BB§7 " + ChatColor.GRAY + "Your Undying Army is now protecting " + ChatColor.YELLOW + numberOfPlayersWithArmy + ChatColor.GRAY + " nearby " + allies);

        for (Player player1 : player.getWorld().getPlayers()) {
            player1.playSound(player.getLocation(), Sound.ZOMBIE_IDLE, 2, 0.3f);
            player1.playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 2, 0.9f);
        }

        // particles
        Location loc = player.getEyeLocation();
        loc.setPitch(0);
        loc.setYaw(0);
        Matrix4d matrix = new Matrix4d();
        for (int i = 0; i < 9; i++) {
            loc.setYaw(loc.getYaw() + 360F / 9F);
            matrix.updateFromLocation(loc);
            for (int c = 0; c < 30; c++) {
                double angle = c / 30D * Math.PI * 2;
                double width = 1.5;

                ParticleEffect.ENCHANTMENT_TABLE.display(0, 0.1f, 0, 0, 1,
                        matrix.translateVector(player.getWorld(), radius, Math.sin(angle) * width, Math.cos(angle) * width), 500);
            }

            for (int c = 0; c < 15; c++) {
                double angle = c / 15D * Math.PI * 2;
                double width = 0.6;

                ParticleEffect.SPELL.display(0, 0, 0, 0, 1,
                        matrix.translateVector(player.getWorld(), radius, Math.sin(angle) * width, Math.cos(angle) * width), 500);
            }
        }

        CircleEffect circle = new CircleEffect(wp.getGame(), wp.getTeam(), player.getLocation(), radius);
        circle.addEffect(new CircumferenceEffect(ParticleEffect.VILLAGER_HAPPY).particlesPerCircumference(2));
        circle.playEffects();
    }
}
