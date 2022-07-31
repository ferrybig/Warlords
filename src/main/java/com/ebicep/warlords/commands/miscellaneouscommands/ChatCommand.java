package com.ebicep.warlords.commands.miscellaneouscommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.ebicep.warlords.permissions.Permissions;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.util.chat.ChatChannels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static com.ebicep.warlords.util.chat.ChatChannels.*;

@CommandAlias("chat")
public class ChatCommand extends BaseCommand {
    public static void sendDebugMessage(Player player, String message, boolean asyncPlayerChat) {
        ChatChannels.playerSendMessage(player, message, ChatChannels.DEBUG, asyncPlayerChat);
    }

    public static void sendDebugMessage(WarlordsPlayer warlordsPlayer, String message, boolean asyncPlayerChat) {
        if (warlordsPlayer.getEntity() instanceof Player) {
            ChatChannels.playerSendMessage((Player) warlordsPlayer.getEntity(), message, ChatChannels.DEBUG, asyncPlayerChat);
        }
    }

    public static void sendDebugMessage(CommandIssuer commandIssuer, String message, boolean asyncPlayerChat) {
        if (commandIssuer.getIssuer() instanceof Player) {
            sendDebugMessage((Player) commandIssuer.getIssuer(), message, asyncPlayerChat);
        } else {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (Permissions.isAdmin(onlinePlayer)) {
                    onlinePlayer.sendMessage(DEBUG.getColoredName() + CHAT_ARROW + ChatColor.YELLOW + "Console: " + ChatColor.WHITE + message);
                }
            }
            commandIssuer.sendMessage(DEBUG.getColoredName() + CHAT_ARROW + message);
        }
    }

    @Subcommand("all|a")
    @Description("Switch to ALL chat channel")
    public void allChat(@Conditions("otherChatChannel:target=ALL") Player player) {
        switchChannels(player, ChatChannels.ALL);
    }

    @Subcommand("party|p")
    @Description("Switch to PARTY chat channel")
    public void partyChat(@Conditions("party:true|otherChatChannel:target=PARTY") Player player) {
        switchChannels(player, ChatChannels.PARTY);
    }

    @Subcommand("guild|g")
    @Description("Switch to GUILD chat channel")
    public void guildChat(@Conditions("guild:true|otherChatChannel:target=GUILD") Player player) {
        switchChannels(player, ChatChannels.GUILD);
    }

    @CommandAlias("achat|ac")
    @Description("Send a message to the ALL chat channel")
    public void allChat(Player player, String message) {
        ChatChannels.playerSendMessage(player, message, ChatChannels.ALL, true);
    }

    @CommandAlias("pchat|pc")
    @Description("Send a message to the PARTY chat channel")
    public void partyChat(@Conditions("party:true") Player player, String message) {
        ChatChannels.playerSendMessage(player, message, ChatChannels.PARTY, true);
    }

    @CommandAlias("gchat|gc")
    @Description("Send a message to the GUILD chat channel")
    public void guildChat(@Conditions("guild:true") Player player, String message) {
        ChatChannels.playerSendMessage(player, message, ChatChannels.GUILD, true);
    }

    @HelpCommand
    public void help(CommandIssuer issuer, CommandHelp help) {
        help.showHelp();
    }

}
