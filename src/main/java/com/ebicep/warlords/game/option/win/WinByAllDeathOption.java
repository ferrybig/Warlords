package com.ebicep.warlords.game.option.win;

import com.ebicep.warlords.events.game.WarlordsGameTriggerWinEvent;
import com.ebicep.warlords.events.player.ingame.WarlordsDeathEvent;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.option.Option;
import com.ebicep.warlords.game.option.marker.TeamMarker;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.util.warlords.PlayerFilterGeneric;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;

/**
 * Triggers a win condition when there is only 1 team left where anyone is alive
 */
public class WinByAllDeathOption implements Option {


    private final EnumSet<Team> deadTeams = EnumSet.noneOf(Team.class);
    private final Team onlyCheckTeam;

    public WinByAllDeathOption() {
        this(null);
    }

    public WinByAllDeathOption(Team onlyCheckTeam) {
        this.onlyCheckTeam = onlyCheckTeam;
    }

    @Override
    public void start(@Nonnull Game game) {
        final EnumSet<Team> teams = TeamMarker.getTeams(game);
        if (onlyCheckTeam != null) {
            teams.removeIf(team -> team != onlyCheckTeam);
        }

        game.registerEvents(new Listener() {

            @EventHandler
            public void onDeath(WarlordsDeathEvent event) {
                if (event.getWarlordsEntity() instanceof WarlordsPlayer) {
                    if (onlyCheckTeam != null) {
                        if (PlayerFilterGeneric.playingGameWarlordsPlayers(game)
                                               .matchingTeam(onlyCheckTeam)
                                               .stream()
                                               .allMatch(WarlordsEntity::isDead)
                        ) {
                            Bukkit.getPluginManager().callEvent(new WarlordsGameTriggerWinEvent(game, WinByAllDeathOption.this, onlyCheckTeam.enemy()));
                        }

                    } else {
                        teams.removeIf(team -> {
                            List<WarlordsPlayer> warlordsPlayers = PlayerFilterGeneric.playingGameWarlordsPlayers(game)
                                                                                      .matchingTeam(team)
                                                                                      .toList();
                            if (warlordsPlayers.isEmpty()) {
                                return false;
                            }
                            for (WarlordsPlayer warlordsPlayer : warlordsPlayers) {
                                if (warlordsPlayer.isAlive()) {
                                    return false;
                                }
                            }
                            deadTeams.add(team);
                            return true;
                        });
                        if (teams.size() == 1) {
                            Bukkit.getPluginManager().callEvent(new WarlordsGameTriggerWinEvent(game, WinByAllDeathOption.this, teams.iterator().next()));
                        }
                    }
                }
            }

        });
    }

    public EnumSet<Team> getDeadTeams() {
        return deadTeams;
    }
}
