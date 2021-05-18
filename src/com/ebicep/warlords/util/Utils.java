package com.ebicep.warlords.util;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.WarlordsPlayer;
import com.ebicep.warlords.classes.abilties.Totem;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static boolean getLookingAt(Player player, Player player1) {
        Location eye = player.getEyeLocation();
        eye.setY(eye.getY() + 0.7);
        Vector toEntity = player1.getEyeLocation().toVector().subtract(eye.toVector());
        float dot = (float) toEntity.normalize().dot(eye.getDirection());

        return dot > 0.91D;
    }

    public static boolean totemDownAndClose(WarlordsPlayer warlordsPlayer, Player player) {
        for (Entity entity : player.getNearbyEntities(5, 3, 5)) {
            if (entity instanceof ArmorStand && entity.hasMetadata("Capacitor Totem - " + warlordsPlayer.getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean lookingAtTotem(Player player) {
        Location eye = player.getEyeLocation();
        eye.setY(eye.getY() + .5);
        for (Entity entity : player.getNearbyEntities(20, 17, 20)) {
            if (entity instanceof ArmorStand && entity.hasMetadata("Capacitor Totem - " + player.getName())) {
                Vector toEntity = ((ArmorStand) entity).getEyeLocation().toVector().subtract(eye.toVector());
                float dot = (float) toEntity.normalize().dot(eye.getDirection());
                return dot > .98f;
            }
        }
        return false;
    }

    public static ArmorStand getTotem(Player player) {
        for (Entity entity : player.getNearbyEntities(20, 17, 20)) {
            if (entity instanceof ArmorStand && entity.hasMetadata("Capacitor Totem - " + player.getName())) {
                return (ArmorStand) entity;
            }
        }
        return null;
    }

    public static class ArmorStandComparator implements Comparator<Entity> {
        @Override
        public int compare(Entity a, Entity b) {
            return a instanceof ArmorStand && b instanceof ArmorStand ? 0 : a instanceof ArmorStand ? -1 : b instanceof ArmorStand ? 1 : 0;
        }
    }

    public static List<Entity> filterOutTeammates(List<Entity> entities, Player player) {
        entities.remove(player);
        return entities.stream().filter(entity -> !(entity instanceof Player) || !Warlords.getInstance().game.onSameTeam((Player) entity, player)).collect(Collectors.toList());
    }

    public static List<Entity> filterOnlyTeammates(List<Entity> entities, Player player) {
        return entities.stream().filter(entity -> !(entity instanceof Player) || Warlords.getInstance().game.onSameTeam((Player) entity, player)).collect(Collectors.toList());
    }

    public static Vector getRightDirection(Location location) {
        Vector direction = location.getDirection().normalize();
        return new Vector(-direction.getZ(), 0.0, direction.getX()).normalize();
    }

    public static Vector getLeftDirection(Location location) {
        Vector direction = location.getDirection().normalize();
        return new Vector(direction.getZ(), 0.0, -direction.getX()).normalize();
    }

    public static double getDistance(Entity e, double accuracy) {
        Location loc = e.getLocation().clone(); // Using .clone so you aren't messing with the direct location object from the entity
        double distance = 0; // Shouldn't start at -2 unless you're wanting the eye height from the ground (I don't know why you'd want that)
        for (double i = loc.getY(); i >= 0; i -= accuracy) {
            loc.setY(i);
            distance += accuracy;
            if (loc.getBlock().getType().isSolid()) // Makes a little more sense than checking if it's air
                break;
        }
        return distance;
    }

    /**
     * Utility message for sending version independent actionbar messages as to be able to
     * support versions from 1.8 and up without having to disable a simple feature such as this.
     *
     * @param player  the recipient of the actionbar message.
     * @param message the message to send. If it is empty ("") the actionbar is cleared.
     */
    public static void sendActionbar(Player player, String message) {
        if (player == null || message == null) return;
        String nmsVersion = Bukkit.getServer().getClass().getPackage().getName();
        nmsVersion = nmsVersion.substring(nmsVersion.lastIndexOf(".") + 1);

        //1.8.x and 1.9.x
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);

            Class<?> ppoc = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat");
            Class<?> packet = Class.forName("net.minecraft.server." + nmsVersion + ".Packet");
            Object packetPlayOutChat;
            Class<?> chat = Class.forName("net.minecraft.server." + nmsVersion + (nmsVersion.equalsIgnoreCase("v1_8_R1") ? ".ChatSerializer" : ".ChatComponentText"));
            Class<?> chatBaseComponent = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");

            Method method = null;
            if (nmsVersion.equalsIgnoreCase("v1_8_R1")) method = chat.getDeclaredMethod("a", String.class);

            Object object = nmsVersion.equalsIgnoreCase("v1_8_R1") ? chatBaseComponent.cast(method.invoke(chat, "{'text': '" + message + "'}")) : chat.getConstructor(new Class[]{String.class}).newInstance(message);
            packetPlayOutChat = ppoc.getConstructor(new Class[]{chatBaseComponent, Byte.TYPE}).newInstance(object, (byte) 2);

            Method handle = craftPlayerClass.getDeclaredMethod("getHandle");
            Object iCraftPlayer = handle.invoke(craftPlayer);
            Field playerConnectionField = iCraftPlayer.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(iCraftPlayer);
            Method sendPacket = playerConnection.getClass().getDeclaredMethod("sendPacket", packet);
            sendPacket.invoke(playerConnection, packetPlayOutChat);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
