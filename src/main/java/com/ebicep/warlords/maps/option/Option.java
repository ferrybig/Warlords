package com.ebicep.warlords.maps.option;

import com.ebicep.warlords.maps.Game;

import javax.annotation.Nonnull;

/**
 * A game exists out of multiple options, who all change the behaviour of the
 * game.
 */
public interface Option {

    /**
     * Registers this option to a game. An Option can only be registered to one
     * game, attempting to register an option to multiple game instances may
     * yield undefined behavior.
     *
     * @param game The game instance
     */
    default void register(@Nonnull Game game) {
    }

    /**
     * Called when the game is started (For a typical game, a transition to the
     * <code>PlayingState</code>). Use this method to start your long running
     * tasks
     *
     * @param game The game instance
     */
    default void start(@Nonnull Game game) {
    }
    
    /**
     * Called when the game transitions to a closed state. Generally, the game
     * no longer accepts players, and players can leave the game without
     * affecting their standings.
     * @param game The game instance
     */
    default void onGameEnding(@Nonnull Game game) {
    }
}
