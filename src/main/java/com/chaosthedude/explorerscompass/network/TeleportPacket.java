package com.chaosthedude.explorerscompass.network;

import java.util.Collections;
import java.util.EnumSet;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.ItemUtils;
import com.chaosthedude.explorerscompass.util.PlayerUtils;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeleportPacket extends PacketByteBuf {

	public static final Identifier ID = new Identifier(ExplorersCompass.MODID, "teleport");

	public TeleportPacket() {
		super(Unpooled.buffer());
	}

	public static void apply(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		server.execute(() -> {
			final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.EXPLORERS_COMPASS_ITEM);
			if (!stack.isEmpty()) {
				final ExplorersCompassItem explorersCompass = (ExplorersCompassItem) stack.getItem();
				if (ExplorersCompassConfig.allowTeleport && PlayerUtils.canTeleport(player)) {
					if (explorersCompass.getState(stack) == CompassState.FOUND) {
						final int x = explorersCompass.getFoundStructureX(stack);
						final int z = explorersCompass.getFoundStructureZ(stack);
						final int y = findValidTeleportHeight(player.getEntityWorld(), x, z);

						player.stopRiding();
						((ServerPlayerEntity) player).networkHandler.requestTeleport(x, y, z, player.getYaw(), player.getPitch(), Collections.emptySet());

						if (!player.isFallFlying()) {
							player.setVelocity(player.getVelocity().getX(), 0, player.getVelocity().getZ());
							player.setOnGround(true);
						}
					}
				} else {
					ExplorersCompass.LOGGER.warn("Player " + player.getDisplayName().getString() + " tried to teleport but does not have permission.");
				}
			}
		});
	}

	private static int findValidTeleportHeight(World world, int x, int z) {
		int upY = world.getSeaLevel();
		int downY = world.getSeaLevel();
		while (!(isValidTeleportPosition(world, new BlockPos(x, upY, z)) || isValidTeleportPosition(world, new BlockPos(x, downY, z)))) {
			upY++;
			downY--;
		}
		BlockPos upPos = new BlockPos(x, upY, z);
		BlockPos downPos = new BlockPos(x, downY, z);
		if (isValidTeleportPosition(world, upPos)) {
			return upY;
		}
		if (isValidTeleportPosition(world, downPos)) {
			return downY;
		}
		return 256;
	}
	
	private static boolean isValidTeleportPosition(World world, BlockPos pos) {
		return !world.isOutOfHeightLimit(pos) && isFree(world, pos) && isFree(world, pos.up()) && !isFree(world, pos.down());
	}
	
	private static boolean isFree(World world, BlockPos pos) {
		return world.getBlockState(pos).isAir() || world.getBlockState(pos).isIn(BlockTags.FIRE) || world.getBlockState(pos).getMaterial().isLiquid() || world.getBlockState(pos).getMaterial().isReplaceable();
	}

}
