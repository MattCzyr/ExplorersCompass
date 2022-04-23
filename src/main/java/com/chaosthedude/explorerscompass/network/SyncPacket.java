package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SyncPacket extends PacketByteBuf {

	public static final Identifier ID = new Identifier(ExplorersCompass.MODID, "sync");

	public SyncPacket(boolean canTeleport, List<Identifier> allowedConfiguredStructureIDs, ListMultimap<Identifier, Identifier> allowedConfiguredStructureIDsToDimensionIDs, Map<Identifier, Identifier> configuredStructureIDsToStructureIDs, ListMultimap<Identifier, Identifier> structureIDsToConfiguredStructureIDs) {
		super(Unpooled.buffer());
		writeBoolean(canTeleport);
		writeInt(allowedConfiguredStructureIDs.size());
		for (Identifier configuredStructureID : allowedConfiguredStructureIDs) {
			writeIdentifier(configuredStructureID);
			List<Identifier> dimensionIDs = allowedConfiguredStructureIDsToDimensionIDs.get(configuredStructureID);
			writeInt(dimensionIDs.size());
			for (Identifier dimensionID : dimensionIDs) {
				writeIdentifier(dimensionID);
			}
			Identifier structureID = configuredStructureIDsToStructureIDs.get(configuredStructureID);
			writeIdentifier(structureID);
		}
		
		writeInt(structureIDsToConfiguredStructureIDs.keySet().size());
		for (Identifier structureID : structureIDsToConfiguredStructureIDs.keySet()) {
			writeIdentifier(structureID);
			List<Identifier> configuredStructureIDs = structureIDsToConfiguredStructureIDs.get(structureID);
			writeInt(configuredStructureIDs.size());
			for (Identifier configuredStructureID : configuredStructureIDs) {
				writeIdentifier(configuredStructureID);
			}
		}
	}

	public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final boolean canTeleport = buf.readBoolean();
		final List<Identifier> allowedConfiguredStructureIDs = new ArrayList<Identifier>();
		final ListMultimap<Identifier, Identifier> allowedConfiguredStructureIDsToDimensionIDs = ArrayListMultimap.create();
		Map<Identifier, Identifier> configuredStructureIDsToStructureIDs = new HashMap<Identifier, Identifier>();
		ListMultimap<Identifier, Identifier> structureIDsToConfiguredStructureIDs = ArrayListMultimap.create();
		
		final int numConfiguredStructures = buf.readInt();
		for (int i = 0; i < numConfiguredStructures; i++) {
			Identifier configuredStructureID = buf.readIdentifier();
			int numDimensions = buf.readInt();
			List<Identifier> dimensionIDs = new ArrayList<Identifier>();
			for (int j = 0; j < numDimensions; j++) {
				dimensionIDs.add(buf.readIdentifier());
			}
			Identifier structureID = buf.readIdentifier();
			
			if (configuredStructureID != null) {
				allowedConfiguredStructureIDs.add(configuredStructureID);
				allowedConfiguredStructureIDsToDimensionIDs.putAll(configuredStructureID, dimensionIDs);
				configuredStructureIDsToStructureIDs.put(configuredStructureID, structureID);
			}
		}
		
		int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			Identifier structureID = buf.readIdentifier();
			int numStructuresToAdd = buf.readInt();
			for (int j = 0; j < numStructuresToAdd; j++) {
				Identifier configuredStructureID = buf.readIdentifier();
				structureIDsToConfiguredStructureIDs.put(structureID, configuredStructureID);
			}
		}
		
		client.execute(() -> {
			ExplorersCompass.canTeleport = canTeleport;
			ExplorersCompass.allowedConfiguredStructureIDs = allowedConfiguredStructureIDs;
			ExplorersCompass.allowedConfiguredStructureIDsToDimensionIDs = allowedConfiguredStructureIDsToDimensionIDs;
			ExplorersCompass.configuredStructureIDsToStructureIDs = configuredStructureIDsToStructureIDs;
			ExplorersCompass.structureIDsToConfiguredStructureIDs = structureIDsToConfiguredStructureIDs;
		});
	}

}