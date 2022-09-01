package com.ebicep.warlords.events;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.abilties.*;
import com.ebicep.warlords.classes.shaman.specs.Spiritguard;
import com.ebicep.warlords.commands.debugcommands.misc.MuteCommand;
import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.database.leaderboards.stats.StatsLeaderboardManager;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGameBase;
import com.ebicep.warlords.database.repositories.player.PlayersCollections;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabasePlayer;
import com.ebicep.warlords.database.repositories.player.pojos.general.FutureMessage;
import com.ebicep.warlords.effects.FireWorkEffectPlayer;
import com.ebicep.warlords.events.game.WarlordsFlagUpdatedEvent;
import com.ebicep.warlords.events.player.WarlordsDeathEvent;
import com.ebicep.warlords.game.GameManager;
import com.ebicep.warlords.game.flags.GroundFlagLocation;
import com.ebicep.warlords.game.flags.PlayerFlagLocation;
import com.ebicep.warlords.game.flags.SpawnFlagLocation;
import com.ebicep.warlords.game.flags.WaitingFlagLocation;
import com.ebicep.warlords.game.option.marker.FlagHolder;
import com.ebicep.warlords.game.state.PreLobbyState;
import com.ebicep.warlords.menu.PlayerHotBarItemListener;
import com.ebicep.warlords.permissions.Permissions;
import com.ebicep.warlords.player.general.CustomScoreboard;
import com.ebicep.warlords.player.general.ExperienceManager;
import com.ebicep.warlords.player.general.Specializations;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.WarlordsNPC;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownFilter;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.PersistentCooldown;
import com.ebicep.warlords.pve.weapons.AbstractWeapon;
import com.ebicep.warlords.util.bukkit.HeadUtils;
import com.ebicep.warlords.util.bukkit.PacketUtils;
import com.ebicep.warlords.util.chat.ChatChannels;
import com.ebicep.warlords.util.chat.ChatUtils;
import com.ebicep.warlords.util.java.DateUtil;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.util.*;

public class WarlordsEvents implements Listener {

    public static Set<Entity> entityList = new HashSet<>();

    public static void addEntityUUID(Entity entity) {
        entityList.add(entity);
    }

