package com.ebicep.warlords.pve.weapons.weapontypes.legendaries;

import com.ebicep.warlords.pve.Currencies;
import com.ebicep.warlords.pve.weapons.weapontypes.legendaries.titles.*;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.function.Function;

public enum LegendaryTitles {

    TITANIC("Titanic", LegendaryTitanic.class, LegendaryTitanic::new, LegendaryTitanic::new, 0),
    VIGOROUS("Vigorous", LegendaryVigorous.class, LegendaryVigorous::new, LegendaryVigorous::new, 1),
    SUSPICIOUS("Suspicious", LegendarySuspicious.class, LegendarySuspicious::new, LegendarySuspicious::new, 2),
    BENEVOLENT("Benevolent", LegendaryBenevolent.class, LegendaryBenevolent::new, LegendaryBenevolent::new, 3),
    VORPAL("Vorpal", LegendaryVorpal.class, LegendaryVorpal::new, LegendaryVorpal::new, 4),
    DIVINE("Divine", LegendaryDivine.class, LegendaryDivine::new, LegendaryDivine::new, 5),
    GALE("Gale", LegendaryGale.class, LegendaryGale::new, LegendaryGale::new, 6),

    ;


    public static final LegendaryTitles[] VALUES = values();

    public final String title;
    public final Class<?> clazz;
    public final Function<UUID, AbstractLegendaryWeapon> create;
    public final Function<AbstractLegendaryWeapon, AbstractLegendaryWeapon> titleWeapon;
    public final int color;

    LegendaryTitles(
            String title,
            Class<?> clazz,
            Function<UUID, AbstractLegendaryWeapon> create,
            Function<AbstractLegendaryWeapon, AbstractLegendaryWeapon> titleWeapon,
            int color
    ) {
        this.title = title;
        this.clazz = clazz;
        this.create = create;
        this.titleWeapon = titleWeapon;
        this.color = color;
    }

    public LinkedHashMap<Currencies, Long> getCost() {
        return new LinkedHashMap<>() {{
            put(Currencies.COIN, 50000L);
            put(Currencies.SYNTHETIC_SHARD, 1000L);
        }};
    }

    public List<String> getCostLore() {
        Set<Map.Entry<Currencies, Long>> cost = getCost().entrySet();

        List<String> loreCost = new ArrayList<>();
        loreCost.add("");
        loreCost.add(ChatColor.AQUA + "Title Cost: ");
        for (Map.Entry<Currencies, Long> currenciesLongEntry : cost) {
            loreCost.add(ChatColor.GRAY + " - " + currenciesLongEntry.getKey().getCostColoredName(currenciesLongEntry.getValue()));
        }
        return loreCost;
    }

}
