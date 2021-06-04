package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.chaosthedude.explorerscompass.ExplorersCompass;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncPacket {

	private boolean canTeleport;
	private List<ResourceLocation> allowedStructures;

	public SyncPacket() {}

	public SyncPacket(boolean canTeleport, List<ResourceLocation> allowedStructures) {
		this.canTeleport = canTeleport;
		this.allowedStructures = allowedStructures;
	}

	public SyncPacket(PacketBuffer buf) {
		canTeleport = buf.readBoolean();
		allowedStructures = new ArrayList<ResourceLocation>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			allowedStructures.add(buf.readResourceLocation());
		}
	}

	public void toBytes(PacketBuffer buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(allowedStructures.size());
		for (ResourceLocation structureKey : allowedStructures) {
			buf.writeResourceLocation(structureKey);
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