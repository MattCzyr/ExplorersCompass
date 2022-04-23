package com.chaosthedude.explorerscompass.network;

import java.util.ArrayList;
import java.util.List;

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
	
	public SearchPacket(Identifier structureID, List<Identifier> configuredStructureIDs, BlockPos pos) {
		super(Unpooled.buffer());
		writeIdentifier(structureID);
		writeInt(configuredStructureIDs.size());
		for (Identifier id : configuredStructureIDs) {
			writeIdentifier(id);
		}
		writeBlockPos(pos);
	}

	public static void apply(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final Identifier structureID = buf.readIdentifier();
		final int numConfiguredStructures = buf.readInt();
		final List<Identifier> configuredStructureIDs = new ArrayList<Identifier>();
		for (int i = 0; i < numConfiguredStructures; i++) {
			configuredStructureIDs.add(buf.readIdentifier());
		}
		final BlockPos pos = buf.readBlockPos();
		
		server.execute(() -> {
			final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.EXPLORERS_COMPASS_ITEM);
			if (!stack.isEmpty()) {
				final ExplorersCompassItem explorersCompass = (ExplorersCompassItem) stack.getItem();
				final World world = player.getEntityWorld();
				explorersCompass.searchForStructure(world, player, structureID, configuredStructureIDs, pos, stack);
			}
		});
	}

}
