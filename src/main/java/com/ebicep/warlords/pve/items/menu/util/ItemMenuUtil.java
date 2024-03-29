package com.ebicep.warlords.pve.items.menu.util;

import com.ebicep.warlords.database.repositories.player.pojos.general.DatabasePlayer;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.player.general.Classes;
import com.ebicep.warlords.player.general.Specializations;
import com.ebicep.warlords.pve.PvEUtils;
import com.ebicep.warlords.pve.Spendable;
import com.ebicep.warlords.pve.items.ItemTier;
import com.ebicep.warlords.pve.items.addons.ItemAddonClassBonus;
import com.ebicep.warlords.pve.items.addons.ItemAddonSpecBonus;
import com.ebicep.warlords.pve.items.modifiers.ItemBucklerModifier;
import com.ebicep.warlords.pve.items.modifiers.ItemGauntletModifier;
import com.ebicep.warlords.pve.items.modifiers.ItemTomeModifier;
import com.ebicep.warlords.pve.items.statpool.BasicStatPool;
import com.ebicep.warlords.pve.items.types.AbstractItem;
import com.ebicep.warlords.pve.items.types.AbstractSpecialItem;
import com.ebicep.warlords.pve.items.types.BonusLore;
import com.ebicep.warlords.pve.items.types.ItemType;
import com.ebicep.warlords.pve.mobs.Aspect;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;
import java.util.function.BiConsumer;

public class ItemMenuUtil {

    public static Component getRequirementMetString(boolean requirementMet, String requirement) {
        return Component.textOfChildren(
                requirementMet ? Component.text("✔ ", NamedTextColor.GREEN) : Component.text("✖ ", NamedTextColor.RED),
                Component.text(requirement, NamedTextColor.GRAY)
        );
    }

    public static void addItemTierRequirement(
            Menu menu,
            ItemTier tier,
            AbstractItem item,
            int x,
            int y,
            BiConsumer<Menu, InventoryClickEvent> onClick
    ) {
        ItemBuilder itemBuilder;
        if (item == null) {
            itemBuilder = new ItemBuilder(tier.clayBlock)
                    .name(Component.text("Click to Select Item", NamedTextColor.GREEN));
        } else {
            itemBuilder = item.generateItemBuilder()
                              .addLore(
                                      Component.empty(),
                                      Component.textOfChildren(
                                              Component.text("CLICK", NamedTextColor.YELLOW, TextDecoration.BOLD),
                                              Component.text(" to swap this item", NamedTextColor.GREEN)
                                      )
                              );
        }
        menu.setItem(x, y,
                itemBuilder.get(),
                onClick
        );
        addPaneRequirement(menu, x + 1, y, item != null);
    }

    public static void addPaneRequirement(Menu menu, int x, int y, boolean requirementMet) {
        menu.setItem(x, y,
                new ItemBuilder(requirementMet ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE)
                        .name(Component.text(" "))
                        .get(),
                (m, e) -> {
                }
        );
    }

    public static void addSpendableCostRequirement(
            DatabasePlayer databasePlayer,
            Menu menu,
            LinkedHashMap<Spendable, Long> cost,
            int x,
            int y
    ) {
        List<Component> costLore = PvEUtils.getCostLore(cost, false);
        Component name = costLore.get(0);
        costLore.remove(0);
        menu.setItem(x, y,
                new ItemBuilder(Material.BOOK)
                        .name(name)
                        .lore(costLore)
                        .get(),
                (m, e) -> {
                }
        );
        boolean hasRequiredCosts = cost
                .entrySet()
                .stream()
                .allMatch(spendableLongEntry -> spendableLongEntry.getKey().getFromPlayer(databasePlayer) >= spendableLongEntry.getValue());
        menu.setItem(x + 1, y,
                new ItemBuilder(hasRequiredCosts ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE)
                        .name(Component.text(" "))
                        .get(),
                (m, e) -> {
                }
        );
    }

    public static void addItemConfirmation(
            Menu menu,
            Runnable onCenterClick
    ) {
        for (int i = 5; i < 8; i++) {
            for (int j = 1; j < 4; j++) {
                if (i == 6 && j == 2) {
                    onCenterClick.run();
                } else {
                    menu.setItem(i, j,
                            new ItemBuilder(Material.IRON_BARS)
                                    .name(Component.text(" "))
                                    .get(),
                            (m, e) -> {
                            }
                    );
                }
            }
        }
    }

