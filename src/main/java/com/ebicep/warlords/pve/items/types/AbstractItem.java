package com.ebicep.warlords.pve.items.types;

import com.ebicep.warlords.pve.items.ItemTier;
import com.ebicep.warlords.pve.items.modifiers.ItemModifier;
import com.ebicep.warlords.pve.items.statpool.ItemStatPool;
import com.ebicep.warlords.util.bukkit.ComponentBuilder;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import com.ebicep.warlords.util.bukkit.WordWrap;
import com.ebicep.warlords.util.java.NumberFormat;
import com.ebicep.warlords.util.java.RandomCollection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractItem<
        R extends Enum<R> & ItemModifier<R>,
        U extends Enum<U> & ItemModifier<U>> {

    public static void sendItemMessage(Player player, String message) {
        player.sendMessage(ChatColor.RED + "Items" + ChatColor.DARK_GRAY + " > " + message);
    }

    public static void sendItemMessage(Player player, ComponentBuilder message) {
        player.spigot().sendMessage(message.prependAndCreate(new ComponentBuilder(ChatColor.RED + "Items" + ChatColor.DARK_GRAY + " > ").create()));
    }

    private static double getAverageValue(double min, double max, double current) {
        return (current - min) / (max - min);
    }

    protected UUID uuid = UUID.randomUUID();
    @Field("obtained_date")
    protected Instant obtainedDate = Instant.now();
    protected ItemTier tier;
    @Field("stat_pool")
    protected Map<ItemStatPool, Float> statPoolDistribution = new HashMap<>();
    @Transient
    protected Map<ItemStatPool, Integer> statPoolValues;
    protected int modifier;

    public AbstractItem() {
    }

    public AbstractItem(ItemTier tier) {
        this(tier, tier.generateStatPool());
    }

    public AbstractItem(ItemTier tier, Set<ItemStatPool> statPool) {
        this.tier = tier;
        for (ItemStatPool stat : statPool) {
            this.statPoolDistribution.put(stat, (float) getRandomValueNormalDistribution());
        }
        if (tier != ItemTier.DELTA && tier != ItemTier.OMEGA) {
            bless(null);
        }
    }

    private static double getRandomValueNormalDistribution() {
        double mean = 0.5;
        double stdDev = 0.15;
        double random = ThreadLocalRandom.current().nextGaussian() * stdDev + mean;
        // Clamp to [0, 1]
        return Math.max(0, Math.min(1, random));
    }

    /**
     * Ran when the item is first created or found blessing applied
     * <p>
     * Either blesses/curses/does nothing to the item based on the tier
     * <p>
     * Based on bless/curse this will generate a random tier
     * <p>
     * Random tier generated adds to current modifier (blessing is positive, curse is negative)
     */
    public void bless(Integer tier) {
        Integer result = new RandomCollection<Integer>()
                .add(this.tier.blessedChance, 1)
                .add(this.tier.cursedChance, -1)
                .add(1 - this.tier.blessedChance - this.tier.cursedChance, 0)
                .next();
        switch (result) {
            case 1:
                this.modifier = Math.min(this.modifier + (tier == null ? ItemModifier.GENERATE_BLESSING.next() : tier), 5);
                break;
            case -1:
                this.modifier = Math.max(this.modifier - (tier == null ? ItemModifier.GENERATE_CURSE.next() : tier), -5);
                break;
        }
    }

    public abstract AbstractItem<R, U> clone();

    public void copyFrom(AbstractItem<R, U> item) {
        this.uuid = item.uuid;
        this.obtainedDate = item.obtainedDate;
        this.tier = item.tier;
        this.statPoolDistribution = new HashMap<>(item.statPoolDistribution);
        this.modifier = item.modifier;
    }

    public ItemStack generateItemStack() {
        return generateItemBuilder().get();
    }

    public ItemBuilder generateItemBuilder() {
        ItemBuilder itemBuilder = new ItemBuilder(Material.SKULL_ITEM)
                .name(getName())
                .lore(
                        ChatColor.GRAY + "Tier: " + tier.getColoredName(),
                        ""
                );
        itemBuilder.addLore(getStatPoolLore());
        if (modifier != 0) {
            itemBuilder.addLore(
                    "",
                    getModifierCalculatedLore(getBlessings(), getCurses(), getModifierCalculated())
            );
        }
        itemBuilder.addLore(
                "",
                getItemScoreString(),
                "",
                getWeightString()
        );
        return itemBuilder;
    }

    public String getName() {
        String name = "";
        if (modifier != 0) {
            if (modifier > 0) {
                name += ChatColor.GREEN + getBlessings()[modifier - 1].getName() + " ";
            } else {
                name += ChatColor.RED + getCurses()[-modifier - 1].getName() + " ";
            }
        } else {
            name += ChatColor.GRAY + "Normal ";
        }
        name += getType().name;
        return name;
    }

    public List<String> getStatPoolLore() {
        return getStatPoolLore(getStatPool());
    }

    public static <R extends Enum<R> & ItemModifier<R>, U extends Enum<U> & ItemModifier<U>> String getModifierCalculatedLore(
            R[] blessings,
            U[] curses,
            float modifierCalculated
    ) {
        if (modifierCalculated > 0) {
            R blessing = blessings[0];
            return WordWrap.wrapWithNewline(blessing.getDescriptionCalculated(modifierCalculated), 150);
        } else {
            U curse = curses[0];
            return WordWrap.wrapWithNewline(curse.getDescriptionCalculated(modifierCalculated), 150);
        }
    }

    public abstract R[] getBlessings();

    public abstract U[] getCurses();

    public float getModifierCalculated() {
        if (modifier == 0) {
            return 0;
        }
        if (modifier > 0) {
            return modifier * getBlessings()[modifier - 1].getIncreasePerTier();
        } else {
            return modifier * getCurses()[-modifier - 1].getIncreasePerTier();
        }
    }

    private String getItemScoreString() {
        return ChatColor.GRAY + "Score: " + ChatColor.YELLOW + NumberFormat.formatOptionalHundredths(getItemScore()) + ChatColor.GRAY + "/" + ChatColor.GREEN + "100";
    }

    private String getWeightString() {
        return ChatColor.GRAY + "Weight: " + ChatColor.GOLD + ChatColor.BOLD + NumberFormat.formatOptionalHundredths(getWeight());
    }

    public abstract ItemType getType();

    public static List<String> getStatPoolLore(Map<ItemStatPool, Integer> statPool) {
        return getStatPoolLore(statPool, "");
    }

    public Map<ItemStatPool, Integer> getStatPool() {
        if (statPoolValues == null) {
            statPoolValues = new HashMap<>();
            statPoolDistribution.forEach((stat, distribution) -> {
                ItemTier.StatRange statRange = ItemStatPool.STAT_RANGES.get(stat);
                double tieredDistribution = distribution + tier.statDistributionModifier;
                // clamp to [0, 1]
                tieredDistribution = Math.max(0, Math.min(1, tieredDistribution));
                int max = statRange.getMax() * stat.getDecimalPlace().value;
                int min = statRange.getMin() * stat.getDecimalPlace().value;
                int value = (int) ((max - min) * tieredDistribution + min);
                // floor value
                value = value / stat.getDecimalPlace().value * stat.getDecimalPlace().value;
                statPoolValues.put(stat, value);
            });
        }
        return statPoolValues;
    }

    public float getItemScore() {
        double sum = 0;
        for (Map.Entry<ItemStatPool, Float> statDistribution : statPoolDistribution.entrySet()) {
            sum += statDistribution.getValue();
        }
        return Math.round(sum / statPoolDistribution.size() * 10000) / 100f;
    }

    public int getWeight() {
        float itemScore = getWeightScore();
        ItemTier.WeightRange weightRange = tier.weightRange;
        int min = weightRange.getMin();
        int normal = weightRange.getNormal();
        int max = weightRange.getMax();

        if (itemScore <= 10) {
            return max;
        }
        if (45 <= itemScore && itemScore <= 55) {
            return normal;
        }
        if (itemScore >= 90) {
            return min;
        }

        int weight = max;
        // score: 10 - normal
        double midToTopIncrement = 35d / (max - normal - 1);
        for (double weightCheck = 10; weightCheck < 45; weightCheck += midToTopIncrement) {
            weight--;
            if (weightCheck <= itemScore && itemScore < weightCheck + midToTopIncrement) {
                return weight;
            }
            // safety check
            if (weight < 0) {
                return 1000;
            }
        }

        // account for normal weight
        weight--;

        // score: normal - 90
        double bottomToMidIncrement = 35d / (normal - min - 1);
        for (double weightCheck = 55; weightCheck < 90; weightCheck += bottomToMidIncrement) {
            weight--;
            if (weightCheck <= itemScore && itemScore < weightCheck + bottomToMidIncrement) {
                return weight;
            }
            // safety check
            if (weight < 0) {
                return 1000;
            }
        }
        return 1000;
    }

    public static List<String> getStatPoolLore(Map<ItemStatPool, Integer> statPool, String prefix) {
        List<String> lore = new ArrayList<>();
        statPool.keySet()
                .stream()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .forEachOrdered(stat -> lore.add(prefix + stat.getValueFormatted(statPool.get(stat))));
        return lore;
    }

    public float getWeightScore() {
        double sum = 0;
        for (Map.Entry<ItemStatPool, Float> statDistribution : statPoolDistribution.entrySet()) {
            float value = statDistribution.getValue() + tier.statDistributionModifier;
            sum += Math.max(0, Math.min(1, value));
        }
        return Math.round(sum / statPoolDistribution.size() * 10000) / 100f;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Instant getObtainedDate() {
        return obtainedDate;
    }

    public ItemTier getTier() {
        return tier;
    }

    public int getModifier() {
        return modifier;
    }

    public AbstractItem<R, U> setModifier(int modifier) {
        this.modifier = modifier;
        return this;
    }
}
