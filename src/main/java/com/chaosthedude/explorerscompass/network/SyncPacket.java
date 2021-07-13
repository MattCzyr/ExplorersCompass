package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.StructureFeature;

public class SyncPacket extends PacketByteBuf {

	public static final Identifier ID = new Identifier(ExplorersCompass.MODID, "sync");

	public SyncPacket(boolean canTeleport, List<StructureFeature<?>> allowedStructures, Map<StructureFeature<?>, List<Identifier>> dimensionsForAllowedStructures) {
		super(Unpooled.buffer());
		writeBoolean(canTeleport);
		writeInt(allowedStructures.size());
		for (StructureFeature<?> structure : allowedStructures) {
			writeIdentifier(StructureUtils.getIDForStructure(structure));
			List<Identifier> dimensions = dimensionsForAllowedStructures.get(structure);
			writeInt(dimensions.size());
			for (Identifier dimensionKey : dimensions) {
				writeIdentifier(dimensionKey);
			}
		}
	}

	public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final boolean canTeleport = buf.readBoolean();
		final List<StructureFeature<?>> allowedStructures = new ArrayList<StructureFeature<?>>();
		final Map<StructureFeature<?>, List<Identifier>> dimensionsForAllowedStructures = new HashMap<StructureFeature<?>, List<Identifier>>();
		final int numStructures = buf.readInt();
		
		for (int i = 0; i < numStructures; i++) {
			StructureFeature<?> structure = StructureUtils.getStructureForID(buf.readIdentifier());
			allowedStructures.add(structure);
			int numDimensions = buf.readInt();
			List<Identifier> dimensions = new ArrayList<Identifier>();
			for (int j = 0; j < numDimensions; j++) {
				dimensions.add(buf.readIdentifier());
			}
			dimensionsForAllowedStructures.put(structure, dimensions);
		}
		
		client.execute(() -> {
			ExplorersCompass.canTeleport = canTeleport;
			ExplorersCompass.allowedStructures = allowedStructures;
			ExplorersCompass.dimensionsForAllowedStructures = dimensionsForAllowedStructures;
		});
	}

}