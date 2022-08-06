package com.ebicep.warlords.util.pve;

import javax.annotation.Nonnull;

// https://minecraft-heads.com/
public enum SkullID {
    FACELESS_BANDIT(
            "10e5ac9b-3c3b-4b2d-98f9-57ccc30e237e",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2VlZTJjYjQxY2VkZTVhYTQ0MTE3MTYyNGUxZTFlMzg4YjgyNjJhNGEwYmI5ZGZiZmQ4ODljYTAyYzQxY2IifX19="
    ),
    FACELESS_MAGE(
            "b31f22fd-3cd3-4183-836b-d98d003df922",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFiOGI2ZDA4YjRhMTdlYjVmMTlkYTNlNTI4MzczYTBkNmQzNjA5ZTEzZmU0OWRjMDIwMDkxNDQ3NWQ4MjNhZiJ9fX0="
    ),
    PURPLE_KNIGHT(
            "c8f8e39e-f616-4564-bdb0-797c7b1c98e3",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmEwM2IzNWQ0NDg1MGNiNDJiMDAwMTdhZGRiN2Y4NWVhYWMyNGI1NmEwY2Q1MWNhMWNhYzIyYjZlYjQyM2UxMSJ9fX0="
    ),
    PURPLE_ENDERMAN(
            "13d9ec9c-2de1-4f0c-b579-2eeaf95b6ca7",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmY5MDIwYzA3ZDg3NWJhZDE0NDAzMzdhZGI1NWEwOGMxNWRiMDZiOTk0NjQ2YTY5MTc5NWY0Y2QyOTNmZTNkZSJ9fX0="
    ),
    NEON_ENDERMAN(
            "88e9a827-7429-4c7f-9f49-dfb8aa2123d0",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJkZWZiZTNjZGUzMjZkNDUxMWJiNTMzMzlkNzc3YWZhNzAzZjNlYzRkYWE2OTdkNjFhNDQwMjc0NGNiYjBjZCJ9fX0="
    ),

    ;

    private final String id;
    private final String textureId;

    /**
     *
     * @param id uuid of the given custom skull.
     * @param textureId texture ID encoded in Base64, cannot be null.
     */
    SkullID(String id, @Nonnull String textureId) {
        this.id = id;
        this.textureId = textureId;
    }

    public String getTextureId() {
        return textureId;
    }

    public String getId() {
        return id;
    }
}