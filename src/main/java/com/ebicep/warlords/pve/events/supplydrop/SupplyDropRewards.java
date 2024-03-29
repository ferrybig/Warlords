package com.ebicep.warlords.pve.events.supplydrop;

import com.ebicep.warlords.database.repositories.player.pojos.pve.DatabasePlayerPvE;
import com.ebicep.warlords.pve.Currencies;
import com.ebicep.warlords.pve.weapons.WeaponsPvE;
import com.ebicep.warlords.util.java.RandomCollection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public enum SupplyDropRewards {

    SYNTHETIC_SHARDS_3("3 Synthetic Shards",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.SYNTHETIC_SHARD, 3),
            1500,
            WeaponsPvE.COMMON
    ),
    SYNTHETIC_SHARDS_5("5 Synthetic Shards",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.SYNTHETIC_SHARD, 5),
            2000,
            WeaponsPvE.COMMON
    ),
    SYNTHETIC_SHARDS_10("10 Synthetic Shards",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.SYNTHETIC_SHARD, 10),
            1000,
            WeaponsPvE.COMMON
    ),
    SYNTHETIC_SHARDS_20("20 Synthetic Shards",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.SYNTHETIC_SHARD, 20),
            500,
            WeaponsPvE.RARE
    ),
    SYNTHETIC_SHARDS_50("50 Synthetic Shards",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.SYNTHETIC_SHARD, 50),
            200,
            WeaponsPvE.EPIC
    ),
    COMMON_STAR_PIECE("Common Star Piece",
            databasePlayerPvE -> databasePlayerPvE.addOneCurrency(Currencies.COMMON_STAR_PIECE),
            100,
            WeaponsPvE.COMMON
    ) {
        @Override
        public Component getDropMessage() {
            return getStarPieceDropMessage();
        }
    },
    RARE_STAR_PIECE("Rare Star Piece",
            databasePlayerPvE -> databasePlayerPvE.addOneCurrency(Currencies.RARE_STAR_PIECE),
            10,
            WeaponsPvE.RARE
    ) {
        @Override
        public Component getDropMessage() {
            return getStarPieceDropMessage();
        }
    },
    EPIC_STAR_PIECE("Epic Star Piece",
            databasePlayerPvE -> databasePlayerPvE.addOneCurrency(Currencies.EPIC_STAR_PIECE),
            1,
            WeaponsPvE.EPIC
    ) {
        @Override
        public Component getDropMessage() {
            return getStarPieceDropMessage();
        }
    },
    SKILL_BOOST_MODIFIER("Skill Boost Modifier",
            databasePlayerPvE -> databasePlayerPvE.addOneCurrency(Currencies.SKILL_BOOST_MODIFIER),
            10,
            WeaponsPvE.EPIC
    ),
    COINS_1000("1,000 Coins",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.COIN, 1000),
            1000,
            WeaponsPvE.COMMON
    ),
    COINS_2000("2,000 Coins",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.COIN, 2000),
            1500,
            WeaponsPvE.COMMON
    ),
    COINS_5000("5,000 Coins",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.COIN, 5000),
            1000,
            WeaponsPvE.COMMON
    ),
    COINS_10000("10,000 Coins",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.COIN, 10000),
            500,
            WeaponsPvE.COMMON
    ),
    COINS_50000("50,000 Coins",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.COIN, 50000),
            200,
            WeaponsPvE.RARE
    ),
    COINS_100000("100,000 Coins",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.COIN, 100000),
            100,
            WeaponsPvE.EPIC
    ),
    FAIRY_ESSENCE_20("20 Fairy Essence",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.FAIRY_ESSENCE, 20),
            500,
            WeaponsPvE.RARE
    ),
    FAIRY_ESSENCE_40("40 Fairy Essence",
            databasePlayerPvE -> databasePlayerPvE.addCurrency(Currencies.FAIRY_ESSENCE, 40),
            200,
            WeaponsPvE.RARE
    ),

    ;

    public static final RandomCollection<SupplyDropRewards> RANDOM_COLLECTION = new RandomCollection<>();

    static {
        for (SupplyDropRewards supplyDropRewards : values()) {
            RANDOM_COLLECTION.add(supplyDropRewards.dropChance, supplyDropRewards);
        }
    }

    public final String name;
    public final Consumer<DatabasePlayerPvE> giveReward;
    public final int dropChance;
    public final WeaponsPvE rarity; //using for convenience

    SupplyDropRewards(String name, Consumer<DatabasePlayerPvE> giveReward, int dropChance, WeaponsPvE rarity) {
        this.name = name;
        this.giveReward = giveReward;
        this.dropChance = dropChance;
        this.rarity = rarity;
    }

    public static SupplyDropRewards getRandomReward() {
        return RANDOM_COLLECTION.next();
    }

    public Component getDropMessage() {
        return Component.text("You received ", NamedTextColor.GRAY)
                        .append(Component.text(name, getTextColor()))
                        .append(Component.text(" from the supply drop."));
    }

    public NamedTextColor getTextColor() {
        return rarity.textColor;
    }

    protected Component getStarPieceDropMessage() {
        return Component.text("A ", NamedTextColor.GRAY)
                        .append(Component.text(getType() + " Star Piece ", getTextColor()))
                        .append(Component.text("has been bestowed upon you."));
    }

    public String getType() {
        return rarity.name;
    }

    public void givePlayerRewardTitle(Player player) {
        player.showTitle(Title.title(
                Component.text(getType().toUpperCase() + "!", getTextColor()),
                Component.text(name, NamedTextColor.GOLD),
                Title.Times.times(Ticks.duration(0), Ticks.duration(40), Ticks.duration(0))
        ));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.2f);
    }
}