    public static List<Component> getTotalBonusLore(List<AbstractItem> equippedItems) {
        List<Component> lore = new ArrayList<>();
        lore.addAll(getStatBonusLore(equippedItems));
        lore.addAll(getAspectBonusLore(equippedItems));
        lore.addAll(getSpecialBonusLore(equippedItems));
        return lore;
    }

    public static List<Component> getStatBonusLore(List<AbstractItem> equippedItems) {
        Map<BasicStatPool, Float> statPool = new HashMap<>();
        for (AbstractItem equippedItem : equippedItems) {
            equippedItem.getStatPool().forEach((stat, tier) -> statPool.merge(stat, tier, Float::sum));
        }
        List<Component> bonusLore = BasicStatPool.getStatPoolLore(statPool, Component.text("➤ ", NamedTextColor.AQUA), true, null);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Stat Bonuses:", NamedTextColor.AQUA));
        lore.addAll(bonusLore.isEmpty() ? Collections.singletonList(Component.text("None", NamedTextColor.GRAY)) : bonusLore);
        return lore;
    }

    public static List<Component> getAspectBonusLore(List<AbstractItem> equippedItems) {
        Map<Aspect, Map<ItemType, Integer>> aspectBonuses = new HashMap<>();
        for (AbstractItem equippedItem : equippedItems) {
            ItemType type = equippedItem.getType();
            Aspect aspectModifier1 = equippedItem.getAspectModifier1();
            Aspect aspectModifier2 = equippedItem.getAspectModifier2();
            if (aspectModifier1 != null) {
                ItemTier tier = equippedItem.getTier();
                if (aspectModifier2 != null) {
                    aspectBonuses.computeIfAbsent(aspectModifier1, k -> new HashMap<>())
                                 .merge(type, tier.aspectModifierValues.dualModifier1(), Integer::sum);
                    aspectBonuses.computeIfAbsent(aspectModifier2, k -> new HashMap<>())
                                 .merge(type, tier.aspectModifierValues.dualModifier2(), Integer::sum);
                } else {
                    aspectBonuses.computeIfAbsent(aspectModifier1, k -> new HashMap<>())
                                 .merge(type, tier.aspectModifierValues.singleModifier(), Integer::sum);
                }
            }
        }
        List<Component> bonusLore = new ArrayList<>();
        aspectBonuses.keySet()
                     .stream()
                     .sorted(Comparator.comparingInt(Enum::ordinal))
                     .forEachOrdered(aspect -> {
                         Map<ItemType, Integer> itemTypeBonuses = aspectBonuses.get(aspect);
                         bonusLore.add(Component.textOfChildren(
                                 Component.text("➤ ", NamedTextColor.AQUA),
                                 Component.text(aspect.name, aspect.textColor)
                         ));
                         itemTypeBonuses.keySet()
                                        .stream()
                                        .sorted(Comparator.comparingInt(Enum::ordinal))
                                        .forEachOrdered(itemType -> {
                                            bonusLore.add(Component.textOfChildren(
                                                    Component.text("   ➤ ", NamedTextColor.AQUA),
                                                    itemType.getModifierDescriptionCalculatedInverted(itemTypeBonuses.get(itemType))
                                            ));
                                        });
                     });
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Aspect Bonuses:", NamedTextColor.AQUA));
        lore.addAll(bonusLore.isEmpty() ? Collections.singletonList(Component.text("None", NamedTextColor.GRAY)) : bonusLore);
        return lore;
    }

