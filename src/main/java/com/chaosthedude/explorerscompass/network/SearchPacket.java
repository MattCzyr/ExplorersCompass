package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.ItemUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record SearchPacket(ResourceLocation groupKey, List<ResourceLocation> structureKeys, BlockPos pos) implements CustomPacketPayload {
	
	public static final ResourceLocation ID = new ResourceLocation(ExplorersCompass.MODID, "search");

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

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeResourceLocation(groupKey);
		buf.writeInt(structureKeys.size());
		for (ResourceLocation key : structureKeys) {
			buf.writeResourceLocation(key);
		}
		buf.writeBlockPos(pos);
	}

	public static void handle(SearchPacket packet, PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
			if (context.player().isPresent() && context.level().isPresent()) {
				final ItemStack stack = ItemUtils.getHeldItem(context.player().get(), ExplorersCompass.explorersCompass);
				if (!stack.isEmpty()) {
					final ExplorersCompassItem explorersCompass = (ExplorersCompassItem) stack.getItem();
					explorersCompass.searchForStructure((ServerLevel) context.level().get(), context.player().get(), packet.groupKey, packet.structureKeys, packet.pos, stack);
				}
			}
		});
	}
	
	@Override
	public ResourceLocation id() {
		return ID;
	}

}
