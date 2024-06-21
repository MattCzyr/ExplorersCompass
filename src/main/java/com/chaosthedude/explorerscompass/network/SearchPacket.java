package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.ItemUtils;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record SearchPacket(Identifier groupID, List<Identifier> structureIDs, BlockPos pos) implements CustomPayload {
	
	public static final CustomPayload.Id<SearchPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(ExplorersCompass.MODID, "search"));
	
	public static final PacketCodec<RegistryByteBuf, SearchPacket> PACKET_CODEC = PacketCodec.of(SearchPacket::write, SearchPacket::read);
	
	public static SearchPacket read(RegistryByteBuf buf) {
		final Identifier groupID = buf.readIdentifier();
		final int numStructures = buf.readInt();
		final List<Identifier> structureIDs = new ArrayList<Identifier>();
		for (int i = 0; i < numStructures; i++) {
			structureIDs.add(buf.readIdentifier());
		}
		final BlockPos pos = buf.readBlockPos();
		return new SearchPacket(groupID, structureIDs, pos);
	}
	
	public void write(RegistryByteBuf buf) {
		buf.writeIdentifier(groupID);
		buf.writeInt(structureIDs.size());
		for (Identifier id : structureIDs) {
			buf.writeIdentifier(id);
		}
		buf.writeBlockPos(pos);
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}

	public static void apply(SearchPacket packet, ServerPlayNetworking.Context context) {
		context.player().getServer().execute(() -> {
			final ItemStack stack = ItemUtils.getHeldItem(context.player(), ExplorersCompass.EXPLORERS_COMPASS_ITEM);
			if (!stack.isEmpty()) {
				final ExplorersCompassItem explorersCompass = (ExplorersCompassItem) stack.getItem();
				explorersCompass.searchForStructure(context.player().getEntityWorld(), context.player(), packet.groupID, packet.structureIDs, packet.pos, stack);
			}
		});
	}

}
