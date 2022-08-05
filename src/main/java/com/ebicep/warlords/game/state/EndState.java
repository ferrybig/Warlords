package com.ebicep.warlords.game.state;

import com.ebicep.warlords.commands.debugcommands.misc.GetPlayerLastAbilityStatsCommand;
import com.ebicep.warlords.events.WarlordsGameTriggerWinEvent;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.GameAddon;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.option.Option;
import com.ebicep.warlords.player.general.ExperienceManager;
import com.ebicep.warlords.player.general.MinuteStats;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.util.bukkit.PacketUtils;
import com.ebicep.warlords.util.bukkit.TextComponentBuilder;
import com.ebicep.warlords.util.chat.ChatUtils;
import com.ebicep.warlords.util.java.NumberFormat;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.ebicep.warlords.database.repositories.games.pojos.DatabaseGameBase.previousGames;


public class EndState implements State, TimerDebugAble {
    @Nonnull
    private final Game game;
    private final WarlordsGameTriggerWinEvent winEvent;
    private int timer;

    public EndState(@Nonnull Game game, @Nullable WarlordsGameTriggerWinEvent event) {
        this.game = game;
        this.winEvent = event;
    }

    @Override
    public void begin() {
        for (Option option : game.getOptions()) {
            option.onGameEnding(game);
        }
        boolean teamBlueWins = winEvent != null && winEvent.getDeclaredWinner() == Team.BLUE;
        boolean teamRedWins = winEvent != null && winEvent.getDeclaredWinner() == Team.RED;
        List<WarlordsEntity> players = game.warlordsPlayers().collect(Collectors.toList());
        if (players.isEmpty()) return;
        sendGlobalMessage(game, "" + ChatColor.GREEN + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        sendGlobalMessage(game, "" + ChatColor.WHITE + ChatColor.BOLD + "  Warlords 2.0", true);
        sendGlobalMessage(game, "", false);
        if (teamBlueWins) {
            sendGlobalMessage(game, ChatColor.YELLOW + "Winner" + ChatColor.GRAY + " - " + ChatColor.BLUE + "BLU", true);
        } else if (teamRedWins) {
            sendGlobalMessage(game, ChatColor.YELLOW + "Winner" + ChatColor.GRAY + " - " + ChatColor.RED + "RED", true);
        } else {
            if (game.getAddons().contains(GameAddon.IMPOSTER_MODE)) {
                sendGlobalMessage(game, ChatColor.YELLOW + "Winner" + ChatColor.GRAY + " - " + ChatColor.LIGHT_PURPLE + "GAME END", true);
            } else {
                sendGlobalMessage(game, ChatColor.YELLOW + "Winner" + ChatColor.GRAY + " - " + ChatColor.LIGHT_PURPLE + "DRAW", true);
            }
        }

        switch (game.getGameMode()) {
            case INTERCEPTION:
            case SIMULATION_TRIAL:
            case TEAM_DEATHMATCH:
                showTopDamage(players);
                showTopHealing(players);
                break;
            case CAPTURE_THE_FLAG:
                showFlagCaptures(players);
                showTopDamage(players);
                showTopHealing(players);
                break;
            default:
                sendGlobalMessage(game, "", false);
                break;
        }

        //PLAYER STATS

        sendGlobalMessage(game, "", false);
        sendGlobalEventMessage(game, Collections.singletonList(new TextComponentBuilder(ChatColor.GOLD.toString() + ChatColor.BOLD + "✚ YOUR STATISTICS ✚")
                .setHoverText(
                        ChatColor.WHITE + "Total Kills (everyone): " + ChatColor.GREEN + NumberFormat.addCommaAndRound(players.stream().mapToInt(wp -> wp.getMinuteStats().total().getKills()).sum()) + "\n" +
                                ChatColor.WHITE + "Total Assists (everyone): " + ChatColor.GREEN + NumberFormat.addCommaAndRound(players.stream().mapToInt(wp -> wp.getMinuteStats().total().getAssists()).sum()) + "\n" +
                                ChatColor.WHITE + "Total Deaths (everyone): " + ChatColor.GREEN + NumberFormat.addCommaAndRound(players.stream().mapToInt(wp -> wp.getMinuteStats().total().getDeaths()).sum()))
                .getTextComponent()));

        for (WarlordsEntity wp : PlayerFilter.playingGameWarlordsPlayers(game)) {
            Player player = Bukkit.getPlayer(wp.getUuid());
            if (player == null) continue;

            List<TextComponent> textComponents = new ArrayList<>(wp.getAllMinuteHoverableStats(MinuteStats.KILLS));
            textComponents.add(ChatUtils.SPACER);
            textComponents.addAll(wp.getAllMinuteHoverableStats(MinuteStats.ASSISTS));
            textComponents.add(ChatUtils.SPACER);
            textComponents.addAll(wp.getAllMinuteHoverableStats(MinuteStats.DEATHS));
            ChatUtils.sendCenteredMessageWithEvents(player, textComponents);

            textComponents.clear();
            textComponents.addAll(wp.getAllMinuteHoverableStats(MinuteStats.DAMAGE));
            textComponents.add(ChatUtils.SPACER);
            textComponents.addAll(wp.getAllMinuteHoverableStats(MinuteStats.HEALING));
            textComponents.add(ChatUtils.SPACER);
            textComponents.addAll(wp.getAllMinuteHoverableStats(MinuteStats.ABSORBED));
            ChatUtils.sendCenteredMessageWithEvents(player, textComponents);

            ChatUtils.sendMessage(player, false, "");

            //ABILITY INFO
            List<TextComponent> formattedData = wp.getSpec().getFormattedData();
            ChatUtils.sendCenteredMessageWithEvents(player, Arrays.asList(formattedData.get(0), ChatUtils.SPACER, formattedData.get(1), ChatUtils.SPACER, formattedData.get(2)));
            ChatUtils.sendCenteredMessageWithEvents(player, Arrays.asList(formattedData.get(3), ChatUtils.SPACER, formattedData.get(4)));

            GetPlayerLastAbilityStatsCommand.playerLastAbilityStats.put(player.getUniqueId(), formattedData);

            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);

            if (!game.getAddons().contains(GameAddon.IMPOSTER_MODE)) {
                if (winEvent == null || winEvent.getDeclaredWinner() == null) {
                    player.playSound(player.getLocation(), "defeat", 500, 1);
                    PacketUtils.sendTitle(player, "§d§lDRAW", "", 0, 100, 0);
                } else if (wp.getTeam() == winEvent.getDeclaredWinner()) {
                    player.playSound(player.getLocation(), "victory", 500, 1);
                    PacketUtils.sendTitle(player, "§6§lVICTORY!", "", 0, 100, 0);
                } else {
                    player.playSound(player.getLocation(), "defeat", 500, 1);
                    PacketUtils.sendTitle(player, "§c§lDEFEAT", "", 0, 100, 0);
                }
            }
        }

        this.resetTimer();

        //EXPERIENCE
        if (winEvent != null && players.size() >= 12 && previousGames.get(previousGames.size() - 1).isCounted()) {
            showExperienceSummary(game);
        }
        sendGlobalMessage(game, "" + ChatColor.GREEN + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
    }

    @Override
    public State run() {
        timer--;
        if (timer <= 0) {
            return new ClosedState(game);
        }
        return null;
    }

    @Override
    public void end() {
        game.removeAllPlayers();
    }

    @Override
    public void skipTimer() throws IllegalStateException {
        this.timer = 0;
    }

    @Override
    public void resetTimer() throws IllegalStateException {
        this.timer = 10 * 20;
    }

    public WarlordsGameTriggerWinEvent getWinEvent() {
        return winEvent;
    }

    private void sendGlobalMessage(Game game, String message, boolean centered) {
        game.forEachOnlinePlayerWithoutSpectators((p, team) -> {
            if (centered) {
                ChatUtils.sendCenteredMessage(p, message);
            } else {
                p.sendMessage(message);
            }
        });
    }

    public void sendGlobalEventMessage(Game game, List<TextComponent> message) {
        game.forEachOnlinePlayerWithoutSpectators((p, team) -> {
            ChatUtils.sendCenteredMessageWithEvents(p, message);
        });
    }

    private void showTopDamage(List<WarlordsEntity> players) {
        sendGlobalMessage(game, "", false);
        sendGlobalEventMessage(game, Collections.singletonList(
                new TextComponentBuilder(ChatColor.RED.toString() + ChatColor.BOLD + "✚ TOP DAMAGE ✚")
                    .setHoverText(
                            ChatColor.RED + "Total Damage (everyone)" +
                            ChatColor.GRAY + ": " +
                            ChatColor.GOLD + NumberFormat.addCommaAndRound(players.stream().mapToLong(wp -> wp.getMinuteStats().total().getDamage()).sum()))
                    .getTextComponent()));

        players = players.stream().sorted(Comparator.comparing((WarlordsEntity wp) -> wp.getMinuteStats().total().getDamage()).reversed()).collect(Collectors.toList());
        List<TextComponent> leaderboardPlayersDamage = new ArrayList<>();
        for (int i = 0; i < players.size() && i < 3; i++) {
            WarlordsEntity we = players.get(i);
            leaderboardPlayersDamage.add(
                    new TextComponentBuilder(
                            ChatColor.AQUA + we.getName() +
                            ChatColor.GRAY + ": " +
                            ChatColor.GOLD + NumberFormat.getSimplifiedNumber(we.getMinuteStats().total().getDamage()))
                    .setHoverText(
                            ChatColor.DARK_GRAY + "Lv" +
                            ChatColor.GRAY + ExperienceManager.getLevelForSpec(we.getUuid(), we.getSpecClass()) + " " +
                            ChatColor.GOLD + we.getSpec().getClassName() +
                            ChatColor.GREEN + " (" +
                            we.getSpec().getClass().getSimpleName() +
                            ")"
                    ).getTextComponent());

            if (i != players.size() - 1 && i != 2) {
                leaderboardPlayersDamage.add(ChatUtils.SPACER);
            }
        }
        sendGlobalEventMessage(game, leaderboardPlayersDamage);
    }

    private void showTopHealing(List<WarlordsEntity> players) {
        sendGlobalMessage(game, "", false);
        sendGlobalEventMessage(game, Collections.singletonList(
                new TextComponentBuilder(ChatColor.GREEN.toString() + ChatColor.BOLD + "✚ TOP HEALING ✚")
                .setHoverText(
                        ChatColor.GREEN + "Total Healing (everyone)" +
                        ChatColor.GRAY + ": " +
                        ChatColor.GOLD + NumberFormat.addCommaAndRound(players.stream().mapToLong(wp -> wp.getMinuteStats().total().getHealing()).sum()))
                .getTextComponent()));

        players = players.stream().sorted(Comparator.comparing((WarlordsEntity wp) -> wp.getMinuteStats().total().getHealing()).reversed()).collect(Collectors.toList());
        List<TextComponent> leaderboardPlayersHealing = new ArrayList<>();
        for (int i = 0; i < players.size() && i < 3; i++) {
            WarlordsEntity wp = players.get(i);
            leaderboardPlayersHealing.add(
                    new TextComponentBuilder(
                            ChatColor.AQUA + wp.getName() +
                            ChatColor.GRAY + ": " +
                            ChatColor.GOLD + NumberFormat.getSimplifiedNumber(wp.getMinuteStats().total().getHealing()))
                    .setHoverText(
                            ChatColor.DARK_GRAY + "Lv" +
                            ChatColor.GRAY + ExperienceManager.getLevelForSpec(wp.getUuid(), wp.getSpecClass()) + " " +
                            ChatColor.GOLD + wp.getSpec().getClassName() +
                            ChatColor.GREEN + " (" +
                            wp.getSpec().getClass().getSimpleName() +
                            ")"
                    ).getTextComponent());

            if (i != players.size() - 1 && i != 2) {
                leaderboardPlayersHealing.add(ChatUtils.SPACER);
            }
        }
        sendGlobalEventMessage(game, leaderboardPlayersHealing);
    }

    /**
     * @param players player collection  to give the capture message module to.
     */
    private void showFlagCaptures(List<WarlordsEntity> players) {
        sendGlobalMessage(game, "", false);
        sendGlobalEventMessage(game, Collections.singletonList(
                new TextComponentBuilder(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "✚ MVP ✚")
                    .setHoverText(
                            ChatColor.LIGHT_PURPLE + "Total Flag Captures (everyone): " +
                            ChatColor.GOLD + NumberFormat.addCommaAndRound(players.stream().mapToInt(WarlordsEntity::getFlagsCaptured).sum()) +
                            "\n" +
                            ChatColor.LIGHT_PURPLE + "Total Flag Returns (everyone): " +
                            ChatColor.GOLD + NumberFormat.addCommaAndRound(players.stream().mapToInt(WarlordsEntity::getFlagsCaptured).sum()))
                    .getTextComponent()));
        players = players.stream().sorted(Comparator.comparing(WarlordsEntity::getTotalCapsAndReturnsWeighted).reversed()).collect(Collectors.toList());
        sendGlobalEventMessage(game, Collections.singletonList(
                new TextComponentBuilder(ChatColor.AQUA + players.get(0).getName())
                    .setHoverText(ChatColor.LIGHT_PURPLE + "Flag Captures: " +
                            ChatColor.GOLD + players.get(0).getFlagsCaptured() +
                            "\n" +
                            ChatColor.LIGHT_PURPLE + "Flag Returns: " + ChatColor.GOLD + players.get(0).getFlagsReturned())
                    .getTextComponent()));
    }

    private void showExperienceSummary(Game game) {
        sendGlobalMessage(game, "", false);
        sendGlobalMessage(game, ChatColor.YELLOW.toString() + ChatColor.BOLD + "✚ EXPERIENCE SUMMARY ✚", true);
        for (WarlordsEntity wp : PlayerFilter.playingGameWarlordsPlayers(game)) {
            Player player = Bukkit.getPlayer(wp.getUuid());
            if (player == null) continue;

            LinkedHashMap<String, Long> expSummary = ExperienceManager.getExpFromGameStats(wp, false);
            long experienceEarnedUniversal = expSummary.values().stream().mapToLong(Long::longValue).sum();
            long experienceEarnedSpec = ExperienceManager.getSpecExpFromSummary(expSummary);
            long experienceOnSpec = ExperienceManager.getExperienceForSpec(wp.getUuid(), wp.getSpecClass());
            long experienceUniversal = ExperienceManager.getUniversalLevel(wp.getUuid());
            StringBuilder specExpSummary = new StringBuilder();
            StringBuilder universalExpSummary = new StringBuilder();
            expSummary.forEach((s, aLong) -> {
                if (
                    !s.equals("First Game of the Day") &&
                    !s.equals("Second Game of the Day") &&
                    !s.equals("Third Game of the Day")
                ) {
                    specExpSummary.append(ChatColor.AQUA)
                            .append(s).append(ChatColor.WHITE)
                            .append(": ")
                            .append(ChatColor.DARK_GRAY)
                            .append("+")
                            .append(ChatColor.DARK_GREEN)
                            .append(aLong)
                            .append("\n");
                }

                universalExpSummary.append(ChatColor.AQUA)
                        .append(s)
                        .append(ChatColor.WHITE)
                        .append(": ")
                        .append(ChatColor.DARK_GRAY)
                        .append("+")
                        .append(ChatColor.DARK_GREEN)
                        .append(aLong)
                        .append("\n");
            });

            specExpSummary.setLength(specExpSummary.length() - 1);
            universalExpSummary.setLength(universalExpSummary.length() - 1);

            ChatUtils.sendCenteredMessageWithEvents(player, Collections.singletonList(
                    new TextComponentBuilder(
                            ChatColor.GRAY + "+" +
                            ChatColor.DARK_GREEN + NumberFormat.addCommaAndRound(experienceEarnedSpec) + " " +
                            ChatColor.GOLD + wp.getSpec().getClassName() + " Experience " +
                            ChatColor.GRAY + "(" +
                                    wp.getSpecClass().specType.chatColor + wp.getSpecClass().name +
                            ChatColor.GRAY + ")")
                    .setHoverText(specExpSummary.toString())
                    .getTextComponent())
            );

            ExperienceManager.giveLevelUpMessage(player, experienceOnSpec, experienceOnSpec + experienceEarnedSpec);
            ChatUtils.sendCenteredMessageWithEvents(player, Collections.singletonList(
                    new TextComponentBuilder(
                            ChatColor.GRAY + "+" +
                            ChatColor.DARK_AQUA + NumberFormat.addCommaAndRound(experienceEarnedUniversal) + " " +
                            ChatColor.GOLD + "Universal Experience ")
                    .setHoverText(universalExpSummary.toString())
                    .getTextComponent())
            );

            ExperienceManager.giveLevelUpMessage(player, experienceUniversal, experienceUniversal + experienceEarnedUniversal);
            ExperienceManager.CACHED_PLAYER_EXP_SUMMARY.remove(wp.getUuid());
        }
    }

    @Override
    public int getTicksElapsed() {
        return 0;
    }

}