    @EventHandler
    public static void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (DatabaseManager.playerService == null && DatabaseManager.enabled) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Please wait!");
        }
    }

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        WarlordsEntity wp = Warlords.getPlayer(player);
        if (wp != null) {
            if (wp.isAlive()) {
                e.getPlayer().setAllowFlight(false);
            }
            e.setJoinMessage(wp.getColoredNameBold() + ChatColor.GOLD + " rejoined the game!");
        } else {
            //checking if in game lobby
            e.getPlayer().setAllowFlight(true);
            e.setJoinMessage(ChatColor.AQUA + e.getPlayer().getName() + ChatColor.GOLD + " joined the lobby!");

            if (DatabaseManager.playerService == null || !DatabaseManager.enabled) {
                HeadUtils.updateHead(e.getPlayer());
            }
            Warlords.newChain()
                    .async(() -> {
                        DatabaseManager.loadPlayer(e.getPlayer().getUniqueId(), PlayersCollections.LIFETIME, () -> {
                            PlayerHotBarItemListener.giveLobbyHotBarDatabase(player);
                            HeadUtils.updateHead(e.getPlayer());

                            Location rejoinPoint = Warlords.getRejoinPoint(player.getUniqueId());
                            if (Bukkit.getWorlds().get(0).equals(rejoinPoint.getWorld())) {
                                if (StatsLeaderboardManager.loaded) {
                                    StatsLeaderboardManager.setLeaderboardHologramVisibility(player);
                                    DatabaseGameBase.setGameHologramVisibility(player);
                                    Warlords.playerScoreboards.get(player.getUniqueId()).giveMainLobbyScoreboard();

                                }
                                ExperienceManager.giveExperienceBar(player);

                                //future messages
                                Warlords.newChain()
                                        .delay(20)
                                        .async(() -> {
                                            DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(player.getUniqueId());
                                            List<FutureMessage> futureMessages = databasePlayer.getFutureMessages();
                                            futureMessages.forEach(futureMessage -> {
                                                if (futureMessage.isCentered()) {
                                                    futureMessage.getMessages().forEach(message -> ChatUtils.sendCenteredMessage(player, message));
                                                } else {
                                                    futureMessage.getMessages().forEach(player::sendMessage);
                                                }
                                            });
                                            databasePlayer.clearFutureMessages();
                                            DatabaseManager.queueUpdatePlayerAsync(databasePlayer);
                                        }).execute();
                            }
                        });
                    })
                    .execute();
        }

        //scoreboard
        if (!Warlords.playerScoreboards.containsKey(player.getUniqueId()) || Warlords.playerScoreboards.get(player.getUniqueId()) == null) {
            Warlords.playerScoreboards.put(player.getUniqueId(), new CustomScoreboard(player));
        }
        player.setScoreboard(Warlords.playerScoreboards.get(player.getUniqueId()).getScoreboard());

        joinInteraction(player, false);

        Bukkit.getOnlinePlayers().forEach(p -> {
            PacketUtils.sendTabHF(p,
                    ChatColor.AQUA + "     Welcome to " + ChatColor.YELLOW + ChatColor.BOLD + "Warlords 2.0     ",
                    ChatColor.GREEN + "Players Online: " + ChatColor.GRAY + Bukkit.getOnlinePlayers().size());
        });
        Warlords.getGameManager().dropPlayerFromQueueOrGames(e.getPlayer());
    }

    public static void joinInteraction(Player player, boolean fromGame) {
        UUID uuid = player.getUniqueId();
        Location rejoinPoint = Warlords.getRejoinPoint(uuid);
        boolean isSpawnWorld = Bukkit.getWorlds().get(0).getName().equals(rejoinPoint.getWorld().getName());
        boolean playerIsInWrongWorld = !player.getWorld().getName().equals(rejoinPoint.getWorld().getName());
        if (isSpawnWorld || playerIsInWrongWorld) {
            player.teleport(rejoinPoint);
        }
        if (playerIsInWrongWorld && isSpawnWorld) {
            player.sendMessage(ChatColor.RED + "The game you were previously playing is no longer running!");
        }
        if (playerIsInWrongWorld && !isSpawnWorld) {
            player.sendMessage(ChatColor.RED + "The game started without you, but we still love you enough and you were warped into the game");
        }
        if (isSpawnWorld) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);
            player.removePotionEffect(PotionEffectType.ABSORPTION);
            player.setGameMode(GameMode.ADVENTURE);

            ChatUtils.sendCenteredMessage(player, ChatColor.GRAY + "-----------------------------------------------------");
            ChatUtils.sendCenteredMessage(player, ChatColor.GOLD + "" + ChatColor.BOLD + "Welcome to Warlords 2.0 " + ChatColor.GRAY + "(" + ChatColor.RED + Warlords.VERSION + ChatColor.GRAY + ")");
            ChatUtils.sendCenteredMessage(player, ChatColor.GOLD + "Developed by " + ChatColor.RED + "sumSmash " + ChatColor.GOLD + "&" + ChatColor.RED + " Plikie");
            ChatUtils.sendCenteredMessage(player, "");
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "Last Updated: " + ChatColor.GOLD + DateUtil.formatCurrentDateEST("d/MM/uuuu"));
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "Currently playable specs - PVE:");
            ChatUtils.sendMessage(player, false,ChatColor.RED + "[DEV] " + ChatColor.GRAY + "- Thunderlord");
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "- Avenger");
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "- Earthwarden");
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "- Berserker");
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "- Crusader");
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "- Protector");
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "- Cryomancer");
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "- Aquamancer");
            ChatUtils.sendMessage(player, false, "");
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "Playable but still has WIP:");
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "- Apothecary");
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "- Pyromancer");
            ChatUtils.sendMessage(player, false, "");
            ChatUtils.sendMessage(player, false, ChatColor.RED + "[DEV] " + ChatColor.GRAY + "Next up: Vindicator, Assassin");
