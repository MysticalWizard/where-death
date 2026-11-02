package net.mystwiz.wheredeath;

import org.jetbrains.annotations.Nullable;

/**
 * Turns a stream of "is the player alive right now" observations into death and respawn events.
 * The client has no death event to hook, so it watches the local player tick by tick instead.
 *
 * <p>A respawn is only reported when this watcher saw the matching death, so reconnecting into an
 * already-dead player reports nothing rather than inventing a death it knows nothing about.
 */
public final class DeathWatcher {
	public sealed interface Event permits Died, Respawned {
	}

	public record Died(DeathRecord death) implements Event {
	}

	public record Respawned(DeathRecord death, long nowMillis) implements Event {
	}

	private @Nullable Boolean wasAlive;
	private @Nullable DeathRecord pendingDeath;

	public @Nullable Event observe(boolean alive, int x, int y, int z, String dimensionId, long nowMillis) {
		Boolean previouslyAlive = wasAlive;
		wasAlive = alive;

		if (alive) {
			if (!Boolean.FALSE.equals(previouslyAlive) || pendingDeath == null) {
				return null;
			}

			DeathRecord death = pendingDeath;
			pendingDeath = null;
			return new Respawned(death, nowMillis);
		}

		if (!Boolean.TRUE.equals(previouslyAlive)) {
			return null;
		}

		pendingDeath = new DeathRecord(x, y, z, dimensionId, nowMillis);
		return new Died(pendingDeath);
	}

	/** Forgets everything. Called when the player leaves the world, so state never crosses sessions. */
	public void reset() {
		wasAlive = null;
		pendingDeath = null;
	}
}
