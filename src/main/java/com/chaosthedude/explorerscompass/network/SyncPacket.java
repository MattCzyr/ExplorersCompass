package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncPacket {

	private boolean canTeleport;
	private List<Structure<?>> allowedStructures;

	public SyncPacket() {}

	public SyncPacket(boolean canTeleport, List<Structure<?>> allowedStructures) {
		this.canTeleport = canTeleport;
		this.allowedStructures = allowedStructures;
	}

	public SyncPacket(PacketBuffer buf) {
		canTeleport = buf.readBoolean();
		allowedStructures = new ArrayList<Structure<?>>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			allowedStructures.add(StructureUtils.getStructureForKey(buf.readResourceLocation()));
		}
	}

	public void toBytes(PacketBuffer buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(allowedStructures.size());
		for (Structure<?> structure : allowedStructures) {
			buf.writeResourceLocation(StructureUtils.getKeyForStructure(structure));
		}
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ExplorersCompass.canTeleport = canTeleport;
			ExplorersCompass.allowedStructures = allowedStructures;
		});
		ctx.get().setPacketHandled(true);
	}

}