//            ChatUtils.sendCenteredMessage(player, ChatColor.GOLD + "Click the Nether Star or do " + ChatColor.GREEN + "/menu" + ChatColor.GOLD + " to open the selection menu.");
//            ChatUtils.sendCenteredMessage(player, ChatColor.GOLD + "You can start private games using the " + ChatColor.GREEN + "Blaze Powder" + ChatColor.GOLD + " in your inventory!");
//            ChatUtils.sendCenteredMessage(player, "");
//            ChatUtils.sendCenteredMessage(player, ChatColor.GOLD + "Make sure to join our discord if you wish to stay up-to-date with our most recent patches, interact with our community and make bug reports or game suggestions at: " + ChatColor.RED + "§ldiscord.gg/GWPAx9sEG7");
            ChatUtils.sendCenteredMessage(player, "");
            ChatUtils.sendCenteredMessage(player, ChatColor.GOLD + "We highly recommend you to download our resource pack at: " + ChatColor.RED + "§lhttps://bit.ly/3J1lGGn");
            ChatUtils.sendCenteredMessage(player, ChatColor.GRAY + "-----------------------------------------------------");

            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});

            PlayerHotBarItemListener.giveLobbyHotBar(player, fromGame);

            if (fromGame) {
                Warlords.playerScoreboards.get(uuid).giveMainLobbyScoreboard();
                ExperienceManager.giveExperienceBar(player);
                if (DatabaseManager.playerService != null) {
                    //check all spec prestige
                    Warlords.newChain()
                            .asyncFirst(() -> DatabaseManager.playerService.findByUUID(uuid))
                            .abortIfNull()
                            .syncLast(databasePlayer -> {
                                for (Specializations value : Specializations.VALUES) {
                                    int level = ExperienceManager.getLevelForSpec(uuid, value);
                                    if (level >= ExperienceManager.LEVEL_TO_PRESTIGE) {
                                        databasePlayer.getSpec(value).addPrestige();
                                        int prestige = databasePlayer.getSpec(value).getPrestige();
                                        FireWorkEffectPlayer.playFirework(player.getLocation(), FireworkEffect.builder()
                                                .with(FireworkEffect.Type.BALL)
                                                .withColor(ExperienceManager.PRESTIGE_COLORS.get(prestige).getB())
                                                .build()
                                        );
                                        PacketUtils.sendTitle(player, ChatColor.MAGIC + "###" + ChatColor.BOLD + ChatColor.GOLD + " Prestige " + Specializations.CRYOMANCER.name + " " + ChatColor.WHITE + ChatColor.MAGIC + "###", ExperienceManager.PRESTIGE_COLORS.get(prestige - 1).getA().toString() + (prestige - 1) + ChatColor.GRAY + " > " + ExperienceManager.PRESTIGE_COLORS.get(prestige).getA() + prestige, 20, 140, 20);
                                        //sumSmash is now prestige level 5 in Pyromancer!
                                        Bukkit.broadcastMessage(ChatColor.AQUA + player.getName() + ChatColor.GRAY + " is now prestige level " + ExperienceManager.PRESTIGE_COLORS.get(prestige).getA() + prestige + ChatColor.GRAY + " in " + ChatColor.GOLD + value.name);
                                    }
                                }
                                DatabaseManager.queueUpdatePlayerAsync(databasePlayer);
                            })
                            .execute();
                }
            }
        }

        WarlordsEntity wp1 = Warlords.getPlayer(player);
        WarlordsPlayer p = wp1 instanceof WarlordsPlayer ? (WarlordsPlayer) wp1 : null;
        if (p != null) {
            player.teleport(p.getLocation());
            p.updatePlayerReference(player);
        } else {
            player.setAllowFlight(true);
        }

        Warlords.getInstance().hideAndUnhidePeople(player);
    }

    @EventHandler
    public static void onPlayerQuit(PlayerQuitEvent e) {
        WarlordsEntity wp1 = Warlords.getPlayer(e.getPlayer());
        WarlordsPlayer wp = wp1 instanceof WarlordsPlayer ? (WarlordsPlayer) wp1 : null;
        if (wp != null) {
            wp.updatePlayerReference(null);
            e.setQuitMessage(wp.getColoredNameBold() + ChatColor.GOLD + " left the game!");
        } else {
            e.setQuitMessage(ChatColor.AQUA + e.getPlayer().getName() + ChatColor.GOLD + " left the lobby!");
        }
        if (e.getPlayer().getVehicle() != null) {
            e.getPlayer().getVehicle().remove();
        }
        //removing player position boards
        StatsLeaderboardManager.removePlayerSpecificHolograms(e.getPlayer());

        Bukkit.getOnlinePlayers().forEach(p -> {
            PacketUtils.sendTabHF(p, ChatColor.AQUA + "     Welcome to " + ChatColor.YELLOW + ChatColor.BOLD + "Warlords 2.0     ", ChatColor.GREEN + "Players Online: " + ChatColor.GRAY + (Bukkit.getOnlinePlayers().size() - 1));
        });

        for (GameManager.GameHolder holder : Warlords.getGameManager().getGames()) {
            if (
                    holder.getGame() != null
                            && holder.getGame().hasPlayer(e.getPlayer().getUniqueId())
                            && holder.getGame().getPlayerTeam(e.getPlayer().getUniqueId()) == null
            ) {
                holder.getGame().removePlayer(e.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            if (entityList.remove(event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        Entity attacker = e.getDamager();
        WarlordsEntity wpAttacker = Warlords.getPlayer(attacker);
        WarlordsEntity wpVictim = Warlords.getPlayer(e.getEntity());
        if (wpAttacker != null && wpVictim != null && wpAttacker.isEnemyAlive(wpVictim) && !wpAttacker.getGame().isFrozen()) {
            if ((!(attacker instanceof Player) || ((Player) attacker).getInventory().getHeldItemSlot() == 0) && wpAttacker.getHitCooldown() == 0) {

                wpAttacker.setHitCooldown(12);
                wpAttacker.subtractEnergy(-wpAttacker.getSpec().getEnergyOnHit(), true);

                if (wpAttacker.getSpec() instanceof Spiritguard && wpAttacker.getCooldownManager().hasCooldown(Soulbinding.class)) {
                    Soulbinding baseSoulBinding = (Soulbinding) wpAttacker.getSpec().getPurple();
                    new CooldownFilter<>(wpAttacker, PersistentCooldown.class)
                            .filter(PersistentCooldown::isShown)
                            .filterCooldownClassAndMapToObjectsOfClass(Soulbinding.class)
                            .forEachOrdered(soulbinding -> {
                                wpAttacker.doOnStaticAbility(Soulbinding.class, Soulbinding::addPlayersBinded);
                                if (soulbinding.hasBoundPlayer(wpVictim)) {
                                    soulbinding.getSoulBindedPlayers().stream()
                                            .filter(p -> p.getBoundPlayer() == wpVictim)
                                            .forEach(boundPlayer -> {
                                                boundPlayer.setHitWithSoul(false);
                                                boundPlayer.setHitWithLink(false);
                                                boundPlayer.setTimeLeft(baseSoulBinding.getBindDuration());
                                            });
                                } else {
                                    wpVictim.sendMessage(
                                            WarlordsEntity.RECEIVE_ARROW_RED +
                                                    ChatColor.GRAY + "You have been bound by " +
                                                    wpAttacker.getName() + "'s " +
                                                    ChatColor.LIGHT_PURPLE + "Soulbinding Weapon" +
                                                    ChatColor.GRAY + "!"
                                    );
                                    wpAttacker.sendMessage(
                                            WarlordsEntity.GIVE_ARROW_GREEN +
                                                    ChatColor.GRAY + "Your " +
                                                    ChatColor.LIGHT_PURPLE + "Soulbinding Weapon " +
                                                    ChatColor.GRAY + "has bound " +
                                                    wpVictim.getName() + "!"
                                    );
                                    soulbinding.getSoulBindedPlayers().add(new Soulbinding.SoulBoundPlayer(wpVictim, baseSoulBinding.getBindDuration()));
                                    Utils.playGlobalSound(wpVictim.getLocation(), "shaman.earthlivingweapon.activation", 2, 1);
                                }
                            });
                }

                if (wpAttacker instanceof WarlordsNPC) {
                    WarlordsNPC warlordsNPC = (WarlordsNPC) wpAttacker;
                    if (!(warlordsNPC.getMinMeleeDamage() == 0)) {
                        wpVictim.addDamageInstance(
                                wpAttacker,
                                "",
                                warlordsNPC.getMinMeleeDamage(),
                                warlordsNPC.getMaxMeleeDamage(),
                                -1,
                                100,
                                false
                        );
                    }
                    wpAttacker.setHitCooldown(20);
                } else {
                    if (wpAttacker instanceof WarlordsPlayer && ((WarlordsPlayer) wpAttacker).getAbstractWeapon() != null) {
                        AbstractWeapon weapon = ((WarlordsPlayer) wpAttacker).getAbstractWeapon();
                        wpVictim.addDamageInstance(
                                wpAttacker,
                                "",
                                weapon.getMeleeDamageMin(),
                                weapon.getMeleeDamageMax(),
                                weapon.getCritChance(),
                                weapon.getCritMultiplier(),
                                false
                        );
                    } else {
                        wpVictim.addDamageInstance(
                                wpAttacker,
                                "",
                                132,
                                179,
                                25,
                                200,
                                false
                        );
                    }
                }
                wpVictim.updateHealth();
            }

            if (wpVictim.getCooldownManager().hasCooldown(IceBarrier.class)) {
                wpAttacker.getSpeed().addSpeedModifier("Ice Barrier", -20, 2 * 20);
            }
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        e.setCancelled(true);
        WarlordsEntity entity = Warlords.getPlayer((Entity) e.getEntity().getShooter());
        if (entity != null) {
            //entity.getSpec().getWeapon().onActivate(entity, null);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();
        Location location = player.getLocation();
        WarlordsEntity wp = Warlords.getPlayer(player);

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            ItemStack itemHeld = player.getItemInHand();
            if (wp != null && wp.isAlive() && !wp.getGame().isFrozen()) {
                switch (itemHeld.getType()) {
                    case GOLD_BARDING:
                        if (player.getInventory().getHeldItemSlot() == 7 && player.getVehicle() == null && wp.getHorseCooldown() <= 0) {
                            if (!Utils.isMountableZone(location) || Utils.blocksInFrontOfLocation(location)) {
                                player.sendMessage(ChatColor.RED + "You can't mount here!");
                            } else {
                                double distance = Utils.getDistance(player, .25);
                                if (distance >= 2) {
                                    player.sendMessage(ChatColor.RED + "You can't mount in the air!");
                                } else if (wp.getCarriedFlag() != null) {
                                    player.sendMessage(ChatColor.RED + "You can't mount while holding the flag!");
                                } else {
                                    player.playSound(player.getLocation(), "mountup", 1, 1);
                                    wp.getHorse().spawn();
                                    if (!wp.isDisableCooldowns()) {
                                        wp.setHorseCooldown((float) (wp.getHorse()
                                                                       .getCooldown() * wp.getCooldownModifier()));
                                    }
                                }
                            }
                        }
                        break;
                    case BONE:
                        player.getInventory().remove(UndyingArmy.BONE);
                        wp.addDamageInstance(
                                Warlords.getPlayer(player),
                                "",
                                100000,
                                100000,
                                -1,
                                100,
                                false
                        );
                        break;
                    case BANNER:
                        if (wp.getFlagDropCooldown() > 0) {
                            player.sendMessage("§cYou cannot drop the flag yet, please wait 3 seconds!");
                        } else if (wp.getCooldownManager().hasCooldown(TimeWarp.class)) {
                            player.sendMessage(ChatColor.RED + "You cannot drop the flag with a Time Warp active!");
                        } else {
                            FlagHolder.dropFlagForPlayer(wp);
                            wp.setFlagDropCooldown(5);
                        }
                        break;
                    case COMPASS:
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 2);
                        wp.toggleTeamFlagCompass();
                        break;
                    case GOLD_NUGGET:
                        player.playSound(player.getLocation(), Sound.DIG_SNOW, 500, 2);
                        ((WarlordsPlayer) wp).getAbilityTree().openAbilityTree();
                        break;
                    default:
                        if (player.getInventory().getHeldItemSlot() == 0 || !Warlords.getPlayerSettings(wp.getUuid()).getHotKeyMode()) {
                            wp.getSpec().onRightClick(wp, player, player.getInventory().getHeldItemSlot(), false);
                        }
                        break;
                }
            } else {
                Warlords.getGameManager().getPlayerGame(player.getUniqueId())
                        .flatMap(g -> g.getState(PreLobbyState.class))
                        .ifPresent(state -> state.interactEvent(player, player.getInventory().getHeldItemSlot()));
            }
        } else if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
            if (action == Action.LEFT_CLICK_AIR) {

            }
        }

    }

    @EventHandler
    public void onMount(VehicleEnterEvent e) {
    }

    @EventHandler
    public void onDismount(VehicleExitEvent e) {
        e.getVehicle().remove();
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) {
            WarlordsEntity warlordsPlayer = Warlords.getPlayer(e.getPlayer().getUniqueId());
            if (warlordsPlayer == null) {
                return;
            }
            if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                e.setCancelled(true);
                e.getPlayer().setSpectatorTarget(null);
            }
        }
    }

    @EventHandler
    public void regenEvent(EntityRegainHealthEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void pickUpItem(PlayerArmorStandManipulateEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void switchItemHeld(PlayerItemHeldEvent e) {
        int slot = e.getNewSlot();
        WarlordsEntity wp = Warlords.getPlayer(e.getPlayer());
        if (wp != null) {
            if (Warlords.getPlayerSettings(wp.getUuid()).getHotKeyMode() && (slot == 1 || slot == 2 || slot == 3 || slot == 4)) {
                wp.getSpec().onRightClick(wp, e.getPlayer(), slot, true);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onOpenInventory(InventoryOpenEvent e) {
        if (e.getPlayer().getVehicle() != null) {
            if (e.getInventory().getHolder() != null && e.getInventory().getHolder().getInventory().getTitle().equals("Horse")) {
                e.setCancelled(true);
            }
        }

        if (e.getInventory() instanceof CraftInventoryAnvil ||
                e.getInventory() instanceof CraftInventoryBeacon ||
                e.getInventory() instanceof CraftInventoryBrewer ||
                e.getInventory() instanceof CraftInventoryCrafting ||
                e.getInventory() instanceof CraftInventoryDoubleChest ||
                e.getInventory() instanceof CraftInventoryFurnace ||
                e.getInventory().getType() == InventoryType.HOPPER ||
                e.getInventory().getType() == InventoryType.DROPPER
        ) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropEvent(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onHorseJump(HorseJumpEvent e) {
        if (Warlords.hasPlayer((OfflinePlayer) e.getEntity().getPassenger())) {
            if (Objects.requireNonNull(Warlords.getPlayer(e.getEntity().getPassenger())).getGame().isFrozen()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.getPlayer().getVehicle() instanceof Horse) {
            Location location = e.getPlayer().getLocation();
            if (!Utils.isMountableZone(location)) {
                e.getPlayer().getVehicle().remove();
            }
        }

        WarlordsEntity warlordsEntity = Warlords.getPlayer(e.getPlayer());
        if (warlordsEntity != null) {
            warlordsEntity.setCurrentVector(e.getTo().toVector().subtract(e.getFrom().toVector()).normalize().clone());
            //System.out.println(warlordsEntity.getCurrentVector());
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                e.getEntity().teleport(Warlords.getRejoinPoint(e.getEntity().getUniqueId()));
                WarlordsEntity wp = Warlords.getPlayer(e.getEntity());
                if (wp != null) {
                    if (wp.isDead()) {
                        wp.getEntity().teleport(wp.getLocation().clone().add(0, 100, 0));
                    } else {
                        wp.addDamageInstance(wp, "Fall", 1000000, 1000000, -1, 100, false);
                    }
                }
            } else if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                //HEIGHT - DAMAGE
                //PLAYER
                //9 - 160 - 6
                //15 - 400 - 12
                //30ish - 1040

                //HORSE
                //HEIGHT - DAMAGE
                //18 - 160
                //HEIGHT x 40 - 200
                if (e.getEntity() instanceof Player) {
                    WarlordsEntity wp = Warlords.getPlayer(e.getEntity());
                    if (wp != null) {
                        int damage = (int) e.getDamage();
                        if (damage > 5) {
                            wp.addDamageInstance(wp, "Fall", ((damage + 3) * 40 - 200), ((damage + 3) * 40 - 200), -1, 100, false);
                            wp.setRegenTimer(10);
                        }
                    }
                }
            } else if (e.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
                //100 flat
                if (e.getEntity() instanceof Player) {
                    WarlordsEntity wp = Warlords.getPlayer(e.getEntity());
                    if (wp != null && !wp.getGame().isFrozen()) {
                        wp.addDamageInstance(wp, "Fall", 100, 100, -1, 100, false);
                        wp.setRegenTimer(10);
                    }
                }
            }
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        e.getDrops().clear();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        e.getBlock().getDrops().clear();
        //e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (MuteCommand.mutedPlayers.getOrDefault(uuid, false)) {
            e.setCancelled(true);
            return;
        }

        if (!Warlords.playerChatChannels.containsKey(uuid) || Warlords.playerChatChannels.get(uuid) == null) {
            Warlords.playerChatChannels.put(uuid, ChatChannels.ALL);
        }

        String prefixWithColor = Permissions.getPrefixWithColor(player);
        if (prefixWithColor.equals(ChatColor.WHITE.toString())) {
            ChatUtils.MessageTypes.WARLORDS.sendErrorMessage("Player has invalid rank or permissions have not been set up properly!");
        }

        ChatChannels channel = Warlords.playerChatChannels.getOrDefault(uuid, ChatChannels.ALL);
        channel.onPlayerChatEvent(e, prefixWithColor);
    }

    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        Player player = event.getPlayer();
        EntityDamageEvent lastDamage = player.getLastDamageCause();

        if ((!(lastDamage instanceof EntityDamageByEntityEvent))) {
            return;
        }

        if ((((EntityDamageByEntityEvent) lastDamage).getDamager() instanceof Player))
            event.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent change) {
        change.setCancelled(true);
        if (change.getWorld().hasStorm()) {
            change.getWorld().setWeatherDuration(0);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent change) {
        change.setCancelled(true);
        if (change.getEntity() instanceof Player) {
            ((Player) change.getEntity()).setFoodLevel(20);
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onFlagChange(WarlordsFlagUpdatedEvent event) {
        //Bukkit.broadcastMessage(event.getTeam() + " " + event.getOld().getClass().getSimpleName() + " => " + event.getNew().getClass().getSimpleName());
        if (event.getOld() instanceof PlayerFlagLocation) {
            ((PlayerFlagLocation) event.getOld()).getPlayer().setCarriedFlag(null);
        }

        if (event.getNew() instanceof PlayerFlagLocation) {
            PlayerFlagLocation pfl = (PlayerFlagLocation) event.getNew();
            WarlordsEntity player = pfl.getPlayer();
            player.setCarriedFlag(event.getInfo());
            //removing invis for assassins
            OrderOfEviscerate.removeCloak(player, false);
            if (event.getOld() instanceof PlayerFlagLocation) {
                // PLAYER -> PLAYER only happens if the multiplier gets to a new scale
                if (pfl.getComputedHumanMultiplier() % 10 == 0) {
                    event.getGame().forEachOnlinePlayerWithoutSpectators((p, t) -> {
                        p.sendMessage("§eThe " + event.getTeam().coloredPrefix() + " §eflag carrier now takes §c" + pfl.getComputedHumanMultiplier() + "% §eincreased damage!");
                    });
                    event.getGame().spectators().forEach(uuid -> {
                        if (Bukkit.getPlayer(uuid) != null) {
                            Player p = Bukkit.getPlayer(uuid);
                            p.sendMessage("§eThe " + event.getTeam().coloredPrefix() + " §eflag carrier now takes §c" + pfl.getComputedHumanMultiplier() + "% §eincreased damage!");
                        }
                    });
                }
            } else {
                // eg GROUND -> PLAYER
                // or SPAWN -> PLAYER
                ChatColor enemyColor = event.getTeam().enemy().teamColor();
                event.getGame().forEachOnlinePlayerWithoutSpectators((p, t) -> {
                    p.sendMessage(enemyColor + player.getName() + " §epicked up the " + event.getTeam().coloredPrefix() + " §eflag!");
                    PacketUtils.sendTitle(p, "", enemyColor + player.getName() + " §epicked up the " + event.getTeam().coloredPrefix() + " §eflag!", 0, 60, 0);
                    if (t == event.getTeam()) {
                        p.playSound(player.getLocation(), "ctf.friendlyflagtaken", 500, 1);
                    } else {
                        p.playSound(player.getLocation(), "ctf.enemyflagtaken", 500, 1);
                    }
                });
                event.getGame().spectators().forEach(uuid -> {
                    if (Bukkit.getPlayer(uuid) != null) {
                        Player p = Bukkit.getPlayer(uuid);
                        p.sendMessage(enemyColor + player.getName() + " §epicked up the " + event.getTeam().coloredPrefix() + " §eflag!");
                        PacketUtils.sendTitle(p, "", enemyColor + player.getName() + " §epicked up the " + event.getTeam().coloredPrefix() + " §eflag!", 0, 60, 0);
                    }
                });
            }
        } else if (event.getNew() instanceof SpawnFlagLocation) {
            WarlordsEntity toucher = ((SpawnFlagLocation) event.getNew()).getFlagReturner();
            if (event.getOld() instanceof GroundFlagLocation) {
                if (toucher != null) {
                    toucher.addFlagReturn();
                    event.getGame().forEachOnlinePlayer((p, t) -> {
                        ChatColor color = event.getTeam().teamColor();
                        p.sendMessage(color + toucher.getName() + " §ehas returned the " + event.getTeam().coloredPrefix() + " §eflag!");
                        PacketUtils.sendTitle(p, "", color + toucher.getName() + " §ehas returned the " + event.getTeam().coloredPrefix() + " §eflag!", 0, 60, 0);
                        if (t == event.getTeam()) {
                            p.playSound(p.getLocation(), "ctf.flagreturned", 500, 1);
                        }
                    });
                } else {
                    event.getGame().forEachOnlinePlayer((p, t) -> {
                        p.sendMessage("§eThe " + event.getTeam().coloredPrefix() + " §eflag has returned to its base.");
                    });
                }
            }
        } else if (event.getNew() instanceof GroundFlagLocation) {
            if (event.getOld() instanceof PlayerFlagLocation) {
                PlayerFlagLocation pfl = (PlayerFlagLocation) event.getOld();
                pfl.getPlayer().updateArmor();
                String flag = event.getTeam().coloredPrefix();
                ChatColor playerColor = event.getTeam().enemy().teamColor();
                event.getGame().forEachOnlinePlayer((p, t) -> {
                    PacketUtils.sendTitle(p, "", playerColor + pfl.getPlayer().getName() + " §ehas dropped the " + flag + " §eflag!", 0, 60, 0);
                    p.sendMessage(playerColor + pfl.getPlayer().getName() + " §ehas dropped the " + flag + " §eflag!");
                });
            }
        } else if (event.getNew() instanceof WaitingFlagLocation && ((WaitingFlagLocation) event.getNew()).getScorer() != null) {
            WarlordsEntity player = ((WaitingFlagLocation) event.getNew()).getScorer();
            player.addFlagCap();
            event.getGame().forEachOnlinePlayer((p, t) -> {
                String message = player.getColoredName() + " §ecaptured the " + event.getInfo().getTeam().coloredPrefix() + " §eflag!";
                p.sendMessage(message);
                PacketUtils.sendTitle(p, "", message, 0, 60, 0);

                if (t != null) {
                    if (event.getTeam() == t) {
                        p.playSound(player.getLocation(), "ctf.enemycapturedtheflag", 500, 1);
                    } else {
                        p.playSound(player.getLocation(), "ctf.enemyflagcaptured", 500, 1);
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        dropFlag(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(WarlordsDeathEvent event) {
        dropFlag(event.getPlayer());
    }

    public boolean dropFlag(Player player) {
        return dropFlag(Warlords.getPlayer(player));
    }

    public boolean dropFlag(@Nullable WarlordsEntity player) {
        if (player == null) {
            return false;
        }
        FlagHolder.dropFlagForPlayer(player);
        return true;
    }
}
