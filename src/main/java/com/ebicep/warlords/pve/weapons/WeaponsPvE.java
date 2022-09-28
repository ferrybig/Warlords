package com.ebicep.warlords.pve.weapons;

import com.ebicep.warlords.database.repositories.masterworksfair.pojos.MasterworksFair;
import com.ebicep.warlords.database.repositories.masterworksfair.pojos.MasterworksFairPlayerEntry;
import com.ebicep.warlords.pve.Currencies;
import com.ebicep.warlords.pve.weapons.weapontypes.CommonWeapon;
import com.ebicep.warlords.pve.weapons.weapontypes.EpicWeapon;
import com.ebicep.warlords.pve.weapons.weapontypes.RareWeapon;
import com.ebicep.warlords.pve.weapons.weapontypes.legendaries.AbstractLegendaryWeapon;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;

public enum WeaponsPvE {

    NONE("None",
            null,
            ChatColor.GRAY,
            null,
            null,
            null,
            0
    ),
    COMMON("Common",
            CommonWeapon.class,
            ChatColor.GREEN,
            new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5),
            MasterworksFair::getCommonPlayerEntries,
            Currencies.COMMON_STAR_PIECE,
            5
    ),
    RARE("Rare",
            RareWeapon.class,
            ChatColor.BLUE,
            new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 3),
            MasterworksFair::getRarePlayerEntries,
            Currencies.RARE_STAR_PIECE,
            10
    ),
    EPIC("Epic",
            EpicWeapon.class,
            ChatColor.DARK_PURPLE,
            new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2),
            MasterworksFair::getEpicPlayerEntries,
            Currencies.EPIC_STAR_PIECE,
            15
    ),
    LEGENDARY("Legendary",
            AbstractLegendaryWeapon.class,
            ChatColor.GOLD,
            new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 1),
            null,
            Currencies.LEGENDARY_STAR_PIECE,
            20
    );

    public static final WeaponsPvE[] VALUES = values();
    public final String name;
    public final Class<?> weaponClass;
    public final ChatColor chatColor;
    public final ItemStack glassItem;
    public final Function<MasterworksFair, List<MasterworksFairPlayerEntry>> getPlayerEntries;
    public final Currencies starPieceCurrency;
    public final int fairyEssenceCost;

    WeaponsPvE(
            String name,
            Class<?> weaponClass,
            ChatColor chatColor,
            ItemStack glassItem,
            Function<MasterworksFair, List<MasterworksFairPlayerEntry>> getPlayerEntries,
            Currencies starPieceCurrency,
            int fairyEssenceCost
    ) {
        this.weaponClass = weaponClass;
        this.chatColor = chatColor;
        this.name = name;
        this.getPlayerEntries = getPlayerEntries;
        this.glassItem = glassItem;
        this.starPieceCurrency = starPieceCurrency;
        this.fairyEssenceCost = fairyEssenceCost;
    }

    public WeaponsPvE next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

    public String getChatColorName() {
        return chatColor.toString() + name;
    }
}
