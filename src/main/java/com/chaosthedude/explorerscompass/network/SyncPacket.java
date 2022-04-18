package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.network.NetworkEvent;

public class SyncPacket {

	private boolean canTeleport;
	private List<StructureFeature<?>> allowedStructures;
	private Map<StructureFeature<?>, List<ResourceLocation>> dimensionsForAllowedStructures;

	public SyncPacket() {}

	public SyncPacket(boolean canTeleport, List<StructureFeature<?>> allowedStructures, Map<StructureFeature<?>, List<ResourceLocation>> dimensionsForAllowedStructures) {
		this.canTeleport = canTeleport;
		this.allowedStructures = allowedStructures;
		this.dimensionsForAllowedStructures = dimensionsForAllowedStructures;
	}

	public SyncPacket(FriendlyByteBuf buf) {
		canTeleport = buf.readBoolean();
		allowedStructures = new ArrayList<StructureFeature<?>>();
		dimensionsForAllowedStructures = new HashMap<StructureFeature<?>, List<ResourceLocation>>();
		int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			StructureFeature<?> structure = StructureUtils.getStructureForKey(buf.readResourceLocation());
			int numDimensions = buf.readInt();
			List<ResourceLocation> dimensions = new ArrayList<ResourceLocation>();
			for (int j = 0; j < numDimensions; j++) {
				dimensions.add(buf.readResourceLocation());
			}
			if (structure != null) {
				allowedStructures.add(structure);
				dimensionsForAllowedStructures.put(structure, dimensions);
			}
		}
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(allowedStructures.size());
		for (StructureFeature<?> structure : allowedStructures) {
			buf.writeResourceLocation(StructureUtils.getKeyForStructure(structure));
			List<ResourceLocation> dimensions = dimensionsForAllowedStructures.get(structure);
			buf.writeInt(dimensions.size());
			for (ResourceLocation dimensionKey : dimensions) {
				buf.writeResourceLocation(dimensionKey);
			}
		}
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ExplorersCompass.canTeleport = canTeleport;
			ExplorersCompass.allowedStructures = allowedStructures;
			ExplorersCompass.dimensionsForAllowedStructures = dimensionsForAllowedStructures;
		});
		ctx.get().setPacketHandled(true);
	}

}