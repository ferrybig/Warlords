package com.ebicep.warlords.pve.weapons.menu;

import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabasePlayer;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.menu.PlayerHotBarItemListener;
import com.ebicep.warlords.player.general.Classes;
import com.ebicep.warlords.player.general.Specializations;
import com.ebicep.warlords.pve.weapons.AbstractWeapon;
import com.ebicep.warlords.pve.weapons.weapontypes.StarterWeapon;
import com.ebicep.warlords.util.bukkit.ComponentBuilder;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import com.ebicep.warlords.util.java.Pair;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class WeaponBindMenu {

    private static final HashMap<Classes, Pair<Integer, Integer>> CLASSES_MENU_LOCATION = new HashMap<>() {{
        put(Classes.MAGE, new Pair<>(1, 1));
        put(Classes.WARRIOR, new Pair<>(4, 1));
        put(Classes.PALADIN, new Pair<>(7, 1));
        put(Classes.SHAMAN, new Pair<>(2, 3));
        put(Classes.ROGUE, new Pair<>(6, 3));
    }};

    public static void openWeaponBindMenu(Player player, DatabasePlayer databasePlayer, AbstractWeapon selectedWeapon) {
        List<AbstractWeapon> weaponInventory = databasePlayer.getPveStats().getWeaponInventory();
        BidiMap<AbstractWeapon, Specializations> boundWeapons = new DualHashBidiMap<>();
        weaponInventory.stream()
                .filter(AbstractWeapon::isBound)
                .forEach(w -> boundWeapons.put(w, w.getSpecializations()));
        Specializations weaponSpec = selectedWeapon.getSpecializations();

        Menu menu = new Menu("Bind Weapons", 9 * 6);

        menu.setItem(
                4,
                0,
                selectedWeapon.generateItemStack(),
                (m, e) -> {
                }
        );

        for (Classes value : Classes.VALUES) {
            Pair<Integer, Integer> menuLocation = CLASSES_MENU_LOCATION.get(value);

            int column = menuLocation.getA();
            int row = menuLocation.getB();
            menu.setItem(
                    column,
                    row,
                    new ItemBuilder(value.item)
                            .name(ChatColor.GREEN + value.name)
                            .get(),
                    (m, e) -> {
                    }
            );

            List<Specializations> specializations = value.subclasses;
            for (int i = -1; i < 2; i++) {
                Specializations spec = specializations.get(i + 1);
                AbstractWeapon boundWeapon = boundWeapons.getKey(spec);
                if (boundWeapon != null) {
                    // Same spec bound
                    if (boundWeapon.getSpecializations() == weaponSpec && boundWeapon != selectedWeapon) {
                        menu.setItem(
                                column + i,
                                row + 1,
                                boundWeapon.generateItemStackInLore(ChatColor.GREEN + "Click to replace binding")
                                        .enchant(Enchantment.OXYGEN, 1)
                                        .get(),
                                (m, e) -> {
                                    boundWeapon.setBound(false);
                                    selectedWeapon.setBound(true);

                                    player.spigot().sendMessage(
                                            new ComponentBuilder(ChatColor.AQUA + "You unbounded ")
                                                    .appendHoverItem(boundWeapon.getName(), boundWeapon.generateItemStack())
                                                    .append(ChatColor.AQUA + " and bound ")
                                                    .appendHoverItem(selectedWeapon.getName(), selectedWeapon.generateItemStack())
                                                    .create()
                                    );

                                    //remove unbounded starter weapon as it is no longer needed
                                    if (boundWeapon instanceof StarterWeapon) {
                                        weaponInventory.remove(boundWeapon);
                                    }

                                    DatabaseManager.queueUpdatePlayerAsync(databasePlayer);
                                    openWeaponBindMenu(player, databasePlayer, selectedWeapon);
                                    PlayerHotBarItemListener.updateWeaponManagerItem(player);
                                }
                        );
                    } else {
                        menu.setItem(
                                column + i,
                                row + 1,
                                boundWeapon.generateItemStack(),
                                (m, e) -> {
                                }
                        );
                    }
                } else {
                    menu.setItem(
                            column + i,
                            row + 1,
                            spec == weaponSpec ?
                                    new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 13)
                                            .name(ChatColor.GREEN + "Click to bind")
                                            .get() :
                                    new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 14)
                                            .name(ChatColor.RED + "You cannot bind this weapon to " + spec.name)
                                            .get(),
                            (m, e) -> {
                                if (spec == weaponSpec) {
                                    //bind the new weapon
                                    selectedWeapon.setBound(true);
                                    DatabaseManager.queueUpdatePlayerAsync(databasePlayer);
                                    openWeaponBindMenu(player, databasePlayer, selectedWeapon);

                                    player.spigot().sendMessage(
                                            new ComponentBuilder(ChatColor.AQUA + "You bound ")
                                                    .appendHoverItem(selectedWeapon.getName(), selectedWeapon.generateItemStack())
                                                    .create()
                                    );
                                }
                            }
                    );
                }
            }
        }

        menu.setItem(4, 5, Menu.MENU_BACK, (m, e) -> WeaponManagerMenu.openWeaponEditor(player, databasePlayer, selectedWeapon));
        menu.openForPlayer(player);
    }
}
