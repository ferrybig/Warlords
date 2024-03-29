package com.ebicep.warlords.database.repositories.player.pojos.pve.wavedefense.classes;

import com.ebicep.warlords.database.repositories.player.pojos.AbstractDatabaseStatInformation;
import com.ebicep.warlords.database.repositories.player.pojos.DatabaseWarlordsSpecs;
import com.ebicep.warlords.database.repositories.player.pojos.pve.wavedefense.DatabaseBasePvEWaveDefense;

public class DatabaseShamanPvEWaveDefense extends DatabaseBasePvEWaveDefense implements DatabaseWarlordsSpecs {

    private DatabaseBasePvEWaveDefense thunderlord = new DatabaseBasePvEWaveDefense();
    private DatabaseBasePvEWaveDefense spiritguard = new DatabaseBasePvEWaveDefense();
    private DatabaseBasePvEWaveDefense earthwarden = new DatabaseBasePvEWaveDefense();

    public DatabaseShamanPvEWaveDefense() {
        super();
    }

    @Override
    public AbstractDatabaseStatInformation[] getSpecs() {
        return new DatabaseBasePvEWaveDefense[]{thunderlord, spiritguard, earthwarden};
    }

    public DatabaseBasePvEWaveDefense getThunderlord() {
        return thunderlord;
    }

    public DatabaseBasePvEWaveDefense getSpiritguard() {
        return spiritguard;
    }

    public DatabaseBasePvEWaveDefense getEarthwarden() {
        return earthwarden;
    }

}
