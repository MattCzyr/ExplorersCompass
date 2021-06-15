package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncPacket {

	private boolean canTeleport;
	private List<Structure<?>> allowedStructures;
	private Map<Structure<?>, List<ResourceLocation>> dimensionsForAllowedStructures;

	public SyncPacket() {}

	public SyncPacket(boolean canTeleport, List<Structure<?>> allowedStructures, Map<Structure<?>, List<ResourceLocation>> dimensionsForAllowedStructures) {
		this.canTeleport = canTeleport;
		this.allowedStructures = allowedStructures;
		this.dimensionsForAllowedStructures = dimensionsForAllowedStructures;
	}

	public SyncPacket(PacketBuffer buf) {
		canTeleport = buf.readBoolean();
		allowedStructures = new ArrayList<Structure<?>>();
		dimensionsForAllowedStructures = new HashMap<Structure<?>, List<ResourceLocation>>();
		int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			Structure<?> structure = StructureUtils.getStructureForKey(buf.readResourceLocation());
			allowedStructures.add(structure);
			int numDimensions = buf.readInt();
			List<ResourceLocation> dimensions = new ArrayList<ResourceLocation>();
			for (int j = 0; j < numDimensions; j++) {
				dimensions.add(buf.readResourceLocation());
			}
			dimensionsForAllowedStructures.put(structure, dimensions);
		}
	}

	public void toBytes(PacketBuffer buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(allowedStructures.size());
		for (Structure<?> structure : allowedStructures) {
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