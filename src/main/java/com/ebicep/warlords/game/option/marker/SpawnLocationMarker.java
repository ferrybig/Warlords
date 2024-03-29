package com.ebicep.warlords.game.option.marker;

import com.ebicep.warlords.player.ingame.WarlordsEntity;

public interface SpawnLocationMarker extends LocationMarker {

    /**
     * Get the priority of this spawnpoint
     *
     * @param player the player to check
     * @return the priority, higher priorities should be preferred above lower
     * ones
     */
    double getPriority(WarlordsEntity player);
}
