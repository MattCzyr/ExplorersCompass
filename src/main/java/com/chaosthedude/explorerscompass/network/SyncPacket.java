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
	private List<ResourceLocation> allowedConfiguredStructureKeys;
	private ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedConfiguredStructureKeys;
	private Map<ResourceLocation, ResourceLocation> configuredStructureKeysToStructureKeys;
	private ListMultimap<ResourceLocation, ResourceLocation> structureKeysToConfiguredStructureKeys;

	public SyncPacket() {}

	public SyncPacket(boolean canTeleport, List<ResourceLocation> allowedStructures, ListMultimap<ResourceLocation, ResourceLocation> dimensionsForAllowedConfiguredStructures, Map<ResourceLocation, ResourceLocation> configuredStructureKeysToStructureKeys, ListMultimap<ResourceLocation, ResourceLocation> structureKeysToConfiguredStructureKeys) {
		this.canTeleport = canTeleport;
		this.allowedConfiguredStructureKeys = allowedStructures;
		this.dimensionKeysForAllowedConfiguredStructureKeys = dimensionsForAllowedConfiguredStructures;
		this.configuredStructureKeysToStructureKeys = configuredStructureKeysToStructureKeys;
		this.structureKeysToConfiguredStructureKeys = structureKeysToConfiguredStructureKeys;
	}

	public SyncPacket(FriendlyByteBuf buf) {
		canTeleport = buf.readBoolean();
		allowedConfiguredStructureKeys = new ArrayList<ResourceLocation>();
		dimensionKeysForAllowedConfiguredStructureKeys = ArrayListMultimap.create();
		configuredStructureKeysToStructureKeys = new HashMap<ResourceLocation, ResourceLocation>();
		structureKeysToConfiguredStructureKeys = ArrayListMultimap.create();
		
		int numConfiguredStructures = buf.readInt();
		for (int i = 0; i < numConfiguredStructures; i++) {
			ResourceLocation configuredStructureKey = buf.readResourceLocation();
			int numDimensions = buf.readInt();
			List<ResourceLocation> dimensions = new ArrayList<ResourceLocation>();
			for (int j = 0; j < numDimensions; j++) {
				dimensions.add(buf.readResourceLocation());
			}
			ResourceLocation structureKey = buf.readResourceLocation();
			if (configuredStructureKey != null) {
				allowedConfiguredStructureKeys.add(configuredStructureKey);
				dimensionKeysForAllowedConfiguredStructureKeys.putAll(configuredStructureKey, dimensions);
				configuredStructureKeysToStructureKeys.put(configuredStructureKey, structureKey);
			}
		}
		
		int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			ResourceLocation structureKey = buf.readResourceLocation();
			int numStructuresToAdd = buf.readInt();
			for (int j = 0; j < numStructuresToAdd; j++) {
				ResourceLocation configuredStructureKey = buf.readResourceLocation();
				structureKeysToConfiguredStructureKeys.put(structureKey, configuredStructureKey);
			}
		}
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(allowedConfiguredStructureKeys.size());
		for (ResourceLocation configuredStructureKey : allowedConfiguredStructureKeys) {
			buf.writeResourceLocation(configuredStructureKey);
			List<ResourceLocation> dimensions = dimensionKeysForAllowedConfiguredStructureKeys.get(configuredStructureKey);
			buf.writeInt(dimensions.size());
			for (ResourceLocation dimensionKey : dimensions) {
				buf.writeResourceLocation(dimensionKey);
			}
			ResourceLocation structureKey = configuredStructureKeysToStructureKeys.get(configuredStructureKey);
			buf.writeResourceLocation(structureKey);
		}
		
		buf.writeInt(structureKeysToConfiguredStructureKeys.keySet().size());
		for (ResourceLocation structureKey : structureKeysToConfiguredStructureKeys.keySet()) {
			buf.writeResourceLocation(structureKey);
			List<ResourceLocation> configuredStructureKeys = structureKeysToConfiguredStructureKeys.get(structureKey);
			buf.writeInt(configuredStructureKeys.size());
			for (ResourceLocation configuredStructureKey : configuredStructureKeys) {
				buf.writeResourceLocation(configuredStructureKey);
			}
		}
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ExplorersCompass.canTeleport = canTeleport;
			ExplorersCompass.allowedConfiguredStructureKeys = allowedConfiguredStructureKeys;
			ExplorersCompass.dimensionKeysForAllowedConfiguredStructureKeys = dimensionKeysForAllowedConfiguredStructureKeys;
			ExplorersCompass.configuredStructureKeysToStructureKeys = configuredStructureKeysToStructureKeys;
			ExplorersCompass.structureKeysToConfiguredStructureKeys = structureKeysToConfiguredStructureKeys;
		});
		ctx.get().setPacketHandled(true);
	}

}