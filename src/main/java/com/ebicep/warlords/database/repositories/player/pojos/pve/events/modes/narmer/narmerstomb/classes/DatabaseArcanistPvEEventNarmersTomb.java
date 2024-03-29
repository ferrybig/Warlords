
package com.ebicep.warlords.database.repositories.player.pojos.pve.events.modes.narmer.narmerstomb.classes;


import com.ebicep.warlords.database.repositories.player.pojos.AbstractDatabaseStatInformation;
import com.ebicep.warlords.database.repositories.player.pojos.DatabaseWarlordsSpecs;
import com.ebicep.warlords.database.repositories.player.pojos.pve.events.modes.narmer.narmerstomb.DatabaseBasePvEEventNarmersTomb;

public class DatabaseArcanistPvEEventNarmersTomb extends DatabaseBasePvEEventNarmersTomb implements DatabaseWarlordsSpecs {

    private DatabaseBasePvEEventNarmersTomb conjurer = new DatabaseBasePvEEventNarmersTomb();
    private DatabaseBasePvEEventNarmersTomb sentinel = new DatabaseBasePvEEventNarmersTomb();
    private DatabaseBasePvEEventNarmersTomb luminary = new DatabaseBasePvEEventNarmersTomb();

    public DatabaseArcanistPvEEventNarmersTomb() {
        super();
    }

    @Override
    public AbstractDatabaseStatInformation[] getSpecs() {
        return new DatabaseBasePvEEventNarmersTomb[]{conjurer, sentinel, luminary};
    }


    public DatabaseBasePvEEventNarmersTomb getConjurer() {
        return conjurer;
    }

    public DatabaseBasePvEEventNarmersTomb getSentinel() {
        return sentinel;
    }

    public DatabaseBasePvEEventNarmersTomb getLuminary() {
        return luminary;
    }

}
