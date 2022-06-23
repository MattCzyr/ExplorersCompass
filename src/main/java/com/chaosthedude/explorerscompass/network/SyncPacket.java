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

	public SyncPacket(boolean canTeleport, List<Identifier> allowedStructureIDs, ListMultimap<Identifier, Identifier> allowedStructureIDsToDimensionIDs, Map<Identifier, Identifier> structureIDsToGroupIDs, ListMultimap<Identifier, Identifier> groupIDsToStructureIDs) {
		super(Unpooled.buffer());
		writeBoolean(canTeleport);
		writeInt(allowedStructureIDs.size());
		for (Identifier structureID : allowedStructureIDs) {
			writeIdentifier(structureID);
			List<Identifier> dimensionIDs = allowedStructureIDsToDimensionIDs.get(structureID);
			writeInt(dimensionIDs.size());
			for (Identifier dimensionID : dimensionIDs) {
				writeIdentifier(dimensionID);
			}
			Identifier groupID = structureIDsToGroupIDs.get(structureID);
			writeIdentifier(groupID);
		}
		
		writeInt(groupIDsToStructureIDs.keySet().size());
		for (Identifier groupID : groupIDsToStructureIDs.keySet()) {
			writeIdentifier(groupID);
			List<Identifier> structureIDs = groupIDsToStructureIDs.get(groupID);
			writeInt(structureIDs.size());
			for (Identifier structureID : structureIDs) {
				writeIdentifier(structureID);
			}
		}
	}

	public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final boolean canTeleport = buf.readBoolean();
		final List<Identifier> allowedStructureIDs = new ArrayList<Identifier>();
		final ListMultimap<Identifier, Identifier> allowedStructureIDsToDimensionIDs = ArrayListMultimap.create();
		Map<Identifier, Identifier> structureIDsToGroupIDs = new HashMap<Identifier, Identifier>();
		ListMultimap<Identifier, Identifier> groupIDsToStructureIDs = ArrayListMultimap.create();
		
		final int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			Identifier structureID = buf.readIdentifier();
			int numDimensions = buf.readInt();
			List<Identifier> dimensionIDs = new ArrayList<Identifier>();
			for (int j = 0; j < numDimensions; j++) {
				dimensionIDs.add(buf.readIdentifier());
			}
			Identifier groupID = buf.readIdentifier();
			
			if (structureID != null) {
				allowedStructureIDs.add(structureID);
				allowedStructureIDsToDimensionIDs.putAll(structureID, dimensionIDs);
				structureIDsToGroupIDs.put(structureID, groupID);
			}
		}
		
		int numGroups = buf.readInt();
		for (int i = 0; i < numGroups; i++) {
			Identifier groupID = buf.readIdentifier();
			int numGroupsToAdd = buf.readInt();
			for (int j = 0; j < numGroupsToAdd; j++) {
				Identifier structureID = buf.readIdentifier();
				groupIDsToStructureIDs.put(groupID, structureID);
			}
		}
		
		client.execute(() -> {
			ExplorersCompass.canTeleport = canTeleport;
			ExplorersCompass.allowedStructureIDs = allowedStructureIDs;
			ExplorersCompass.allowedStructureIDsToDimensionIDs = allowedStructureIDsToDimensionIDs;
			ExplorersCompass.structureIDsToGroupIDs = structureIDsToGroupIDs;
			ExplorersCompass.groupIDsToStructureIDs = groupIDsToStructureIDs;
		});
	}

}