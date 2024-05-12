package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.ItemUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SearchPacket(ResourceLocation groupKey, List<ResourceLocation> structureKeys, BlockPos pos) implements CustomPacketPayload {
	
	public static final Type<SearchPacket> TYPE = new Type<SearchPacket>(new ResourceLocation(ExplorersCompass.MODID, "search"));
	
	public static final StreamCodec<FriendlyByteBuf, SearchPacket> CODEC = StreamCodec.ofMember(SearchPacket::write, SearchPacket::read);

	public static SearchPacket read(FriendlyByteBuf buf) {
		final ResourceLocation groupKey = buf.readResourceLocation();
		final List<ResourceLocation> structureKeys = new ArrayList<ResourceLocation>();
		int numStructures = buf.readInt();
		for (int i = 0; i < numStructures; i++) {
			structureKeys.add(buf.readResourceLocation());
		}
		final BlockPos pos = buf.readBlockPos();
		return new SearchPacket(groupKey, structureKeys, pos);
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeResourceLocation(groupKey);
		buf.writeInt(structureKeys.size());
		for (ResourceLocation key : structureKeys) {
			buf.writeResourceLocation(key);
		}
		buf.writeBlockPos(pos);
	}

	public static void handle(SearchPacket packet, IPayloadContext context) {
		if (context.flow().isServerbound()) {
			context.enqueueWork(() -> {
				final ItemStack stack = ItemUtils.getHeldItem(context.player(), ExplorersCompass.explorersCompass);
				if (!stack.isEmpty()) {
					final ExplorersCompassItem explorersCompass = (ExplorersCompassItem) stack.getItem();
					explorersCompass.searchForStructure((ServerLevel) context.player().level(), context.player(), packet.groupKey, packet.structureKeys, packet.pos, stack);
				}
			});
		}
	}
	
	@Override
	public Type<SearchPacket> type() {
		return TYPE;
	}
}
