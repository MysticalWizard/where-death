package net.mystwiz.wheredeath;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Reports deaths to players from the server, so the feature works for vanilla clients that have
 * nothing installed. The message is sent to that one player only, so nobody else sees it.
 */
public final class ServerDeathTracker {
	/**
	 * Deaths waiting to be reported, keyed by player. Entries deliberately survive a disconnect: a
	 * player who logs out while dead is still dead when they come back, and gets their report then.
	 */
	private static final Map<UUID, DeathRecord> PENDING_DEATHS = new ConcurrentHashMap<>();

	private ServerDeathTracker() {
	}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity instanceof ServerPlayer player) {
				recordDeath(player);
			}
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			// `alive` means the player stepped out of the End portal rather than actually dying.
			if (alive) {
				return;
			}

			report(newPlayer, PENDING_DEATHS.remove(newPlayer.getUUID()));
		});
	}

	private static void recordDeath(ServerPlayer player) {
		BlockPos pos = player.blockPosition();
		DeathRecord death = new DeathRecord(
				pos.getX(), pos.getY(), pos.getZ(),
				player.level().dimension().identifier().toString(),
				System.currentTimeMillis());

		PENDING_DEATHS.put(player.getUUID(), death);
		WhereDeath.LOGGER.info(DeathReport.logText(player.getName().getString(), death));
	}

	private static void report(ServerPlayer player, @Nullable DeathRecord death) {
		// A client running this mod reports the death itself, so staying quiet avoids doubling it up.
		if (ServerPlayNetworking.canSend(player, WhereDeathPayload.TYPE)) {
			return;
		}

		if (death != null) {
			player.sendSystemMessage(DeathMessage.respawn(
					DeathReport.respawnReport(death, System.currentTimeMillis())));
			return;
		}

		// The server restarted while the player was dead, so only vanilla's saved location is left
		// and the duration is gone. Coordinates without a duration still beat saying nothing.
		player.getLastDeathLocation().ifPresent(lastDeath -> {
			BlockPos pos = lastDeath.pos();
			player.sendSystemMessage(DeathMessage.respawn(DeathReport.respawnReportWithoutDuration(
					pos.getX(), pos.getY(), pos.getZ(), lastDeath.dimension().identifier().toString())));
		});
	}
}
