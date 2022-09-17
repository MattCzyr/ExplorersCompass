package com.chaosthedude.explorerscompass.worker;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraftforge.common.WorldWorkerManager;

public class StructureSearchWorker implements WorldWorkerManager.IWorker {

	public ServerLevel level;
	public List<Map.Entry<StructurePlacement, Set<Holder<Structure>>>> entries;
	public BlockPos startPos;
	public BlockPos currentPos;
	public int samples;
	public ItemStack stack;
	public Player player;
	public boolean finished;
	public Pair<BlockPos, Holder<Structure>> closest;

	public StructureSearchWorker(ServerLevel level, Player player, ItemStack stack, List<Map.Entry<StructurePlacement, Set<Holder<Structure>>>> entries, BlockPos startPos) {
		this.level = level;
		this.player = player;
		this.stack = stack;
		this.entries = entries;
		this.startPos = startPos;
		currentPos = startPos;
		samples = 0;
		finished = !level.getServer().getWorldData().worldGenSettings().generateStructures();
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
			if (ConfigHandler.GENERAL.maxRadius.get() > 0) {
				ExplorersCompass.LOGGER.info("Starting search: " + ConfigHandler.GENERAL.maxRadius.get() + " max radius, " + ConfigHandler.GENERAL.maxSamples.get() + " max samples");
				WorldWorkerManager.addWorker(this);
			} else {
				fail();
			}
		}
	}

	@Override
	public boolean hasWork() {
		return !finished && getRadius() < ConfigHandler.GENERAL.maxRadius.get() && samples < ConfigHandler.GENERAL.maxSamples.get();
	}

	@Override
	public boolean doWork() {
		return false;
	}

	protected void succeed(Structure structure) {
		ExplorersCompass.LOGGER.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
			((ExplorersCompassItem) stack.getItem()).succeed(stack, StructureUtils.getKeyForStructure(level, structure), currentPos.getX(), currentPos.getZ(), samples, ConfigHandler.GENERAL.displayCoordinates.get());
		} else {
			ExplorersCompass.LOGGER.error("Invalid compass after successful search");
		}
		finished = true;
	}
	
	protected void fail() {
		ExplorersCompass.LOGGER.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
			((ExplorersCompassItem) stack.getItem()).fail(stack, roundRadius(getRadius(), 250), samples);
		} else {
			ExplorersCompass.LOGGER.error("Invalid compass after failed search");
		}
		finished = true;
	}
	
	public void stop() {
		ExplorersCompass.LOGGER.info("Search stopped: " + getRadius() + " radius, " + samples + " samples");
		finished = true;
	}

	private int getRadius() {
		return StructureUtils.getHorizontalDistanceToLocation(startPos, currentPos.getX(), currentPos.getZ());
	}

	private int roundRadius(int radius, int roundTo) {
		return ((int) radius / roundTo) * roundTo;
	}

	private BlockPos getLocatePos(ChunkPos chunkPos) {
		return new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
	}

}
