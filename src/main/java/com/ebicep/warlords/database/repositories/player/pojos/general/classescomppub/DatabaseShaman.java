package com.ebicep.warlords.database.repositories.player.pojos.general.classescomppub;

import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGameBase;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGamePlayerBase;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGamePlayerResult;
import com.ebicep.warlords.database.repositories.player.PlayersCollections;
import com.ebicep.warlords.database.repositories.player.pojos.AbstractDatabaseStatInformation;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabasePlayer;
import com.ebicep.warlords.game.GameMode;

public class DatabaseShaman extends AbstractDatabaseStatInformation {

    protected DatabaseBaseSpec thunderlord = new DatabaseBaseSpec();
    protected DatabaseBaseSpec spiritguard = new DatabaseBaseSpec();
    protected DatabaseBaseSpec earthwarden = new DatabaseBaseSpec();

    public DatabaseShaman() {
    }

    @Override
    public void updateCustomStats(
            DatabasePlayer databasePlayer, DatabaseGameBase databaseGame,
            GameMode gameMode,
            DatabaseGamePlayerBase gamePlayer,
            DatabaseGamePlayerResult result,
            int multiplier,
            PlayersCollections playersCollection
    ) {
        //UPDATE SPEC EXPERIENCE
        this.experience += gamePlayer.getExperienceEarnedSpec() * multiplier;
    }

    public DatabaseBaseSpec getThunderlord() {
        return thunderlord;
    }

    public void setThunderlord(DatabaseBaseSpec thunderlord) {
        this.thunderlord = thunderlord;
    }

    public DatabaseBaseSpec getSpiritguard() {
        return spiritguard;
    }

    public void setSpiritguard(DatabaseBaseSpec spiritguard) {
        this.spiritguard = spiritguard;
    }

    public DatabaseBaseSpec getEarthwarden() {
        return earthwarden;
    }

    public void setEarthwarden(DatabaseBaseSpec earthwarden) {
        this.earthwarden = earthwarden;
    }
}