    public static List<Component> getSpecialBonusLore(List<AbstractItem> equippedItems) {
        List<Component> bonusLore = new ArrayList<>();
        HashMap<String, LinkedHashSet<List<Component>>> bonuses = new HashMap<>();
        equippedItems.stream()
                     .filter(BonusLore.class::isInstance)
                     .filter(item -> ((BonusLore) item).getBonusLore() != null)
                     .forEach(item -> {
                         BonusLore bonus = (BonusLore) item;
                         if (item instanceof ItemAddonClassBonus classBonus) {
                             bonuses.computeIfAbsent(classBonus.getClasses().name, k -> new LinkedHashSet<>())
                                    .add(bonus.getBonusLore());
                             return;
                         }
                         if (item instanceof ItemAddonSpecBonus specBonus) {
                             LinkedHashSet<List<Component>> hashSet = bonuses.computeIfAbsent(specBonus.getSpec().name, k -> new LinkedHashSet<>());
                             if (item instanceof AbstractSpecialItem specialItem && specialItem.getTier() == ItemTier.GAMMA) {
                                 // first element needs to be filler bc its USUALLY "Bonus" and this way is omega scuffed
                                 List<Component> upgradeTreeBonusDescription = Arrays.asList(
                                         Component.empty(),
                                         Component.text(specialItem.getUpgradeTreeBonusDescription(1), NamedTextColor.GRAY)
                                 );
                                 boolean hadPreviously = hashSet.remove(upgradeTreeBonusDescription);
                                 if (hadPreviously) {
                                     hashSet.add(Arrays.asList(
                                             Component.empty(),
                                             Component.text(specialItem.getUpgradeTreeBonusDescription(2), NamedTextColor.GRAY)
                                     ));
                                 } else {
                                     hashSet.add(upgradeTreeBonusDescription);
                                 }
                             } else {
                                 hashSet.add(bonus.getBonusLore());
                             }
                             return;
                         }
                         bonuses.computeIfAbsent("General", k -> new LinkedHashSet<>()).add(bonus.getBonusLore());
                     });
        bonuses.entrySet()
               .stream()
               .sorted((o1, o2) -> {
                   //general first
                   if (o1.getKey().equals("General")) {
                       return -1;
                   } else if (o2.getKey().equals("General")) {
                       return 1;
                   }
                   //then class
                   Classes o1Class = Classes.getClassFromName(o1.getKey());
                   Classes o2Class = Classes.getClassFromName(o2.getKey());
                   if (o1Class != null && o2Class != null) {
                       return o1Class.compareTo(o2Class);
                   } else if (o1Class != null) {
                       return -1;
                   } else if (o2Class != null) {
                       return 1;
                   }
                   //then spec
                   Specializations o1Spec = Specializations.getSpecFromNameNullable(o1.getKey());
                   Specializations o2Spec = Specializations.getSpecFromNameNullable(o2.getKey());
                   if (o1Spec != null && o2Spec != null) {
                       return o1Spec.compareTo(o2Spec);
                   } else if (o1Spec != null) {
                       return -1;
                   } else if (o2Spec != null) {
                       return 1;
                   }
                   return 0;
               })
               .forEachOrdered(entry -> {
                   String category = entry.getKey();
                   LinkedHashSet<List<Component>> lists = entry.getValue();
                   bonusLore.add(Component.textOfChildren(
                           Component.text("➤ ", NamedTextColor.AQUA),
                           Component.text(category, NamedTextColor.GREEN)
                   ));
                   lists.forEach(bonusLores -> {
                       for (int i = 1; i < bonusLores.size(); i++) {
                           Component lore = bonusLores.get(i);
                           if (i == 1) {
                               bonusLore.add(Component.textOfChildren(
                                       Component.text("   "),
                                       Component.text("➤ ", NamedTextColor.AQUA),
                                       lore
                               ));
                           } else {
                               bonusLore.add(Component.text("      ").append(lore));
                           }
                       }
                   });
               });
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Special Bonuses:", NamedTextColor.AQUA));
        lore.addAll(bonusLore.isEmpty() ? Collections.singletonList(Component.text("None", NamedTextColor.GRAY)) : bonusLore);
        return lore;
    }


