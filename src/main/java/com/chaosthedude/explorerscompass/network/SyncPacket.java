package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPacket(boolean canTeleport, boolean infiniteXp, List<Identifier> allowedStructures, Map<Identifier, Integer> xpLevelsForAllowedStructures, ListMultimap<Identifier, Identifier> dimensionsForAllowedStructures, Map<Identifier, Identifier> structureIdsToGroupIds, ListMultimap<Identifier, Identifier> groupIdsToStructureIds) implements CustomPacketPayload {

	public static final Type<SyncPacket> TYPE = new Type<SyncPacket>(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "sync"));

	public static final StreamCodec<FriendlyByteBuf, SyncPacket> CODEC = StreamCodec.ofMember(SyncPacket::write, SyncPacket::read);

	public static SyncPacket read(FriendlyByteBuf buf) {
		final boolean canTeleport = buf.readBoolean();
		final boolean infiniteXp = buf.readBoolean();
		final List<Identifier> allowedStructureIds = new ArrayList<Identifier>();
		final Map<Identifier, Integer> xpLevelsForAllowedStructures = new HashMap<Identifier, Integer>();
		final ListMultimap<Identifier, Identifier> dimensionsForAllowedStructures = ArrayListMultimap.create();
		final Map<Identifier, Identifier> structureIdsToGroupIds = new HashMap<Identifier, Identifier>();
		final ListMultimap<Identifier, Identifier> groupIdsToStructureIds = ArrayListMultimap.create();

		final int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			final Identifier structureId = buf.readIdentifier();
			final int numDimensions = buf.readInt();
			final List<Identifier> dimensions = new ArrayList<Identifier>();
			for (int j = 0; j < numDimensions; j++) {
				dimensions.add(buf.readIdentifier());
			}
			final Identifier typeId = buf.readIdentifier();
			final int xpLevels = buf.readInt();
			if (structureId != null) {
				allowedStructureIds.add(structureId);
				dimensionsForAllowedStructures.putAll(structureId, dimensions);
				structureIdsToGroupIds.put(structureId, typeId);
				xpLevelsForAllowedStructures.put(structureId, xpLevels);
			}
		}

		final int numTypes = buf.readInt();
		for (int i = 0; i < numTypes; i++) {
			final Identifier typeKey = buf.readIdentifier();
			final int numStructuresToAdd = buf.readInt();
			for (int j = 0; j < numStructuresToAdd; j++) {
				final Identifier structureKey = buf.readIdentifier();
				groupIdsToStructureIds.put(typeKey, structureKey);
			}
		}

		return new SyncPacket(canTeleport, infiniteXp, allowedStructureIds, xpLevelsForAllowedStructures, dimensionsForAllowedStructures, structureIdsToGroupIds, groupIdsToStructureIds);
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeBoolean(infiniteXp);
		buf.writeInt(allowedStructures.size());
		for (Identifier structureKey : allowedStructures) {
			buf.writeIdentifier(structureKey);
			List<Identifier> dimensions = dimensionsForAllowedStructures.get(structureKey);
			buf.writeInt(dimensions.size());
			for (Identifier dimensionKey : dimensions) {
				buf.writeIdentifier(dimensionKey);
			}
			Identifier typeKey = structureIdsToGroupIds.get(structureKey);
			buf.writeIdentifier(typeKey);
			int xpLevels = xpLevelsForAllowedStructures.get(structureKey);
			buf.writeInt(xpLevels);
		}

		buf.writeInt(groupIdsToStructureIds.keySet().size());
		for (Identifier typeKey : groupIdsToStructureIds.keySet()) {
			buf.writeIdentifier(typeKey);
			List<Identifier> structureKeys = groupIdsToStructureIds.get(typeKey);
			buf.writeInt(structureKeys.size());
			for (Identifier structureKey : structureKeys) {
				buf.writeIdentifier(structureKey);
			}
		}
	}

	public static void handle(SyncPacket packet, IPayloadContext  context) {
		if (context.flow().isClientbound()) {
			context.enqueueWork(() -> {
				ExplorersCompass.canTeleport = packet.canTeleport;
				ExplorersCompass.infiniteXp = packet.infiniteXp;
				ExplorersCompass.allowedStructures = packet.allowedStructures;
				ExplorersCompass.xpLevelsForAllowedStructures = packet.xpLevelsForAllowedStructures;
				ExplorersCompass.dimensionsForAllowedStructures = packet.dimensionsForAllowedStructures;
				ExplorersCompass.structureIdsToGroupIds = packet.structureIdsToGroupIds;
				ExplorersCompass.groupIdsToStructureIds = packet.groupIdsToStructureIds;
			});
		}
	}

	@Override
	public Type<SyncPacket> type() {
		return TYPE;
	}

}
