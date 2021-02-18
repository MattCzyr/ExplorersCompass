package com.chaosthedude.explorerscompass.network;

import java.util.function.Supplier;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.util.PlayerUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class RequestSyncPacket {

	public RequestSyncPacket() {}

	public RequestSyncPacket(PacketBuffer buf) {}

	public void fromBytes(PacketBuffer buf) {}

	public void toBytes(PacketBuffer buf) {}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			final boolean canTeleport = ConfigHandler.GENERAL.allowTeleport.get() && PlayerUtils.canTeleport(ctx.get().getSender());
			ExplorersCompass.network.sendTo(new SyncPacket(canTeleport, StructureUtils.getAllowedStructures()), ctx.get().getSender().connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
		});
		ctx.get().setPacketHandled(true);
	}

}