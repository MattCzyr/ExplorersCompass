package com.chaosthedude.explorerscompass.network;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.item.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.ItemUtils;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public record SearchForNextPacket() implements CustomPacketPayload {

	public static final Type<SearchForNextPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, "continue_search"));

	public static final StreamCodec<FriendlyByteBuf, SearchForNextPacket> CODEC = StreamCodec.ofMember(SearchForNextPacket::write, SearchForNextPacket::read);

	public static SearchForNextPacket read(FriendlyByteBuf buf) {
		return new SearchForNextPacket();
	}

	public void write(FriendlyByteBuf buf) {
	}

	public static void handle(SearchForNextPacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			final ItemStack stack = ItemUtils.getHeldItem(context.player(), ExplorersCompass.EXPLORERS_COMPASS_ITEM);
			if (!stack.isEmpty()) {
				final ExplorersCompassItem explorersCompass = (ExplorersCompassItem) stack.getItem();
				explorersCompass.searchForNextStructure((ServerLevel) context.player().level(), context.player(), context.player().blockPosition(), stack);
			}
		});
	}

	@Override
	public Type<SearchForNextPacket> type() {
		return TYPE;
	}

}
