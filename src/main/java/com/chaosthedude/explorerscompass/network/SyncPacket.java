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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPacket(boolean canTeleport, int maxNextSearches, boolean infiniteXp, List<ResourceLocation> allowedStructureKeys, Map<ResourceLocation, Integer> xpLevelsForAllowedStructureKeys, ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedStructureKeys, Map<ResourceLocation, ResourceLocation> structureKeysToTypeKeys, ListMultimap<ResourceLocation, ResourceLocation> typeKeysToStructureKeys) implements CustomPacketPayload {

	public static final Type<SyncPacket> TYPE = new Type<SyncPacket>(ResourceLocation.fromNamespaceAndPath(ExplorersCompass.MODID, "sync"));
	
	public static final StreamCodec<FriendlyByteBuf, SyncPacket> CODEC = StreamCodec.ofMember(SyncPacket::write, SyncPacket::read);
	
	public static SyncPacket read(FriendlyByteBuf buf) {
		final boolean canTeleport = buf.readBoolean();
		final int maxNextSearches = buf.readInt();
		final boolean infiniteXp = buf.readBoolean();
		final List<ResourceLocation> allowedStructureKeys = new ArrayList<ResourceLocation>();
		final Map<ResourceLocation, Integer> xpLevelsForAllowedStructureKeys = new HashMap<ResourceLocation, Integer>();
		final ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedStructureKeys = ArrayListMultimap.create();
		final Map<ResourceLocation, ResourceLocation> structureKeysToTypeKeys = new HashMap<ResourceLocation, ResourceLocation>();
		final ListMultimap<ResourceLocation, ResourceLocation> typeKeysToStructureKeys = ArrayListMultimap.create();

		final int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			final ResourceLocation structureKey = buf.readResourceLocation();
			final int numDimensions = buf.readInt();
			final List<ResourceLocation> dimensions = new ArrayList<ResourceLocation>();
			for (int j = 0; j < numDimensions; j++) {
				dimensions.add(buf.readResourceLocation());
			}
			final ResourceLocation typeKey = buf.readResourceLocation();
			final int xpLevels = buf.readInt();
			if (structureKey != null) {
				allowedStructureKeys.add(structureKey);
				dimensionKeysForAllowedStructureKeys.putAll(structureKey, dimensions);
				structureKeysToTypeKeys.put(structureKey, typeKey);
				xpLevelsForAllowedStructureKeys.put(structureKey, xpLevels);
			}
		}

		final int numTypes = buf.readInt();
		for (int i = 0; i < numTypes; i++) {
			final ResourceLocation typeKey = buf.readResourceLocation();
			final int numStructuresToAdd = buf.readInt();
			for (int j = 0; j < numStructuresToAdd; j++) {
				final ResourceLocation structureKey = buf.readResourceLocation();
				typeKeysToStructureKeys.put(typeKey, structureKey);
			}
		}

		return new SyncPacket(canTeleport, maxNextSearches, infiniteXp, allowedStructureKeys, xpLevelsForAllowedStructureKeys, dimensionKeysForAllowedStructureKeys, structureKeysToTypeKeys, typeKeysToStructureKeys);
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(maxNextSearches);
		buf.writeBoolean(infiniteXp);
		buf.writeInt(allowedStructureKeys.size());
		for (ResourceLocation structureKey : allowedStructureKeys) {
			buf.writeResourceLocation(structureKey);
			List<ResourceLocation> dimensions = dimensionKeysForAllowedStructureKeys.get(structureKey);
			buf.writeInt(dimensions.size());
			for (ResourceLocation dimensionKey : dimensions) {
				buf.writeResourceLocation(dimensionKey);
			}
			ResourceLocation typeKey = structureKeysToTypeKeys.get(structureKey);
			buf.writeResourceLocation(typeKey);
			int xpLevels = xpLevelsForAllowedStructureKeys.get(structureKey);
			buf.writeInt(xpLevels);
		}

		buf.writeInt(typeKeysToStructureKeys.keySet().size());
		for (ResourceLocation typeKey : typeKeysToStructureKeys.keySet()) {
			buf.writeResourceLocation(typeKey);
			List<ResourceLocation> structureKeys = typeKeysToStructureKeys.get(typeKey);
			buf.writeInt(structureKeys.size());
			for (ResourceLocation structureKey : structureKeys) {
				buf.writeResourceLocation(structureKey);
			}
		}
	}

	public static void handle(SyncPacket packet, IPayloadContext  context) {
		if (context.flow().isClientbound()) {
			context.enqueueWork(() -> {
				ExplorersCompass.synced = true;
				ExplorersCompass.canTeleport = packet.canTeleport;
				ExplorersCompass.maxNextSearches = packet.maxNextSearches;
				ExplorersCompass.infiniteXp = packet.infiniteXp;
				ExplorersCompass.allowedStructureKeys = packet.allowedStructureKeys;
				ExplorersCompass.xpLevelsForAllowedStructureKeys = packet.xpLevelsForAllowedStructureKeys;
				ExplorersCompass.dimensionKeysForAllowedStructureKeys = packet.dimensionKeysForAllowedStructureKeys;
				ExplorersCompass.structureKeysToTypeKeys = packet.structureKeysToTypeKeys;
				ExplorersCompass.typeKeysToStructureKeys = packet.typeKeysToStructureKeys;
			});
		}
	}
	
	@Override
	public Type<SyncPacket> type() {
		return TYPE;
	}

}