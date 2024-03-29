package com.ebicep.warlords.commands.debugcommands.game;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.HelpEntry;
import co.aikar.commands.annotation.*;
import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.GameManager.GameHolder;
import com.ebicep.warlords.game.GameMap;
import com.ebicep.warlords.game.GameMode;
import com.ebicep.warlords.util.chat.ChatChannels;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;

@CommandAlias("killgame")
@CommandPermission("warlords.game.kill")
public class GameKillCommand extends BaseCommand {

    public static void killGameMatching(CommandIssuer issuer, Predicate<GameHolder> gamePredicate, String from) {
        List<String> skippedGames = new ArrayList<>();
        for (GameHolder gameHolder : Warlords.getGameManager().getGames()) {
            if (gameHolder.getGame() == null) {
                skippedGames.add(gameHolder.getName());
                continue;
            }
            if (gamePredicate.test(gameHolder)) {
                ChatChannels.sendDebugMessage(issuer,
                        Component.text("Killed game from " + from + ": " + gameHolder.getName() + " - " + gameHolder.getMap() + " - " + gameHolder.getGame()
                                                                                                                                                  .playersCount() + " players",
                                NamedTextColor.GREEN
                        )
                );
                gameHolder.forceEndGame();
            }
        }
        ChatChannels.sendDebugMessage(issuer, Component.text("(" + skippedGames.size() + ") Skipped inactive kill game from " + from + ": " + skippedGames, NamedTextColor.RED));
    }

    @Default
    @Description("Kills your current game")
    public void killGame(@Conditions("requireGame") Player player) {
        Game playerGame = Warlords.getGameManager().getPlayerGame(player.getUniqueId()).get();
        for (GameHolder game : Warlords.getGameManager().getGames()) {
            if (Objects.equals(game.getGame(), playerGame)) {
                game.forceEndGame();
                ChatChannels.sendDebugMessage(player, Component.text("Killed own game " + game.getName(), NamedTextColor.GREEN));
                break;
            }
        }
    }

    @Subcommand("all")
    @CommandPermission("warlords.game.end.remote")
    @Description("Kill all games")
    public void killAllGames(CommandIssuer issuer) {
        for (GameHolder game : Warlords.getGameManager().getGames()) {
            game.forceEndGame();
        }
        ChatChannels.sendDebugMessage(issuer, Component.text("Killed all games", NamedTextColor.RED));
    }

    @Subcommand("map")
    @CommandPermission("warlords.game.end.remote")
    @Description("Kill all games matching map")
    public void killGameFromMap(CommandIssuer issuer, GameMap map) {
        killGameMatching(issuer, game -> Objects.equals(game.getGame().getMap(), map), "MAP");
    }

    @Subcommand("gamemode")
    @CommandPermission("warlords.game.end.remote")
    @Description("Kill all games matching gamemode")
    public void killGameFromGameMode(CommandIssuer issuer, GameMode gameMode) {
        killGameMatching(issuer, game -> Objects.equals(game.getGame().getGameMode(), gameMode), "GAMEMODE");
    }

    @Subcommand("gameid")
    @CommandCompletion("@gameids")
    @CommandPermission("warlords.game.end.remote")
    @Description("Kill all games with matching id")
    public void killGameFromGameId(CommandIssuer issuer, @Values("@gameids") UUID uuid) {
        killGameMatching(issuer, game -> Objects.equals(game.getGame().getGameId(), uuid), "GAMEID");
    }

    @HelpCommand
    public void help(CommandIssuer issuer, CommandHelp help) {
        help.getHelpEntries().sort(Comparator.comparing(HelpEntry::getCommand));
        help.showHelp();
    }
}
