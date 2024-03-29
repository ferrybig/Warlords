package com.ebicep.warlords.events.game;

import com.ebicep.warlords.game.Game;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

public class WarlordsGameUpdatedEvent extends AbstractWarlordsGameEvent {

    private static final HandlerList handlers = new HandlerList();
    private final String key;

    public WarlordsGameUpdatedEvent(Game game, String key) {
        super(game);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Nonnull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}