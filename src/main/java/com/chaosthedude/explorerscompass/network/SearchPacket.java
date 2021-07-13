package com.chaosthedude.explorerscompass.network;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.ItemUtils;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SearchPacket extends PacketByteBuf {
	
	public static final Identifier ID = new Identifier(ExplorersCompass.MODID, "search");
	
	public SearchPacket(Identifier structureID, BlockPos pos) {
		super(Unpooled.buffer());
		writeIdentifier(structureID);
		writeBlockPos(pos);
	}

	public static void apply(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final Identifier structureID = buf.readIdentifier();
		final BlockPos pos = buf.readBlockPos();
		
		server.execute(() -> {
			final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.EXPLORERS_COMPASS_ITEM);
			if (!stack.isEmpty()) {
				final ExplorersCompassItem explorersCompass = (ExplorersCompassItem) stack.getItem();
				final World world = player.getEntityWorld();
				explorersCompass.searchForStructure(world, player, structureID, pos, stack);
			}
		});
	}

}
