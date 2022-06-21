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
	private List<ResourceLocation> allowedStructureKeys;
	private ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedStructureKeys;
	private Map<ResourceLocation, ResourceLocation> structureKeysToTypeKeys;
	private ListMultimap<ResourceLocation, ResourceLocation> typeKeysToStructureKeys;

	public SyncPacket() {}

	public SyncPacket(boolean canTeleport, List<ResourceLocation> allowedStructures, ListMultimap<ResourceLocation, ResourceLocation> dimensionsForAllowedStructures, Map<ResourceLocation, ResourceLocation> structureKeysToTypeKeys, ListMultimap<ResourceLocation, ResourceLocation> typeKeysToStructureKeys) {
		this.canTeleport = canTeleport;
		this.allowedStructureKeys = allowedStructures;
		this.dimensionKeysForAllowedStructureKeys = dimensionsForAllowedStructures;
		this.structureKeysToTypeKeys = structureKeysToTypeKeys;
		this.typeKeysToStructureKeys = typeKeysToStructureKeys;
	}

	public SyncPacket(FriendlyByteBuf buf) {
		canTeleport = buf.readBoolean();
		allowedStructureKeys = new ArrayList<ResourceLocation>();
		dimensionKeysForAllowedStructureKeys = ArrayListMultimap.create();
		structureKeysToTypeKeys = new HashMap<ResourceLocation, ResourceLocation>();
		typeKeysToStructureKeys = ArrayListMultimap.create();
		
		int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			ResourceLocation structureKey = buf.readResourceLocation();
			int numDimensions = buf.readInt();
			List<ResourceLocation> dimensions = new ArrayList<ResourceLocation>();
			for (int j = 0; j < numDimensions; j++) {
				dimensions.add(buf.readResourceLocation());
			}
			ResourceLocation typeKey = buf.readResourceLocation();
			if (structureKey != null) {
				allowedStructureKeys.add(structureKey);
				dimensionKeysForAllowedStructureKeys.putAll(structureKey, dimensions);
				structureKeysToTypeKeys.put(structureKey, typeKey);
			}
		}
		
		int numTypes = buf.readInt();
		for (int i = 0; i < numTypes; i++) {
			ResourceLocation typeKey = buf.readResourceLocation();
			int numStructuresToAdd = buf.readInt();
			for (int j = 0; j < numStructuresToAdd; j++) {
				ResourceLocation structureKey = buf.readResourceLocation();
				typeKeysToStructureKeys.put(typeKey, structureKey);
			}
		}
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBoolean(canTeleport);
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

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ExplorersCompass.canTeleport = canTeleport;
			ExplorersCompass.allowedStructureKeys = allowedStructureKeys;
			ExplorersCompass.dimensionKeysForAllowedStructureKeys = dimensionKeysForAllowedStructureKeys;
			ExplorersCompass.structureKeysToTypeKeys = structureKeysToTypeKeys;
			ExplorersCompass.typeKeysToStructureKeys = typeKeysToStructureKeys;
		});
		ctx.get().setPacketHandled(true);
	}

}