package net.mystwiz.wheredeath;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhereDeath implements ModInitializer {
	public static final String MOD_ID = "where-death";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Registered on both sides: the client needs the type to advertise the channel, the server
		// needs it to test whether a joining client has the mod.
		PayloadTypeRegistry.clientboundPlay().register(WhereDeathPayload.TYPE, WhereDeathPayload.CODEC);

		ServerDeathTracker.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
