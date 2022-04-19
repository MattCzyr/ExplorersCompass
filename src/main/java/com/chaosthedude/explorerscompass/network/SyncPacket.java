package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.List;
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

	public SyncPacket() {}

	public SyncPacket(boolean canTeleport, List<ResourceLocation> allowedStructures, ListMultimap<ResourceLocation, ResourceLocation> dimensionsForAllowedConfiguredStructures) {
		this.canTeleport = canTeleport;
		this.allowedConfiguredStructureKeys = allowedStructures;
		this.dimensionKeysForAllowedConfiguredStructureKeys = dimensionsForAllowedConfiguredStructures;
	}

	public SyncPacket(FriendlyByteBuf buf) {
		canTeleport = buf.readBoolean();
		allowedConfiguredStructureKeys = new ArrayList<ResourceLocation>();
		dimensionKeysForAllowedConfiguredStructureKeys = ArrayListMultimap.create();
		int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			ResourceLocation structureKey = buf.readResourceLocation();
			int numDimensions = buf.readInt();
			List<ResourceLocation> dimensions = new ArrayList<ResourceLocation>();
			for (int j = 0; j < numDimensions; j++) {
				dimensions.add(buf.readResourceLocation());
			}
			if (structureKey != null) {
				allowedConfiguredStructureKeys.add(structureKey);
				dimensionKeysForAllowedConfiguredStructureKeys.putAll(structureKey, dimensions);
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
		}
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ExplorersCompass.canTeleport = canTeleport;
			ExplorersCompass.allowedConfiguredStructureKeys = allowedConfiguredStructureKeys;
			ExplorersCompass.dimensionKeysForAllowedConfiguredStructureKeys = dimensionKeysForAllowedConfiguredStructureKeys;
		});
		ctx.get().setPacketHandled(true);
	}

}