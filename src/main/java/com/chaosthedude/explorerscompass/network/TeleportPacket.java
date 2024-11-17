package com.chaosthedude.explorerscompass.network;

import java.util.Collections;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.ItemUtils;
import com.chaosthedude.explorerscompass.util.PlayerUtils;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record TeleportPacket() implements CustomPayload {

	public static final CustomPayload.Id<TeleportPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(ExplorersCompass.MODID, "teleport"));
	
	public static final PacketCodec<RegistryByteBuf, TeleportPacket> PACKET_CODEC = PacketCodec.of(TeleportPacket::write, TeleportPacket::read);

	public static TeleportPacket read(RegistryByteBuf buf) {
		return new TeleportPacket();
	}
	
	public void write(RegistryByteBuf buf) {
	}

	public static void apply(TeleportPacket packet, ServerPlayNetworking.Context context) {
		context.player().getServer().execute(() -> {
			final ItemStack stack = ItemUtils.getHeldItem(context.player(), ExplorersCompass.EXPLORERS_COMPASS_ITEM);
			if (!stack.isEmpty()) {
				final ExplorersCompassItem explorersCompass = (ExplorersCompassItem) stack.getItem();
				if (ExplorersCompassConfig.allowTeleport && PlayerUtils.canTeleport(context.player())) {
					if (explorersCompass.getState(stack) == CompassState.FOUND) {
						final int x = explorersCompass.getFoundStructureX(stack);
						final int z = explorersCompass.getFoundStructureZ(stack);
						final int y = findValidTeleportHeight(context.player().getEntityWorld(), x, z);

						context.player().stopRiding();
						context.player().networkHandler.requestTeleport(x, y, z, context.player().getYaw(), context.player().getPitch());

						if (!context.player().isGliding()) {
							context.player().setVelocity(context.player().getVelocity().getX(), 0, context.player().getVelocity().getZ());
							context.player().setOnGround(true);
						}
					}
				} else {
					ExplorersCompass.LOGGER.warn("Player " + context.player().getDisplayName().getString() + " tried to teleport but does not have permission.");
				}
			}
		});
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}

	private static int findValidTeleportHeight(World world, int x, int z) {
		int upY = world.getSeaLevel();
		int downY = world.getSeaLevel();
		while ((!world.isOutOfHeightLimit(upY) || !world.isOutOfHeightLimit(downY)) && !(isValidTeleportPosition(world, new BlockPos(x, upY, z)) || isValidTeleportPosition(world, new BlockPos(x, downY, z)))) {
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
		return isFree(world, pos) && isFree(world, pos.up()) && !isFree(world, pos.down());
	}
	
	private static boolean isFree(World world, BlockPos pos) {
		return world.getBlockState(pos).isAir() || world.getBlockState(pos).isIn(BlockTags.FIRE) || world.getBlockState(pos).isLiquid() || world.getBlockState(pos).isReplaceable();
	}

}
