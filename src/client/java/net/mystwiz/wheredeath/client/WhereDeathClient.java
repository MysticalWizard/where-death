package net.mystwiz.wheredeath.client;

import net.fabricmc.api.ClientModInitializer;

public class WhereDeathClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientDeathTracker.register();
	}
}
