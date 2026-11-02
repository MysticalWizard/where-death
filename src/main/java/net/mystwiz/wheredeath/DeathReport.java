package net.mystwiz.wheredeath;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Builds the human-readable strings this mod shows. Deliberately free of Minecraft types so the
 * wording and arithmetic can be tested without a game running.
 */
public final class DeathReport {
	private static final Map<String, String> VANILLA_DIMENSION_NAMES = Map.of(
			"minecraft:overworld", "the Overworld",
			"minecraft:the_nether", "the Nether",
			"minecraft:the_end", "the End");

	private DeathReport() {
	}

	/**
	 * The respawn message, split where it needs different styling. The coordinates are their own
	 * piece so they can be made clickable; {@code suggestion} is what a click puts in the chat box.
	 */
	public record RespawnReport(String prefix, String coordinates, String suggestion, String suffix) {
		public String plainText() {
			return prefix + coordinates + suffix;
		}
	}

	public static String formatDuration(long millis) {
		long totalSeconds = Math.max(0L, TimeUnit.MILLISECONDS.toSeconds(millis));
		long hours = totalSeconds / 3600L;
		long minutes = (totalSeconds % 3600L) / 60L;
		long seconds = totalSeconds % 60L;

		if (hours > 0L) {
			return "%dh %02dm %02ds".formatted(hours, minutes, seconds);
		}

		if (minutes > 0L) {
			return "%dm %02ds".formatted(minutes, seconds);
		}

		return seconds + "s";
	}

	public static String dimensionName(String dimensionId) {
		return VANILLA_DIMENSION_NAMES.getOrDefault(dimensionId, dimensionId);
	}

	public static RespawnReport respawnReport(DeathRecord death, long nowMillis) {
		return report(death.x(), death.y(), death.z(), " in %s \u2014 you were dead for %s.".formatted(
				dimensionName(death.dimensionId()),
				formatDuration(nowMillis - death.timeMillis())));
	}

	public static RespawnReport respawnReportWithoutDuration(int x, int y, int z, String dimensionId) {
		return report(x, y, z, " in %s.".formatted(dimensionName(dimensionId)));
	}

	public static String logText(String subject, DeathRecord death) {
		return "%s died at %d, %d, %d in %s".formatted(
				subject, death.x(), death.y(), death.z(), death.dimensionId());
	}

	private static RespawnReport report(int x, int y, int z, String suffix) {
		return new RespawnReport(
				"You died at ",
				"%d, %d, %d".formatted(x, y, z),
				"%d %d %d".formatted(x, y, z),
				suffix);
	}
}
