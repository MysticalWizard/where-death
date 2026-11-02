package net.mystwiz.wheredeath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DeathWatcherTest {
	private static final String OVERWORLD = "minecraft:overworld";

	private final DeathWatcher watcher = new DeathWatcher();

	@Test
	void reportsNothingWhileThePlayerStaysAlive() {
		assertNull(watcher.observe(true, 0, 64, 0, OVERWORLD, 1_000L));
		assertNull(watcher.observe(true, 1, 64, 1, OVERWORLD, 2_000L));
	}

	@Test
	void reportsTheDeathWithTheSpotAndTimeItHappened() {
		watcher.observe(true, 0, 64, 0, OVERWORLD, 1_000L);

		DeathWatcher.Event event = watcher.observe(false, 142, 63, -871, OVERWORLD, 5_000L);

		DeathWatcher.Died died = assertInstanceOf(DeathWatcher.Died.class, event);
		assertEquals(new DeathRecord(142, 63, -871, OVERWORLD, 5_000L), died.death());
	}

	@Test
	void reportsOnlyOnceWhileThePlayerStaysDead() {
		watcher.observe(true, 0, 64, 0, OVERWORLD, 1_000L);
		watcher.observe(false, 142, 63, -871, OVERWORLD, 5_000L);

		assertNull(watcher.observe(false, 142, 63, -871, OVERWORLD, 5_050L));
	}

	@Test
	void reportsTheRespawnWithTheDeathItRecorded() {
		watcher.observe(true, 0, 64, 0, OVERWORLD, 1_000L);
		watcher.observe(false, 142, 63, -871, OVERWORLD, 5_000L);

		DeathWatcher.Event event = watcher.observe(true, 0, 64, 0, OVERWORLD, 78_000L);

		DeathWatcher.Respawned respawned = assertInstanceOf(DeathWatcher.Respawned.class, event);
		assertEquals(new DeathRecord(142, 63, -871, OVERWORLD, 5_000L), respawned.death());
		assertEquals(78_000L, respawned.nowMillis());
	}

	@Test
	void reportsNothingForADeathItNeverSaw() {
		// Reconnecting while already dead: the first thing the watcher ever sees is a corpse.
		assertNull(watcher.observe(false, 142, 63, -871, OVERWORLD, 5_000L));
		assertNull(watcher.observe(true, 0, 64, 0, OVERWORLD, 78_000L));
	}

	@Test
	void reportsASecondDeathAfterTheFirstRespawn() {
		watcher.observe(true, 0, 64, 0, OVERWORLD, 1_000L);
		watcher.observe(false, 142, 63, -871, OVERWORLD, 5_000L);
		watcher.observe(true, 0, 64, 0, OVERWORLD, 78_000L);

		DeathWatcher.Event event = watcher.observe(false, 9, 12, 9, "minecraft:the_nether", 90_000L);

		DeathWatcher.Died died = assertInstanceOf(DeathWatcher.Died.class, event);
		assertEquals(new DeathRecord(9, 12, 9, "minecraft:the_nether", 90_000L), died.death());
	}

	@Test
	void resetForgetsAPendingDeath() {
		watcher.observe(true, 0, 64, 0, OVERWORLD, 1_000L);
		watcher.observe(false, 142, 63, -871, OVERWORLD, 5_000L);

		watcher.reset();

		assertNull(watcher.observe(true, 0, 64, 0, OVERWORLD, 78_000L));
	}
}
