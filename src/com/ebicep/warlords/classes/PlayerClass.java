package com.ebicep.warlords.classes;

import com.ebicep.warlords.Warlords;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

public abstract class PlayerClass {

    protected Player player;
    protected int maxHealth;
    protected int maxEnergy;
    protected int energyPerSec;
    protected int energyOnHit;
    protected int damageResistance;
    protected AbstractAbility weapon;
    protected AbstractAbility red;
    protected AbstractAbility purple;
    protected AbstractAbility blue;
    protected AbstractAbility orange;
    protected String className;

    public PlayerClass(Player player, int maxHealth, int maxEnergy, int energyPerSec, int energyOnHit, int damageResistance, AbstractAbility weapon, AbstractAbility red, AbstractAbility purple, AbstractAbility blue, AbstractAbility orange) {
        this.player = player;
        this.maxHealth = maxHealth;
        this.maxEnergy = maxEnergy;
        this.energyPerSec = energyPerSec;
        this.energyOnHit = energyOnHit;
        this.damageResistance = damageResistance;
        this.weapon = weapon;
        this.red = red;
        this.purple = purple;
        this.blue = blue;
        this.orange = orange;
        if (red.getName().contains("Consecrate")) {
            className = "Paladin";
        } else if (purple.getName().contains("Time")) {
            className = "Mage";
        } else if (purple.getName().contains("Ground")) {
            className = "Warrior";
        } else {
            className = "Shaman";
        }
    }

    public void onRightClick(Player player) {
        if (player.getInventory().getHeldItemSlot() == 0) {
            if (player.getLevel() >= weapon.getEnergyCost()) {
                weapon.onActivate(player);
            }
        } else if (player.getInventory().getHeldItemSlot() == 1) {
            if (red.getCurrentCooldown() == 0 && player.getLevel() >= red.getEnergyCost()) {
                red.onActivate(player);
                if (!red.getName().contains("Chain") && !red.getName().contains("Link"))
                    red.setCurrentCooldown(red.cooldown);
            }
        } else if (player.getInventory().getHeldItemSlot() == 2) {
            if (purple.getCurrentCooldown() == 0 && player.getLevel() >= purple.getEnergyCost()) {
                purple.onActivate(player);
                purple.setCurrentCooldown(purple.cooldown);
            }
        } else if (player.getInventory().getHeldItemSlot() == 3) {
            if (blue.getCurrentCooldown() == 0 && player.getLevel() >= blue.getEnergyCost()) {
                blue.onActivate(player);
                if (!blue.getName().contains("Chain")) {
                    blue.setCurrentCooldown(blue.cooldown);
                }
            }
        } else if (player.getInventory().getHeldItemSlot() == 4) {
            if (orange.getCurrentCooldown() == 0 && player.getLevel() >= orange.getEnergyCost()) {
                orange.onActivate(player);
                orange.setCurrentCooldown(orange.cooldown);

            }
        }
    }

    public void onRightClickHotKey(Player player, int slot) {
        if (slot == 0) {
            if (player.getLevel() >= weapon.getEnergyCost()) {
                weapon.onActivate(player);
            }
        } else if (slot == 1) {
            if (red.getCurrentCooldown() == 0 && player.getLevel() >= red.getEnergyCost()) {
                red.onActivate(player);
                if (!red.getName().contains("Chain") && !red.getName().contains("Link"))
                    red.setCurrentCooldown(red.cooldown);
            }
        } else if (slot == 2) {
            if (purple.getCurrentCooldown() == 0 && player.getLevel() >= purple.getEnergyCost()) {
                purple.onActivate(player);
                purple.setCurrentCooldown(purple.cooldown);
            }
        } else if (slot == 3) {
            if (blue.getCurrentCooldown() == 0 && player.getLevel() >= blue.getEnergyCost()) {
                blue.onActivate(player);
                if (!blue.getName().contains("Chain")) {
                    blue.setCurrentCooldown(blue.cooldown);
                }
            }
        } else if (slot == 4) {
            if (orange.getCurrentCooldown() == 0 && player.getLevel() >= orange.getEnergyCost()) {
                orange.onActivate(player);
                orange.setCurrentCooldown(orange.cooldown);

            }
        }
        player.getInventory().setHeldItemSlot(0);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    public int getEnergyPerSec() {
        return energyPerSec;
    }

    public void setEnergyPerSec(int energyPerSec) {
        this.energyPerSec = energyPerSec;
    }

    public int getEnergyOnHit() {
        return energyOnHit;
    }

    public void setEnergyOnHit(int energyOnHit) {
        this.energyOnHit = energyOnHit;
    }

    public int getDamageResistance() {
        return damageResistance;
    }

    public void setDamageResistance(int damageResistance) {
        this.damageResistance = damageResistance;
    }

    public AbstractAbility getWeapon() {
        return weapon;
    }

    public void setWeapon(AbstractAbility weapon) {
        this.weapon = weapon;
    }

    public AbstractAbility getRed() {
        return red;
    }

    public void setRed(AbstractAbility red) {
        this.red = red;
    }

    public AbstractAbility getPurple() {
        return purple;
    }

    public void setPurple(AbstractAbility purple) {
        this.purple = purple;
    }

    public AbstractAbility getBlue() {
        return blue;
    }

    public void setBlue(AbstractAbility blue) {
        this.blue = blue;
    }

    public AbstractAbility getOrange() {
        return orange;
    }

    public void setOrange(AbstractAbility orange) {
        this.orange = orange;
    }

    public String getClassName() {
        return className;
    }
}
