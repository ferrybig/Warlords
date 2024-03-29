package com.ebicep.warlords.achievements;

import com.ebicep.warlords.game.GameMode;
import com.ebicep.warlords.player.general.Specializations;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.util.bukkit.WordWrap;
import com.ebicep.warlords.util.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.function.Function;

public interface Achievement {

    GameMode getGameMode();

    Specializations getSpec();

    boolean isHidden();

    Difficulty getDifficulty();

    default void sendAchievementUnlockMessage(Player player) {
        TextComponent component = Component.text(">>  Achievement Unlocked: ", NamedTextColor.GREEN)
                                           .append(Component.text(getName(), NamedTextColor.GOLD)
                                                            .hoverEvent(HoverEvent.showText(WordWrap.wrapWithNewline(Component.text(getDescription(), NamedTextColor.GREEN), 200))))
                                           .append(Component.text("  <<", NamedTextColor.GREEN));
        ChatUtils.sendMessageToPlayer(player, component, NamedTextColor.GREEN, true);
    }

    String getName();

    String getDescription();

    default void sendAchievementUnlockMessageToOthers(WarlordsEntity warlordsEntity) {
        TextComponent component = Component.text(">> ", NamedTextColor.GREEN)
                                           .append(Component.text(warlordsEntity.getName(), NamedTextColor.AQUA))
                                           .append(Component.text(" unlocked: ", NamedTextColor.GREEN))
                                           .append(Component.text(getName(), NamedTextColor.GOLD)
                                                            .hoverEvent(HoverEvent.showText(WordWrap.wrapWithNewline(Component.text(getDescription(), NamedTextColor.GREEN), 200))))
                                           .append(Component.text("  <<", NamedTextColor.GREEN));
        warlordsEntity.getGame().warlordsPlayers()
                      //.filter(wp -> wp.getTeam() == warlordsEntity.getTeam())
                      .filter(wp -> wp != warlordsEntity)
                      .filter(wp -> wp.getEntity() instanceof Player)
                      .map(wp -> (Player) wp.getEntity())
                      .forEachOrdered(player -> ChatUtils.sendMessageToPlayer(player, component, NamedTextColor.GREEN, true));

    }

    enum Difficulty {
        EASY("Easy",
                NamedTextColor.GREEN,
                integer -> (int) Math.round(Math.pow(integer, 1 / 4.0))
        ),
        MEDIUM("Medium",
                NamedTextColor.GOLD,
                integer -> (int) Math.round(Math.pow(integer, 1 / 3.5))
        ),
        HARD("Hard",
                NamedTextColor.RED,
                integer -> (int) Math.round(Math.pow(integer, 1 / 3))
        ),

        ;

        public static final Difficulty[] VALUES = values();
        public final String name;
        public final NamedTextColor textColor;
        public final Function<Integer, Integer> weightFunction;

        Difficulty(String name, NamedTextColor textColor, Function<Integer, Integer> weightFunction) {
            this.name = name;
            this.textColor = textColor;
            this.weightFunction = weightFunction;
        }

        public Component getColoredName() {
            return Component.text(name, textColor);
        }
    }

    abstract class AbstractAchievementRecord<T extends Enum<T> & Achievement> {

        private T achievement;
        private Instant date;

        public AbstractAchievementRecord() {
        }

        public AbstractAchievementRecord(T achievement) {
            this.achievement = achievement;
            this.date = Instant.now();
        }

        public AbstractAchievementRecord(T achievement, Instant date) {
            this.achievement = achievement;
            this.date = date;
        }

        public T getAchievement() {
            return achievement;
        }

        public Instant getDate() {
            return date;
        }

    }
}
