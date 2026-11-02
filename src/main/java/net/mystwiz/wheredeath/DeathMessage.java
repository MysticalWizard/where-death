package net.mystwiz.wheredeath;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

/** Turns a report into the styled chat line this mod shows on both sides. */
public final class DeathMessage {
	private static final Component COORDINATE_TOOLTIP =
			Component.literal("Click to put these coordinates in chat");

	private DeathMessage() {
	}

	public static Component respawn(DeathReport.RespawnReport report) {
		return Component.literal("\u2620 ")
				.withStyle(ChatFormatting.RED)
				.append(Component.literal(report.prefix()).withStyle(ChatFormatting.GRAY))
				.append(Component.literal(report.coordinates()).withStyle(style -> style
						.withColor(ChatFormatting.AQUA)
						.withUnderlined(true)
						// Fills the chat box without sending it, so it can be edited or shared as-is.
						.withClickEvent(new ClickEvent.SuggestCommand(report.suggestion()))
						.withHoverEvent(new HoverEvent.ShowText(COORDINATE_TOOLTIP))))
				.append(Component.literal(report.suffix()).withStyle(ChatFormatting.GRAY));
	}
}