    @Deprecated
    public static List<Component> getTotalBonusLoreLegacy(List<AbstractItem> equippedItems, boolean skipFirstLine) {
        HashMap<BasicStatPool, Float> statPool = new HashMap<>();
        float gauntletModifier = 0;
        float tomeModifier = 0;
        float bucklerModifier = 0;
        for (AbstractItem equippedItem : equippedItems) {
            ItemType type = equippedItem.getType();
            equippedItem.getStatPool().forEach((stat, tier) -> statPool.merge(stat, tier, Float::sum));
            switch (type) {
                case GAUNTLET -> gauntletModifier += equippedItem.getModifierCalculated();
                case TOME -> tomeModifier += equippedItem.getModifierCalculated();
                case BUCKLER -> bucklerModifier += equippedItem.getModifierCalculated();
            }
        }
        List<Component> bonusLore = BasicStatPool.getStatPoolLore(statPool, Component.text("- ", NamedTextColor.AQUA), true, null);
        List<Component> blessCurseLore = new ArrayList<>();
        if (gauntletModifier != 0) {
            List<Component> lore = AbstractItem.getModifierCalculatedLore(
                    ItemGauntletModifier.Blessings.VALUES,
                    ItemGauntletModifier.Curses.VALUES,
                    gauntletModifier,
                    true
            );
            addBlessCurseLore(blessCurseLore, lore);
        }
        if (tomeModifier != 0) {
            List<Component> lore = AbstractItem.getModifierCalculatedLore(
                    ItemTomeModifier.Blessings.VALUES,
                    ItemTomeModifier.Curses.VALUES,
                    tomeModifier,
                    true
            );
            addBlessCurseLore(blessCurseLore, lore);
        }
        if (bucklerModifier != 0) {
            List<Component> lore = AbstractItem.getModifierCalculatedLore(
                    ItemBucklerModifier.Blessings.VALUES,
                    ItemBucklerModifier.Curses.VALUES,
                    bucklerModifier,
                    true
            );
            addBlessCurseLore(blessCurseLore, lore);
        }
        if (!blessCurseLore.isEmpty()) {
            bonusLore.add(Component.text("Blessings/Curses:", NamedTextColor.AQUA));
            bonusLore.addAll(blessCurseLore);
        }
        HashMap<Classes, LinkedHashSet<List<Component>>> bonuses = new HashMap<>();
        equippedItems.stream()
                     .sorted(Comparator.comparingInt(o -> o.getTier().ordinal()))
                     .filter(BonusLore.class::isInstance)
                     .filter(item -> ((BonusLore) item).getBonusLore() != null)
                     .forEach(item -> {
                         BonusLore bonus = (BonusLore) item;
                         if (item instanceof ItemAddonClassBonus classBonus) {
                             bonuses.computeIfAbsent(classBonus.getClasses(), k -> new LinkedHashSet<>()).add(bonus.getBonusLore());
                         } else {
                             bonuses.computeIfAbsent(null, k -> new LinkedHashSet<>()).add(bonus.getBonusLore());
                         }
                     });
        if (!bonuses.isEmpty()) {
            bonusLore.add(Component.text("Special Bonuses:", NamedTextColor.AQUA));
            bonuses.entrySet()
                   .stream()
                   .sorted((o1, o2) -> {
                       if (o1.getKey() == null) {
                           return -1;
                       } else if (o2.getKey() == null) {
                           return 1;
                       } else {
                           return o1.getKey().compareTo(o2.getKey());
                       }
                   })
                   .forEachOrdered(entry -> {
                       Classes classes = entry.getKey();
                       LinkedHashSet<List<Component>> lists = entry.getValue();
                       bonusLore.add(Component.textOfChildren(
                               Component.text("- ", NamedTextColor.AQUA),
                               Component.text(classes == null ? "General" : classes.name, NamedTextColor.GREEN)
                       ));
                       if (classes == null) {
                           lists.forEach(bonusLores -> {
                               for (int i = 1; i < bonusLores.size(); i++) {
                                   Component lore = bonusLores.get(i);
                                   if (i == 1) {
                                       bonusLore.add(Component.textOfChildren(
                                               Component.text("    "),
                                               Component.text("- ", NamedTextColor.AQUA),
                                               lore
                                       ));
                                   } else {
                                       bonusLore.add(Component.text("       ").append(lore));
                                   }
                               }
                           });
                       } else {
                           lists.forEach(bonusLores -> {
                               for (int i = 1; i < bonusLores.size(); i++) {
                                   Component lore = bonusLores.get(i);
                                   if (i == 1) {
                                       bonusLore.add(Component.textOfChildren(
                                               Component.text("    "),
                                               Component.text("- ", NamedTextColor.AQUA),
                                               lore
                                       ));
                                   } else {
                                       bonusLore.add(Component.text("       ").append(lore));
                                   }
                               }
                           });
                       }
                   });
        }
        List<Component> lore = new ArrayList<>();
        if (!skipFirstLine) {
            lore.add(Component.text("Stat Bonuses:", NamedTextColor.AQUA));
        }
        lore.addAll(bonusLore.isEmpty() ? Collections.singletonList(Component.text("None", NamedTextColor.GRAY)) : bonusLore);
        return lore;
    }

    private static void addBlessCurseLore(List<Component> blessCurseLore, List<Component> lore) {
        for (int i = 0; i < lore.size(); i++) {
            Component component = lore.get(i);
            if (i == 0) {
                blessCurseLore.add(Component.textOfChildren(
                        Component.text("- ", NamedTextColor.AQUA),
                        component
                ));
            } else {
                blessCurseLore.add(component);
            }
        }
    }
}
