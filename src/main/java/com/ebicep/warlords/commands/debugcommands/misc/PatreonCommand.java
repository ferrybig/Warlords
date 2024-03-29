package com.ebicep.warlords.commands.debugcommands.misc;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import com.ebicep.warlords.commands.DatabasePlayerFuture;
import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.pve.rewards.types.PatreonReward;
import com.ebicep.warlords.util.chat.ChatChannels;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.time.Month;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.CompletionStage;

@CommandAlias("patreon")
@CommandPermission("group.administrator")
public class PatreonCommand extends BaseCommand {

    @Subcommand("give")
    public CompletionStage<?> add(CommandIssuer issuer, DatabasePlayerFuture databasePlayerFuture, Month month, @Optional Year year) {
        if (year == null) {
            year = Year.from(ZonedDateTime.now());
        }
        Year finalYear = year;
        return databasePlayerFuture.future().thenAccept(databasePlayer -> {
            boolean given = PatreonReward.giveMonthlyPatreonRewards(databasePlayer, month, finalYear);
            if (given) {
                ChatChannels.sendDebugMessage(issuer,
                        Component.text("Gave ", NamedTextColor.GREEN)
                                 .append(Component.text(finalYear.getValue() + " " + month.getDisplayName(TextStyle.FULL, Locale.ENGLISH), NamedTextColor.LIGHT_PURPLE))
                                 .append(Component.text(" Patreon reward to "))
                                 .append(Component.text(databasePlayer.getName(), NamedTextColor.AQUA))
                );
            } else {
                ChatChannels.sendDebugMessage(issuer,
                        Component.text(databasePlayer.getName(), NamedTextColor.AQUA)
                                 .append(Component.text(" has already received their monthly Patreon reward", NamedTextColor.RED))
                );
            }
            PatreonReward.givePatreonFutureMessage(databasePlayer, month, finalYear);
            DatabaseManager.queueUpdatePlayerAsync(databasePlayer);
        });
    }
}
