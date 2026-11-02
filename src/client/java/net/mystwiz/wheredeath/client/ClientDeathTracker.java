package net.mystwiz.wheredeath.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.mystwiz.wheredeath.DeathMessage;
import net.mystwiz.wheredeath.DeathRecord;
import net.mystwiz.wheredeath.DeathReport;
import net.mystwiz.wheredeath.DeathWatcher;
import net.mystwiz.wheredeath.WhereDeath;
import net.mystwiz.wheredeath.WhereDeathPayload;

/**
 * Reports deaths from the client, which is what makes the mod useful on servers you cannot install
 * anything on. The message is added straight to the local chat and never leaves this machine.
 */
public final class ClientDeathTracker {
	private static final DeathWatcher WATCHER = new DeathWatcher();

	private ClientDeathTracker() {
	}

	public static void register() {
		// Never actually receives anything. Registering it advertises the channel to the server,
		// which is how a server running this mod knows to leave the message to us.
		ClientPlayNetworking.registerGlobalReceiver(WhereDeathPayload.TYPE, (payload, context) -> {
		});

		ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> WATCHER.reset());
		ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> WATCHER.reset());
		ClientTickEvents.END_CLIENT_TICK.register(ClientDeathTracker::tick);
	}

	private static void tick(Minecraft client) {
		LocalPlayer player = client.player;

		// The player is momentarily absent while a world loads. Leaving the watcher untouched keeps
		// a pending death intact; a real departure is handled by the disconnect event above.
		if (player == null) {
			return;
		}

		BlockPos pos = player.blockPosition();
		DeathWatcher.Event event = WATCHER.observe(
				player.isAlive(),
				pos.getX(), pos.getY(), pos.getZ(),
				player.level().dimension().identifier().toString(),
				System.currentTimeMillis());

		switch (event) {
			case DeathWatcher.Died(DeathRecord death) ->
					WhereDeath.LOGGER.info(DeathReport.logText("You", death));
			case DeathWatcher.Respawned(DeathRecord death, long nowMillis) ->
					player.sendSystemMessage(
							DeathMessage.respawn(DeathReport.respawnReport(death, nowMillis)));
			case null -> {
			}
		}
	}
}
