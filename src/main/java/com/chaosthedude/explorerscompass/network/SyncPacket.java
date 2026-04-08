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

	public SyncPacket(boolean canTeleport, int maxNextSearches, boolean infiniteXp, List<Identifier> allowedStructureIDs, Map<Identifier, Integer> xpLevelsForAllowedStructures, ListMultimap<Identifier, Identifier> allowedStructureIDsToDimensionIDs, Map<Identifier, Identifier> structureIDsToGroupIDs) {
		super(Unpooled.buffer());
		writeBoolean(canTeleport);
		writeInt(maxNextSearches);
		writeBoolean(infiniteXp);
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
			int xpLevels = xpLevelsForAllowedStructures.getOrDefault(structureID, 0);
			writeInt(xpLevels);
		}
	}

	public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
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
			int xpLevels = buf.readInt();

			if (structureID != null) {
				allowedStructureIDs.add(structureID);
				allowedStructureIDsToDimensionIDs.putAll(structureID, dimensionIDs);
				structureIDsToGroupIDs.put(structureID, groupID);
				xpLevelsForAllowedStructures.put(structureID, xpLevels);
			}
		}

		client.execute(() -> {
			ExplorersCompass.synced = true;
			ExplorersCompass.canTeleport = canTeleport;
			ExplorersCompass.maxNextSearches = maxNextSearches;
			ExplorersCompass.infiniteXp = infiniteXp;
			ExplorersCompass.allowedStructureIDs = allowedStructureIDs;
			ExplorersCompass.xpLevelsForAllowedStructures = xpLevelsForAllowedStructures;
			ExplorersCompass.allowedStructureIDsToDimensionIDs = allowedStructureIDsToDimensionIDs;
			ExplorersCompass.structureIDsToGroupIDs = structureIDsToGroupIDs;
		});
	}

}
