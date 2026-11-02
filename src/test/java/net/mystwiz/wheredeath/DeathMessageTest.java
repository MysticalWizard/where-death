package net.mystwiz.wheredeath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

class DeathMessageTest {
	private static final DeathReport.RespawnReport REPORT = new DeathReport.RespawnReport(
			"You died at ", "142, 63, -871", "142 63 -871", " in the Overworld.");

	@Test
	void readsAsTheWholeReportBehindASkull() {
		assertEquals(
				"\u2620 You died at 142, 63, -871 in the Overworld.",
				DeathMessage.respawn(REPORT).getString());
	}

	@Test
	void clickingTheCoordinatesPutsTheBareCoordinatesInTheChatBox() {
		assertEquals(
				new ClickEvent.SuggestCommand("142 63 -871"),
				styleOf(DeathMessage.respawn(REPORT), "142, 63, -871").getClickEvent());
	}

	@Test
	void theCoordinatesLookClickable() {
		Style style = styleOf(DeathMessage.respawn(REPORT), "142, 63, -871");

		assertEquals(TextColor.fromLegacyFormat(ChatFormatting.AQUA), style.getColor());
		assertTrue(style.isUnderlined());
	}

	@Test
	void theCoordinatesExplainThemselvesOnHover() {
		assertEquals(
				new HoverEventText("Click to put these coordinates in chat"),
				hoverText(styleOf(DeathMessage.respawn(REPORT), "142, 63, -871")));
	}

	@Test
	void nothingButTheCoordinatesIsClickable() {
		Component message = DeathMessage.respawn(REPORT);

		assertNull(styleOf(message, "You died at ").getClickEvent());
		assertNull(styleOf(message, " in the Overworld.").getClickEvent());
	}

	private static Style styleOf(Component message, String text) {
		return message.getSiblings().stream()
				.filter(sibling -> sibling.getString().equals(text))
				.findFirst()
				.map(Component::getStyle)
				.orElse(Style.EMPTY);
	}

	private static HoverEventText hoverText(Style style) {
		return style.getHoverEvent() instanceof net.minecraft.network.chat.HoverEvent.ShowText showText
				? new HoverEventText(showText.value().getString())
				: null;
	}

	private record HoverEventText(String text) {
	}
}
