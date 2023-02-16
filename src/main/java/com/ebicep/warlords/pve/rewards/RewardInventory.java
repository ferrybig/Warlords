package com.ebicep.warlords.pve.rewards;

import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.database.repositories.player.pojos.pve.DatabasePlayerPvE;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.menu.generalmenu.WarlordsNewHotbarMenu;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RewardInventory {

    public static void sendRewardMessage(UUID uuid, Component component) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.getPlayer() != null) {
            offlinePlayer.getPlayer().sendMessage(Component.text(ChatColor.GOLD + "Reward" + ChatColor.DARK_GRAY + " > ").append(component));
        }
    }

    public static void openRewardInventory(Player player, int page) {
        DatabaseManager.getPlayer(player.getUniqueId(), databasePlayer -> {
            DatabasePlayerPvE databasePlayerPvE = databasePlayer.getPveStats();
            List<AbstractReward> rewards = Stream.of(
                                                         databasePlayerPvE.getMasterworksFairRewards(),
                                                         databasePlayerPvE.getPatreonRewards(),
                                                         databasePlayerPvE.getCompensationRewards(),
                                                         databasePlayerPvE.getGameEventRewards()
                                                 )
                                                 .flatMap(List::stream)
                                                 .filter(reward -> reward.getTimeClaimed() == null)
                                                 .collect(Collectors.toList());
            if (rewards.isEmpty()) {
                player.sendMessage(ChatColor.RED + "You have no rewards to claim!");
                return;
            }

            Menu menu = new Menu("Your Rewards", 9 * 6);
            for (int i = 0; i < 45; i++) {
                int rewardNumber = ((page - 1) * 45) + i;
                if (rewardNumber < rewards.size()) {
                    AbstractReward reward = rewards.get(rewardNumber);
                    menu.setItem(i % 9, i / 9,
                            reward.getItem(),
                            (m, e) -> {
                                reward.giveToPlayer(databasePlayer);
                                DatabaseManager.queueUpdatePlayerAsync(databasePlayer);

                                sendRewardMessage(
                                        player.getUniqueId(),
                                        Component.text(ChatColor.GREEN + "Claimed: ")
                                                 .append(Component.text(reward.getNameColor() + reward.getFrom() + " Reward")
                                                                  .hoverEvent(reward.getItemWithoutClaim().asHoverEvent()))
                                                 .hoverEvent(HoverEvent.showText(Component.text(reward.getNameColor() + reward.getFrom() + " Reward")))
                                );

                                if (rewards.size() > 1) {
                                    openRewardInventory(player, page);
                                } else {
                                    player.closeInventory();
                                }
                            }
                    );

                } else {
                    break;
                }
            }

            menu.setItem(3, 5,
                    new ItemBuilder(Material.GOLD_BLOCK)
                            .name(ChatColor.GREEN + "Click to claim all rewards!")
                            .get(),
                    (m, e) -> {
                        for (AbstractReward reward : rewards) {
                            reward.giveToPlayer(databasePlayer);

                            sendRewardMessage(
                                    player.getUniqueId(),
                                    Component.text(ChatColor.GREEN + "Claimed: ")
                                             .append(Component.text(reward.getNameColor() + reward.getFrom() + " Reward")
                                                              .hoverEvent(reward.getItemWithoutClaim().asHoverEvent()))
                                             .hoverEvent(HoverEvent.showText(Component.text(reward.getNameColor() + reward.getFrom() + " Reward")))
                            );
                        }
                        DatabaseManager.queueUpdatePlayerAsync(databasePlayer);
                        player.closeInventory();
                    }
            );

            if (page - 1 > 0) {
                menu.setItem(0, 5,
                        new ItemBuilder(Material.ARROW)
                                .name(ChatColor.GREEN + "Previous Page")
                                .lore(ChatColor.YELLOW + "Page " + (page - 1))
                                .get(),
                        (m, e) -> {
                            openRewardInventory(player, page - 1);
                        }
                );
            }
            if (rewards.size() > (page * 45)) {
                menu.setItem(8, 5,
                        new ItemBuilder(Material.ARROW)
                                .name(ChatColor.GREEN + "Next Page")
                                .lore(ChatColor.YELLOW + "Page " + (page + 1))
                                .get(),
                        (m, e) -> {
                            openRewardInventory(player, page + 1);
                        }
                );
            }


            menu.setItem(4, 5, WarlordsNewHotbarMenu.PvEMenu.MENU_BACK_PVE, (m, e) -> WarlordsNewHotbarMenu.PvEMenu.openPvEMenu(player));
            menu.openForPlayer(player);
        });
    }

}
