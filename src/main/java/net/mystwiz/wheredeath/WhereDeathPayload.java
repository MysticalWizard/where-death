package net.mystwiz.wheredeath;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * An empty payload that is never sent. Its only job is to exist as a registered channel: a client
 * running this mod advertises the channel on join, which lets the server detect the mod is present
 * and leave the respawn message to the client instead of sending a duplicate of its own.
 */
public record WhereDeathPayload() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<WhereDeathPayload> TYPE =
			new CustomPacketPayload.Type<>(WhereDeath.id("installed"));

	public static final StreamCodec<RegistryFriendlyByteBuf, WhereDeathPayload> CODEC =
			StreamCodec.unit(new WhereDeathPayload());

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
