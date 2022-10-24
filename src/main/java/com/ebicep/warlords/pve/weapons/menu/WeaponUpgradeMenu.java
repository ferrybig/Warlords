package com.ebicep.warlords.pve.weapons.menu;

import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabasePlayer;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.pve.Currencies;
import com.ebicep.warlords.pve.weapons.AbstractWeapon;
import com.ebicep.warlords.pve.weapons.weaponaddons.Upgradeable;
import com.ebicep.warlords.util.bukkit.ComponentBuilder;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class WeaponUpgradeMenu {

    public static <T extends AbstractWeapon & Upgradeable> void openWeaponUpgradeMenu(Player player, DatabasePlayer databasePlayer, T weapon) {
        if (weapon == null) {
            return;
        }

        Menu menu = new Menu("Upgrade Weapon", 9 * 3);

        menu.setItem(2, 1,
                weapon.getUpgradeItem(),
                (m, e) -> {
                    upgradeWeapon(player, databasePlayer, weapon);
                    WeaponManagerMenu.openWeaponEditor(player, databasePlayer, weapon);
                }
        );

        menu.setItem(4, 1,
                weapon.generateItemStack(),
                (m, e) -> {
                }
        );

        menu.setItem(6, 1,
                new ItemBuilder(Material.STAINED_CLAY, 1, (short) 14)
                        .name(ChatColor.RED + "Deny")
                        .lore(ChatColor.GRAY + "Go back.")
                        .get(),
                (m, e) -> WeaponManagerMenu.openWeaponEditor(player, databasePlayer, weapon)
        );

        menu.openForPlayer(player);
    }

    public static <T extends AbstractWeapon & Upgradeable> void upgradeWeapon(Player player, DatabasePlayer databasePlayer, T weapon) {
        if (weapon == null) {
            return;
        }
        if (databasePlayer.getPveStats().getWeaponInventory().contains(weapon)) {
            LinkedHashMap<Currencies, Long> upgradeCost = weapon.getUpgradeCost(weapon.getUpgradeLevel() + 1);
            for (Map.Entry<Currencies, Long> currenciesLongEntry : upgradeCost.entrySet()) {
                databasePlayer.getPveStats().subtractCurrency(currenciesLongEntry.getKey(), currenciesLongEntry.getValue());
            }
            weapon.upgrade();
            DatabaseManager.queueUpdatePlayerAsync(databasePlayer);

            player.spigot().sendMessage(
                    new ComponentBuilder(ChatColor.GRAY + "Upgraded Weapon: ")
                            .appendHoverItem(weapon.getName(), weapon.generateItemStack())
                            .create()
            );
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 500, 2);
        }

    }

}
