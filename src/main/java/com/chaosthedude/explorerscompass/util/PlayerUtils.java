package com.chaosthedude.explorerscompass.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.LogicalSidedProvider;

public class PlayerUtils {

	public static boolean canTeleport(Player player) {
		return cheatModeEnabled(player) || isOp(player);
	}

	public static boolean cheatModeEnabled(Player player) {
		final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
		if (server != null && server.isSingleplayer()) {
			LevelData levelData = server.getLevel(player.level.dimension()).getLevelData();
			if (levelData instanceof ServerLevelData) {
				return ((ServerLevelData) levelData).getAllowCommands();
			}
		}

		return false;
	}

	public static boolean isOp(Player player) {
		if (player instanceof ServerPlayer) {
			final ServerOpListEntry userEntry = ((ServerPlayer) player).getServer().getPlayerList().getOps().get(player.getGameProfile());
			return userEntry != null;
		}

		return false;
	}

}