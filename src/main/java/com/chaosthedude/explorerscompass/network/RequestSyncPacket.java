package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.util.PlayerUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.structure.Structure;
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
			final List<Structure<?>> allowedStructures = StructureUtils.getAllowedStructures(ctx.get().getSender().getServerWorld());
 			List<ResourceLocation> allowedStructureKeys = new ArrayList<ResourceLocation>();
 			for (Structure<?> structure : allowedStructures) {
 				allowedStructureKeys.add(StructureUtils.getKeyForStructure(ctx.get().getSender().getServerWorld(), structure));
 			}
 			ExplorersCompass.network.sendTo(new SyncPacket(canTeleport, allowedStructureKeys), ctx.get().getSender().connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
		});
		ctx.get().setPacketHandled(true);
	}

}