package com.ebicep.warlords.game.option.respawn;

import com.ebicep.warlords.events.player.ingame.WarlordsDeathEvent;
import com.ebicep.warlords.events.player.ingame.WarlordsRespawnEvent;
import com.ebicep.warlords.events.player.ingame.pve.WarlordsGiveRespawnEvent;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.option.Option;
import com.ebicep.warlords.game.option.marker.TimerSkipAbleMarker;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

public class RespawnWaveOption implements Option, Listener {
    public static final int DEFAULT_INITIAL_DELAY = 0;
    public static final int DEFAULT_TASK_PERIOD = 12;
    public static final int DEFAULT_MIN_RESPAWN_TIMER = 5;

    private final int initialDelay;
    private final int taskPeriod;
    private final int minRespawnTimer;
    private int currentTimer = 0;

    public RespawnWaveOption() {
        this(DEFAULT_INITIAL_DELAY, DEFAULT_TASK_PERIOD, DEFAULT_MIN_RESPAWN_TIMER);
    }

    public RespawnWaveOption(int initialTaskDelay, int taskPeriod, int minRespawnTimer) {
        this.initialDelay = initialTaskDelay;
        this.taskPeriod = taskPeriod;
        this.minRespawnTimer = minRespawnTimer;
    }

    @Override
    public void register(@Nonnull Game game) {
        game.registerEvents(this);
        game.registerGameMarker(TimerSkipAbleMarker.class, (delayInTicks) -> {
            currentTimer += delayInTicks / 20;
            for (WarlordsEntity player : PlayerFilter.playingGame(game)) {
                if (player.getRespawnTickTimer() >= 0) {
                    player.setRespawnTimerSeconds(Math.max((player.getRespawnTickTimer() / 20) - (delayInTicks * 20), 0));
                }
            }
        });
        this.currentTimer = -initialDelay;
    }

    @Override
    public void start(@Nonnull Game game) {
        new GameRunnable(game) {
            @Override
            public void run() {
                currentTimer++;
                for (WarlordsEntity player : PlayerFilter.playingGame(game)) {
                    if (player.isDead() && player.isOnline() && player.getRespawnTickTimer() == -1) {
                        giveRespawnTimer(player);
                    }
                }
            }
        }.runTaskTimer(GameRunnable.SECOND, GameRunnable.SECOND);
    }
    
    @EventHandler
    public void onEvent(WarlordsDeathEvent event) {
        giveRespawnTimer(event.getWarlordsEntity());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(WarlordsRespawnEvent event) {
        if (event.isCancelled()) {
            if (event.getWarlordsEntity().getRespawnTickTimer() == 0) {
                int respawn = -currentTimer % this.taskPeriod;
                while (respawn < 1) {
                    respawn += this.taskPeriod;
                }
                event.getWarlordsEntity().setRespawnTimerSeconds(respawn);
            }
        }
    }

    public void giveRespawnTimer(WarlordsEntity player) {
        int respawn = -currentTimer % this.taskPeriod;
        while (respawn < minRespawnTimer) {
            respawn += this.taskPeriod;
        }
        AtomicInteger respawnTime = new AtomicInteger(respawn);
        Bukkit.getPluginManager().callEvent(new WarlordsGiveRespawnEvent(player, respawnTime));
        player.setRespawnTimerSeconds(Math.max(2, respawnTime.get()));
    }
    
}
