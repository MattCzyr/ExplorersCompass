package com.chaosthedude.explorerscompass;

import com.chaosthedude.explorerscompass.network.SyncPacket;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ExplorersCompassClient implements ClientModInitializer {
	
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncPacket.PACKET_ID, SyncPacket::apply);
	}

}
