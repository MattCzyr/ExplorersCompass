package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.ItemUtils;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public record SearchPacket(Identifier groupId, List<Identifier> structureIds, BlockPos pos) implements CustomPacketPayload {
	
	public static final Type<SearchPacket> TYPE = new Type<SearchPacket>(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "search"));
	
	public static final StreamCodec<FriendlyByteBuf, SearchPacket> CODEC = StreamCodec.ofMember(SearchPacket::write, SearchPacket::read);

	public static SearchPacket read(FriendlyByteBuf buf) {
		final Identifier groupId = buf.readIdentifier();
		final List<Identifier> structureIds = new ArrayList<Identifier>();
		int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			structureIds.add(buf.readIdentifier());
		}
		final BlockPos pos = buf.readBlockPos();
		return new SearchPacket(groupId, structureIds, pos);
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeIdentifier(groupId);
		buf.writeInt(structureIds.size());
		for (Identifier structureId : structureIds) {
			buf.writeIdentifier(structureId);
		}
		buf.writeBlockPos(pos);
	}

	public static void handle(SearchPacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			final ItemStack stack = ItemUtils.getHeldItem(context.player(), ExplorersCompass.EXPLORERS_COMPASS_ITEM);
			if (!stack.isEmpty()) {
				final ExplorersCompassItem explorersCompass = (ExplorersCompassItem) stack.getItem();
				explorersCompass.searchForStructure((ServerLevel) context.player().level(), context.player(), packet.groupId, packet.structureIds, packet.pos, stack);
			}
		});
	}
	
	@Override
	public Type<SearchPacket> type() {
		return TYPE;
	}
}