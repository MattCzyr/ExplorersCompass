package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncPacket(boolean canTeleport, List<Identifier> allowedStructureIDs, ListMultimap<Identifier, Identifier> allowedStructureIDsToDimensionIDs, Map<Identifier, Identifier> structureIDsToGroupIDs, ListMultimap<Identifier, Identifier> groupIDsToStructureIDs) implements CustomPayload {

	public static final CustomPayload.Id<SyncPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(ExplorersCompass.MODID, "sync"));
	
	public static final PacketCodec<RegistryByteBuf, SyncPacket> PACKET_CODEC = PacketCodec.of(SyncPacket::write, SyncPacket::read);
	
	public static SyncPacket read(RegistryByteBuf buf) {
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
		return new SyncPacket(canTeleport, allowedStructureIDs, allowedStructureIDsToDimensionIDs, structureIDsToGroupIDs, groupIDsToStructureIDs);
	}

	public void write(RegistryByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(allowedStructureIDs.size());
		for (Identifier structureID : allowedStructureIDs) {
			buf.writeIdentifier(structureID);
			List<Identifier> dimensionIDs = allowedStructureIDsToDimensionIDs.get(structureID);
			buf.writeInt(dimensionIDs.size());
			for (Identifier dimensionID : dimensionIDs) {
				buf.writeIdentifier(dimensionID);
			}
			Identifier groupID = structureIDsToGroupIDs.get(structureID);
			buf.writeIdentifier(groupID);
		}
		
		buf.writeInt(groupIDsToStructureIDs.keySet().size());
		for (Identifier groupID : groupIDsToStructureIDs.keySet()) {
			buf.writeIdentifier(groupID);
			List<Identifier> structureIDs = groupIDsToStructureIDs.get(groupID);
			buf.writeInt(structureIDs.size());
			for (Identifier structureID : structureIDs) {
				buf.writeIdentifier(structureID);
			}
		}
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}

	public static void apply(SyncPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			ExplorersCompass.canTeleport = packet.canTeleport;
			ExplorersCompass.allowedStructureIDs = packet.allowedStructureIDs;
			ExplorersCompass.allowedStructureIDsToDimensionIDs = packet.allowedStructureIDsToDimensionIDs;
			ExplorersCompass.structureIDsToGroupIDs = packet.structureIDsToGroupIDs;
			ExplorersCompass.groupIDsToStructureIDs = packet.groupIDsToStructureIDs;
		});
	}

}