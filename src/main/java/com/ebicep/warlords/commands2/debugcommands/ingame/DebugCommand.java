package com.ebicep.warlords.commands2.debugcommands.ingame;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.commands2.CommandManager;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.option.GameFreezeOption;
import com.ebicep.warlords.game.state.TimerDebugAble;
import com.ebicep.warlords.menu.debugmenu.DebugMenu;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static com.ebicep.warlords.commands2.miscellaneouscommands.ChatChannelCommand.sendDebugMessage;

@CommandAlias("wl")
@CommandPermission("warlords.game.debug")
public class DebugCommand extends BaseCommand {

    @Default
    public void openDebugMenu(Player player) {
        DebugMenu.openDebugMenu(player);
    }

    @Subcommand("freeze")
    @Description("Freezes/Unfreezes the game")
    public void freezeGame(@Conditions("requireGame") Player player) {
        Game game = Warlords.getGameManager().getPlayerGame(player.getUniqueId()).get();
        if (!game.isUnfreezeCooldown()) {
            if (game.isFrozen()) {
                GameFreezeOption.resumeGame(game);
            } else {
                game.addFrozenCause(ChatColor.GOLD + "Manually paused by §c" + player.getName());
                sendDebugMessage(player, ChatColor.GREEN + "The game has been frozen!");
            }
        } else {
            sendDebugMessage(player, ChatColor.RED + "The game is currently unfreezing!");
        }
    }

    @Subcommand("timer")
    @CommandCompletion("reset|skip")
    @Description("Resets or skips the timer")
    public void timer(@Conditions("requireGame") Player player, @Values("reset|skip") String option) {
        Game game = Warlords.getGameManager().getPlayerGame(player.getUniqueId()).get();
        if (!(game.getState() instanceof TimerDebugAble)) {
            sendDebugMessage(player, ChatColor.RED + "This gamestate cannot be manipulated by the timer debug option!");
            return;
        }
        TimerDebugAble timerDebugAble = (TimerDebugAble) game.getState();
        switch (option) {
            case "reset":
                timerDebugAble.resetTimer();
                sendDebugMessage(player, ChatColor.GREEN + "Timer has been reset!");
                break;
            case "skip":
                timerDebugAble.skipTimer();
                sendDebugMessage(player, ChatColor.GREEN + "Timer has been skipped!");
                break;

        }
    }

    @Subcommand("respawn")
    @CommandCompletion("@warlordsplayers")
    @Description("Respawns a player or sender if there is no target")
    public void respawn(CommandIssuer issuer, @Default(CommandManager.SELF) WarlordsPlayer target) {
        target.respawn();
    }

    @Subcommand("energy")
    @CommandCompletion("@enabledisable @warlordsplayers")
    @Description("Toggles ability energy usage for a player or sender if there is no target")
    public void setEnergy(CommandIssuer issuer, @Values("@enabledisable") String option, @Default(CommandManager.SELF) WarlordsPlayer target) {
        boolean enable = option.equals("enable");
        target.setNoEnergyConsumption(enable);
        sendDebugMessage(issuer, target.getColoredName() + ChatColor.GREEN + "'s No Energy Consumption was set to " + enable);
    }

    @Subcommand("cooldown")
    @CommandCompletion("@enabledisable @warlordsplayers")
    @Description("Toggles ability cooldowns for a player or sender if there is no target")
    public void setCooldown(CommandIssuer issuer, @Values("@enabledisable") String option, @Default(CommandManager.SELF) WarlordsPlayer target) {
        boolean disable = option.equals("disable");
        target.setDisableCooldowns(disable);
        sendDebugMessage(issuer, target.getColoredName() + ChatColor.GREEN + "'s Cooldown Timers have been " + option + "d!");
    }

    @Subcommand("takedamage")
    @CommandCompletion("@enabledisable @warlordsplayers")
    @Description("Toggles if a player takes damage or sender if there is no target")
    public void setTakeDamage(CommandIssuer issuer, @Values("@enabledisable") String option, @Default(CommandManager.SELF) WarlordsPlayer target) {
        boolean enable = option.equals("enable");
        target.setTakeDamage(enable);
        sendDebugMessage(issuer, target.getColoredName() + ChatColor.GREEN + " will " + (!enable ? "no longer take" : "start taking") + " damage!");
    }

    @Subcommand("crits")
    @CommandCompletion("@enabledisable @warlordsplayers")
    @Description("Toggles if a player can crit or sender if there is no target")
    public void setCrits(CommandIssuer issuer, @Values("@enabledisable") String option, @Default(CommandManager.SELF) WarlordsPlayer target) {
        boolean enable = option.equals("enable");
        target.setCanCrit(enable);
        sendDebugMessage(issuer, target.getColoredName() + ChatColor.GREEN + "'s Crits have been " + option + "d!");
    }

    @Subcommand("heal")
    @CommandCompletion("@warlordsplayers")
    @Description("Heals a player based on the amount or sender if there is no target")
    public void heal(CommandIssuer issuer, @Default("1000") @Conditions("limits:min=0,max=100000") Integer amount, @Default(CommandManager.SELF) WarlordsPlayer target) {
        target.addHealingInstance(target, "debug", amount, amount, -1, 100, false, false);
        target.setRegenTimer(10);
        sendDebugMessage(issuer, target.getColoredName() + ChatColor.GREEN + " was healed for " + amount + " health!");
    }

    @Subcommand("damage")
    @CommandCompletion("@warlordsplayers")
    @Description("Damages a player based on the amount or sender if there is no target")
    public void damage(CommandIssuer issuer, @Default("1000") @Conditions("limits:min=0,max=100000") Integer amount, @Default(CommandManager.SELF) WarlordsPlayer target) {
        target.addDamageInstance(target, "debug", amount, amount, -1, 100, false);
        target.setRegenTimer(10);
        sendDebugMessage(issuer, target.getColoredName() + ChatColor.GREEN + " took " + amount + " damage!");
    }

    @HelpCommand
    public void help(CommandIssuer issuer, CommandHelp help) {
        help.showHelp();
    }
}