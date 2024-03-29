package com.ebicep.warlords.game.option.freeze;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.GameAddon;
import com.ebicep.warlords.game.GameMode;
import com.ebicep.warlords.game.option.Option;
import com.ebicep.warlords.game.state.PlayingState;
import com.ebicep.warlords.permissions.Permissions;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AFKDetectionOption implements Option {

    public static boolean enabled = true;

    private final HashMap<WarlordsPlayer, List<Location>> playerLocations = new HashMap<>();

    @Override
    public void register(@Nonnull Game game) {
        game.registerEvents(new Listener() {

            @EventHandler
            public void onPlayerInteract(PlayerInteractEvent event) {
                Player player = event.getPlayer();
                WarlordsEntity warlordsPlayer = Warlords.getPlayer(player);
                if (warlordsPlayer instanceof WarlordsPlayer) {
                    if (warlordsPlayer.getGame().equals(game)) {
                        //clearing player location list for clicking while standing still
                        playerLocations.computeIfAbsent((WarlordsPlayer) warlordsPlayer, k -> new ArrayList<>()).clear();
                    }
                }
            }

            @EventHandler
            public void onPlayerSneak(PlayerToggleSneakEvent event) {
                Player player = event.getPlayer();
                WarlordsEntity warlordsPlayer = Warlords.getPlayer(player);
                if (warlordsPlayer instanceof WarlordsPlayer) {
                    if (warlordsPlayer.getGame().equals(game)) {
                        //clearing player location list for sneaking while standing still
                        playerLocations.computeIfAbsent((WarlordsPlayer) warlordsPlayer, k -> new ArrayList<>()).clear();
                    }
                }
            }

        });
    }

    @Override
    public void start(@Nonnull Game game) {
        if (game.getPlayers().size() < 14 || game.getAddons().contains(GameAddon.CUSTOM_GAME) || GameMode.isPvE(game.getGameMode())) {
            return;
        }
        new GameRunnable(game) {

            boolean wasFrozen = false;

            @Override
            public void run() {
                if (!enabled) {
                    return;
                }

                //skips right after unfreeze
                if (wasFrozen) {
                    wasFrozen = false;
                    return;
                }

                game.getState(PlayingState.class).ifPresent(state -> {
                    for (WarlordsEntity we : PlayerFilter.playingGame(game)) {
                        if (we.isDead()) {
                            continue;
                        }
                        if (!(we.getEntity() instanceof Player)) {
                            continue;
                        }
                        if (we.isSneaking()) {
                            continue; //make sure no ppl that are sneaking are marked as AFK
                        }
                        playerLocations.computeIfAbsent((WarlordsPlayer) we, k -> new ArrayList<>()).add(we.getLocation());
                        List<Location> locations = playerLocations.get(we);
                        if (locations.size() >= 2) {
                            Location lastLocation = locations.get(locations.size() - 1);
                            Location secondLastLocation = locations.get(locations.size() - 2);
                            if (locations.size() >= 3) {
                                Location thirdLastLocation = locations.get(locations.size() - 3);
                                if (locations.size() >= 4) {
                                    Location fourthLastLocation = locations.get(locations.size() - 4);
                                    if (locations.size() >= 5) {
                                        Location fifthLastLocation = locations.get(locations.size() - 5);
                                        if (lastLocation.equals(secondLastLocation) && lastLocation.equals(thirdLastLocation) && lastLocation.equals(
                                                fourthLastLocation) && lastLocation.equals(fifthLastLocation)) {
                                            //hasnt moved for 12.5 seconds
                                            for (WarlordsEntity wp : PlayerFilter.playingGame(game)) {
                                                Permissions.sendMessageToDebug(wp, Component.text("----------------------------------------", NamedTextColor.RED));
                                                Permissions.sendMessageToDebug(wp,
                                                        Component.text(we.getName(), NamedTextColor.AQUA)
                                                                 .append(Component.text(" is AFK. (Hasn't moved for 12.5 seconds)", NamedTextColor.RED))
                                                );
                                                Permissions.sendMessageToDebug(wp, Component.text("----------------------------------------", NamedTextColor.RED));
                                            }
                                            game.addFrozenCause(Component.text(we.getName(), NamedTextColor.AQUA)
                                                                         .append(Component.text(" has been detected as AFK.", NamedTextColor.RED))
                                            );
                                            wasFrozen = true;
                                            continue;
                                        }
                                    }
                                    if (thirdLastLocation.equals(fourthLastLocation)) {
                                        //hasnt moved for 10 seconds
                                        for (WarlordsEntity wp : PlayerFilter.playingGame(game)) {
                                            Permissions.sendMessageToDebug(wp, Component.text("----------------------------------------", NamedTextColor.RED));
                                            Permissions.sendMessageToDebug(wp,
                                                    Component.text(we.getName(), NamedTextColor.AQUA)
                                                             .append(Component.text(" is AFK. (Hasn't moved for 10 seconds)", NamedTextColor.RED))
                                            );
                                            Permissions.sendMessageToDebug(wp, Component.text("----------------------------------------", NamedTextColor.RED));
                                        }
                                    }
                                    continue;
                                }
                                if (secondLastLocation.equals(thirdLastLocation)) {
                                    //hasnt moved for 7.5 seconds
                                    for (WarlordsEntity wp : PlayerFilter.playingGame(game)) {
                                        Permissions.sendMessageToDebug(wp, Component.text("----------------------------------------", NamedTextColor.RED));
                                        Permissions.sendMessageToDebug(wp,
                                                Component.text(we.getName(), NamedTextColor.AQUA)
                                                         .append(Component.text(" is AFK. (Hasn't moved for 7.5 seconds)", NamedTextColor.RED))
                                        );
                                        Permissions.sendMessageToDebug(wp, Component.text("----------------------------------------", NamedTextColor.RED));
                                    }
                                }
                                continue;
                            }
                            if (lastLocation.equals(secondLastLocation)) {
                                //hasnt moved for 5 seconds
                                for (WarlordsEntity wp : PlayerFilter.playingGame(game)) {
                                    Permissions.sendMessageToDebug(wp, Component.text("----------------------------------------", NamedTextColor.RED));
                                    Permissions.sendMessageToDebug(wp,
                                            Component.text(we.getName(), NamedTextColor.AQUA)
                                                     .append(Component.text(" is AFK. (Hasn't moved for 5 seconds)", NamedTextColor.RED))
                                    );
                                    Permissions.sendMessageToDebug(wp, Component.text("----------------------------------------", NamedTextColor.RED));
                                }
                            }
                        }
                    }
                });
            }
        }.runTaskTimer(20 * 15 + 5, 50); //5 seconds after gates fall - every 2.5 seconds
    }

}
