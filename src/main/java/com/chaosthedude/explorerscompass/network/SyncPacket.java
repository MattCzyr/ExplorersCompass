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

public record SyncPacket(boolean canTeleport, int maxNextSearches, boolean infiniteXp, List<Identifier> allowedStructureIDs, Map<Identifier, Integer> xpLevelsForAllowedStructures, ListMultimap<Identifier, Identifier> allowedStructureIDsToDimensionIDs, Map<Identifier, Identifier> structureIDsToGroupIDs) implements CustomPayload {

	public static final CustomPayload.Id<SyncPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(ExplorersCompass.MODID, "sync"));

	public static final PacketCodec<RegistryByteBuf, SyncPacket> PACKET_CODEC = PacketCodec.of(SyncPacket::write, SyncPacket::read);

	public static SyncPacket read(RegistryByteBuf buf) {
		final boolean canTeleport = buf.readBoolean();
		final int maxNextSearches = buf.readInt();
		final boolean infiniteXp = buf.readBoolean();
		final List<Identifier> allowedStructureIDs = new ArrayList<Identifier>();
		final Map<Identifier, Integer> xpLevelsForAllowedStructures = new HashMap<Identifier, Integer>();
		final ListMultimap<Identifier, Identifier> allowedStructureIDsToDimensionIDs = ArrayListMultimap.create();
		final Map<Identifier, Identifier> structureIDsToGroupIDs = new HashMap<Identifier, Identifier>();

		final int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			Identifier structureID = buf.readIdentifier();
			int numDimensions = buf.readInt();
			List<Identifier> dimensionIDs = new ArrayList<Identifier>();
			for (int j = 0; j < numDimensions; j++) {
				dimensionIDs.add(buf.readIdentifier());
			}
			Identifier groupID = buf.readIdentifier();
			int xpLevel = buf.readInt();

			if (structureID != null) {
				allowedStructureIDs.add(structureID);
				allowedStructureIDsToDimensionIDs.putAll(structureID, dimensionIDs);
				structureIDsToGroupIDs.put(structureID, groupID);
				xpLevelsForAllowedStructures.put(structureID, xpLevel);
			}
		}

		return new SyncPacket(canTeleport, maxNextSearches, infiniteXp, allowedStructureIDs, xpLevelsForAllowedStructures, allowedStructureIDsToDimensionIDs, structureIDsToGroupIDs);
	}

	public void write(RegistryByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(maxNextSearches);
		buf.writeBoolean(infiniteXp);
		buf.writeInt(allowedStructureIDs.size());
		for (Identifier structureID : allowedStructureIDs) {
			buf.writeIdentifier(structureID);
			List<Identifier> dimensionIDs = allowedStructureIDsToDimensionIDs.get(structureID);
			buf.writeInt(dimensionIDs.size());
			for (Identifier dimensionID : dimensionIDs) {
				buf.writeIdentifier(dimensionID);
			}
			buf.writeIdentifier(structureIDsToGroupIDs.get(structureID));
			buf.writeInt(xpLevelsForAllowedStructures.getOrDefault(structureID, 0));
		}
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}

	public static void apply(SyncPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			ExplorersCompass.synced = true;
			ExplorersCompass.canTeleport = packet.canTeleport;
			ExplorersCompass.maxNextSearches = packet.maxNextSearches;
			ExplorersCompass.infiniteXp = packet.infiniteXp;
			ExplorersCompass.allowedStructureIDs = packet.allowedStructureIDs;
			ExplorersCompass.xpLevelsForAllowedStructures = packet.xpLevelsForAllowedStructures;
			ExplorersCompass.allowedStructureIDsToDimensionIDs = packet.allowedStructureIDsToDimensionIDs;
			ExplorersCompass.structureIDsToGroupIDs = packet.structureIDsToGroupIDs;
		});
	}

}
