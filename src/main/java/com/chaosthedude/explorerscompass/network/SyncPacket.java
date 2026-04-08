package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

public class SyncPacket {

	private boolean canTeleport;
	private int maxNextSearches;
	private boolean infiniteXp;
	private List<ResourceLocation> allowedStructureKeys;
	private Map<ResourceLocation, Integer> xpLevelsForAllowedStructureKeys;
	private ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedStructureKeys;
	private Map<ResourceLocation, ResourceLocation> structureKeysToTypeKeys;

	public SyncPacket() {}

	public SyncPacket(boolean canTeleport, int maxNextSearches, boolean infiniteXp, List<ResourceLocation> allowedStructures, Map<ResourceLocation, Integer> xpLevelsForAllowedStructures, ListMultimap<ResourceLocation, ResourceLocation> dimensionsForAllowedStructures, Map<ResourceLocation, ResourceLocation> structureKeysToTypeKeys) {
		this.canTeleport = canTeleport;
		this.maxNextSearches = maxNextSearches;
		this.infiniteXp = infiniteXp;
		this.allowedStructureKeys = allowedStructures;
		this.xpLevelsForAllowedStructureKeys = xpLevelsForAllowedStructures;
		this.dimensionKeysForAllowedStructureKeys = dimensionsForAllowedStructures;
		this.structureKeysToTypeKeys = structureKeysToTypeKeys;
	}

	public SyncPacket(FriendlyByteBuf buf) {
		canTeleport = buf.readBoolean();
		maxNextSearches = buf.readInt();
		infiniteXp = buf.readBoolean();
		allowedStructureKeys = new ArrayList<ResourceLocation>();
		xpLevelsForAllowedStructureKeys = new HashMap<ResourceLocation, Integer>();
		dimensionKeysForAllowedStructureKeys = ArrayListMultimap.create();
		structureKeysToTypeKeys = new HashMap<ResourceLocation, ResourceLocation>();

		int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			ResourceLocation structureKey = buf.readResourceLocation();
			int numDimensions = buf.readInt();
			List<ResourceLocation> dimensions = new ArrayList<ResourceLocation>();
			for (int j = 0; j < numDimensions; j++) {
				dimensions.add(buf.readResourceLocation());
			}
			ResourceLocation typeKey = buf.readResourceLocation();
			int xpLevels = buf.readInt();
			if (structureKey != null) {
				allowedStructureKeys.add(structureKey);
				xpLevelsForAllowedStructureKeys.put(structureKey, xpLevels);
				dimensionKeysForAllowedStructureKeys.putAll(structureKey, dimensions);
				structureKeysToTypeKeys.put(structureKey, typeKey);
			}
		}
	}

	public void toBytes(FriendlyByteBuf buf) {
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
			int xpLevels = xpLevelsForAllowedStructureKeys.getOrDefault(structureKey, 0);
			buf.writeInt(xpLevels);
		}
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ExplorersCompass.synced = true;
			ExplorersCompass.canTeleport = canTeleport;
			ExplorersCompass.maxNextSearches = maxNextSearches;
			ExplorersCompass.infiniteXp = infiniteXp;
			ExplorersCompass.allowedStructureKeys = allowedStructureKeys;
			ExplorersCompass.xpLevelsForAllowedStructureKeys = xpLevelsForAllowedStructureKeys;
			ExplorersCompass.dimensionKeysForAllowedStructureKeys = dimensionKeysForAllowedStructureKeys;
			ExplorersCompass.structureKeysToTypeKeys = structureKeysToTypeKeys;
		});
		ctx.get().setPacketHandled(true);
	}

}
