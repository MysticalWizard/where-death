package net.mystwiz.wheredeath;

/** Where and when a player died. Coordinates are the block the player died on. */
public record DeathRecord(int x, int y, int z, String dimensionId, long timeMillis) {
}
