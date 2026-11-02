package net.mystwiz.wheredeath;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class DeathReportTest {
	private static final DeathRecord OVERWORLD_DEATH =
			new DeathRecord(142, 63, -871, "minecraft:overworld", 1_000L);

	@Test
	void formatsSecondsOnlyForUnderAMinute() {
		assertEquals("45s", DeathReport.formatDuration(TimeUnit.SECONDS.toMillis(45)));
	}

	@Test
	void roundsDownToWholeSeconds() {
		assertEquals("3s", DeathReport.formatDuration(3999));
	}

	@Test
	void formatsMinutesAndPaddedSecondsForUnderAnHour() {
		assertEquals("1m 05s", DeathReport.formatDuration(TimeUnit.SECONDS.toMillis(65)));
	}

	@Test
	void formatsHoursMinutesAndSeconds() {
		long millis = TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(4) + TimeUnit.SECONDS.toMillis(9);
		assertEquals("1h 04m 09s", DeathReport.formatDuration(millis));
	}

	@Test
	void treatsNegativeDurationsAsZero() {
		assertEquals("0s", DeathReport.formatDuration(-5000));
	}

	@Test
	void namesTheThreeVanillaDimensions() {
		assertEquals("the Overworld", DeathReport.dimensionName("minecraft:overworld"));
		assertEquals("the Nether", DeathReport.dimensionName("minecraft:the_nether"));
		assertEquals("the End", DeathReport.dimensionName("minecraft:the_end"));
	}

	@Test
	void fallsBackToTheRawIdForModdedDimensions() {
		assertEquals("aether:the_aether", DeathReport.dimensionName("aether:the_aether"));
	}

	@Test
	void respawnReportSplitsOutTheClickableCoordinates() {
		DeathReport.RespawnReport report = DeathReport.respawnReport(OVERWORLD_DEATH, 73_000L);

		assertEquals("You died at ", report.prefix());
		assertEquals("142, 63, -871", report.coordinates());
		assertEquals(" in the Overworld \u2014 you were dead for 1m 12s.", report.suffix());
	}

	@Test
	void respawnReportSuggestsBareCoordinatesForTheChatBox() {
		assertEquals("142 63 -871", DeathReport.respawnReport(OVERWORLD_DEATH, 73_000L).suggestion());
	}

	@Test
	void respawnReportPartsReadAsOneSentence() {
		assertEquals(
				"You died at 142, 63, -871 in the Overworld \u2014 you were dead for 1m 12s.",
				DeathReport.respawnReport(OVERWORLD_DEATH, 73_000L).plainText());
	}

	@Test
	void respawnReportOmitsDurationWhenTheDeathTimeIsUnknown() {
		DeathReport.RespawnReport report =
				DeathReport.respawnReportWithoutDuration(8, 70, -14, "minecraft:the_nether");

		assertEquals("8, 70, -14", report.coordinates());
		assertEquals("8 70 -14", report.suggestion());
		assertEquals("You died at 8, 70, -14 in the Nether.", report.plainText());
	}

	@Test
	void logTextUsesRawDimensionIdAndNamesTheSubject() {
		assertEquals(
				"mystwiz died at 142, 63, -871 in minecraft:overworld",
				DeathReport.logText("mystwiz", OVERWORLD_DEATH));
	}
}
