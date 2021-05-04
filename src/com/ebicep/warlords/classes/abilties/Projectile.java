package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.classes.AbstractAbility;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Projectile extends AbstractAbility {

    private int maxDistance;

    public Projectile(String name, int minDamageHeal, int maxDamageHeal, int cooldown, int energyCost, int critChance, int critMultiplier, String description, int maxDistance) {
        super(name, minDamageHeal, maxDamageHeal, cooldown, energyCost, critChance, critMultiplier, description);
        this.maxDistance = maxDistance;
    }

    @Override
    public void onActivate(Player player) {
        CustomProjectile customProjectile = new CustomProjectile(player, player.getLocation(), player.getLocation(), player.getLocation().getDirection(), maxDistance, this);
        Warlords.getCustomProjectiles().add(customProjectile);
    }

    public static class CustomProjectile {

        private Player shooter;
        private Location startingLocation;
        private Location currentLocation;
        private Vector direction;
        private int maxDistance;
        private Projectile projectile;

        public CustomProjectile(Player shooter, Location startingLocation, Location currentLocation, Vector direction, int maxDistance, Projectile projectile) {
            this.shooter = shooter;
            this.startingLocation = startingLocation;
            this.currentLocation = currentLocation;
            this.direction = direction;
            this.maxDistance = maxDistance;
            this.projectile = projectile;
        }

        public Player getShooter() {
            return shooter;
        }

        public void setShooter(Player shooter) {
            this.shooter = shooter;
        }

        public Location getStartingLocation() {
            return startingLocation;
        }

        public void setStartingLocation(Location startingLocation) {
            this.startingLocation = startingLocation;
        }

        public Location getCurrentLocation() {
            return currentLocation;
        }

        public void setCurrentLocation(Location currentLocation) {
            this.currentLocation = currentLocation;
        }

        public Vector getDirection() {
            return direction;
        }

        public void setDirection(Vector direction) {
            this.direction = direction;
        }

        public int getMaxDistance() {
            return maxDistance;
        }

        public void setMaxDistance(int maxDistance) {
            this.maxDistance = maxDistance;
        }

        public Projectile getBall() {
            return projectile;
        }

        public void setBall(Projectile projectile) {
            this.projectile = projectile;
        }
    }
}