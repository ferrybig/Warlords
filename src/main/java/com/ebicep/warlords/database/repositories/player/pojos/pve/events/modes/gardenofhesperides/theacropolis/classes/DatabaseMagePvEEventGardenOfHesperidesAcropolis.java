package com.ebicep.warlords.database.repositories.player.pojos.pve.events.modes.gardenofhesperides.theacropolis.classes;

import com.ebicep.warlords.database.repositories.player.pojos.AbstractDatabaseStatInformation;
import com.ebicep.warlords.database.repositories.player.pojos.DatabaseWarlordsSpecs;
import com.ebicep.warlords.database.repositories.player.pojos.pve.events.modes.gardenofhesperides.theacropolis.DatabaseBasePvEEventGardenOfHesperidesAcropolis;

public class DatabaseMagePvEEventGardenOfHesperidesAcropolis extends DatabaseBasePvEEventGardenOfHesperidesAcropolis implements DatabaseWarlordsSpecs {

    protected DatabaseBasePvEEventGardenOfHesperidesAcropolis pyromancer = new DatabaseBasePvEEventGardenOfHesperidesAcropolis();
    protected DatabaseBasePvEEventGardenOfHesperidesAcropolis cryomancer = new DatabaseBasePvEEventGardenOfHesperidesAcropolis();
    protected DatabaseBasePvEEventGardenOfHesperidesAcropolis aquamancer = new DatabaseBasePvEEventGardenOfHesperidesAcropolis();

    public DatabaseMagePvEEventGardenOfHesperidesAcropolis() {
        super();
    }

    @Override
    public AbstractDatabaseStatInformation[] getSpecs() {
        return new DatabaseBasePvEEventGardenOfHesperidesAcropolis[]{pyromancer, cryomancer, aquamancer};
    }

    public DatabaseBasePvEEventGardenOfHesperidesAcropolis getPyromancer() {
        return pyromancer;
    }

    public DatabaseBasePvEEventGardenOfHesperidesAcropolis getCryomancer() {
        return cryomancer;
    }

    public DatabaseBasePvEEventGardenOfHesperidesAcropolis getAquamancer() {
        return aquamancer;
    }

}
