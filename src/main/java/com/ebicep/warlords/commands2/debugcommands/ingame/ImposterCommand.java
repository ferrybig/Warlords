package com.ebicep.warlords.commands2.debugcommands.ingame;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.commands2.miscellaneouscommands.ChatChannelCommand;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.option.ImposterModeOption;
import com.ebicep.warlords.game.option.Option;
import com.ebicep.warlords.game.state.PlayingState;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;


@CommandAlias("imposter|impostor")
public class ImposterCommand extends BaseCommand {

    @Subcommand("assign")
    @CommandPermission("warlords.game.impostertoggle")
    @Description("Assign/Reassign the imposters in the game")
    public void assign(@Conditions("requireWarlordsPlayer|requireGame:withAddon=IMPOSTER_MODE") Player player) {
        WarlordsEntity warlordsPlayer = Warlords.getPlayer(player);
        for (Option option : warlordsPlayer.getGame().getOptions()) {
            if (option instanceof ImposterModeOption) {
                ((ImposterModeOption) option).assignImpostersWithAnimation(0);
                ChatChannelCommand.sendDebugMessage(player, ChatColor.GREEN + "Imposters assigned");
                return;
            }
        }
    }

    @Subcommand("vote")
    @Description("Vote to vote out a player")
    public void vote(@Conditions("requireWarlordsPlayer|requireGame:withAddon=IMPOSTER_MODE") Player player) {
        WarlordsEntity warlordsPlayer = Warlords.getPlayer(player);
        if (warlordsPlayer.getGame().getState(PlayingState.class)
                .map(PlayingState::getTicksElapsed)
                .orElse(0) < 60 * 20 * 5) {
            player.sendMessage(ChatColor.RED + "You cannot request to vote before 5 minutes have past!");
            return;
        }
        for (Option option : warlordsPlayer.getGame().getOptions()) {
            if (option instanceof ImposterModeOption) {
                ImposterModeOption imposterModeOption = (ImposterModeOption) option;
                if (imposterModeOption.getPoll() != null) {
                    player.sendMessage(ChatColor.GREEN + "There is an ongoing poll!");
                    return;
                }
                if (imposterModeOption.getVoters().values().stream().anyMatch(warlordsPlayers -> warlordsPlayers.contains(warlordsPlayer))) {
                    player.sendMessage(ChatColor.RED + "You already voted to vote!");
                    return;
                }
                imposterModeOption.getVoters().computeIfAbsent(warlordsPlayer.getTeam(), v -> new ArrayList<>()).add(warlordsPlayer);

                int votesNeeded = (int) (warlordsPlayer.getGame().getPlayers().entrySet().stream().filter(uuidTeamEntry -> uuidTeamEntry.getValue() == warlordsPlayer.getTeam()).count() * .75 + 1);
                if (votesNeeded >= imposterModeOption.getVoters().get(warlordsPlayer.getTeam()).size()) {
                    Team team = warlordsPlayer.getTeam();
                    imposterModeOption.sendPoll(team);
                    warlordsPlayer.getGame().addFrozenCause(team.teamColor + team.name + ChatColor.GREEN + " is voting!");
                } else {
                    warlordsPlayer.getGame().forEachOnlinePlayerWithoutSpectators((p, team) -> {
                        if (team == warlordsPlayer.getTeam()) {
                            p.sendMessage(ChatColor.GREEN + "A player wants to vote out someone! (" + imposterModeOption.getVoters().get(warlordsPlayer.getTeam()).size() + "/" + votesNeeded + ")");
                        }
                    });
                }

                return;
            }
        }
    }

    @HelpCommand
    public void help(CommandIssuer issuer, CommandHelp help) {
        help.showHelp();
    }
}