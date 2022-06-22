package com.chaosthedude.explorerscompass.util;

import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.WorldWorkerManager;

public class StructureSearchWorker implements WorldWorkerManager.IWorker {

	public ServerLevel level;
	public List<Structure> structures;
	public ResourceLocation structureKey;
	public BlockPos startPos;
	public int samples;
	public int nextLength;
	public Direction direction;
	public ItemStack stack;
	public Player player;
	public int chunkX;
	public int chunkZ;
	public int length;
	public boolean finished;
	public int x;
	public int z;
	public int lastRadiusThreshold;

	public StructureSearchWorker(ServerLevel world, Player player, ItemStack stack, List<Structure> structures, BlockPos startPos) {
		this.level = world;
		this.player = player;
		this.stack = stack;
		this.structures = structures;
		this.startPos = startPos;
		chunkX = startPos.getX() >> 4;
		chunkZ = startPos.getZ() >> 4;
		x = startPos.getX();
		z = startPos.getZ();
		nextLength = 1;
		length = 0;
		samples = 0;
		direction = Direction.UP;
		lastRadiusThreshold = 0;
		finished = !world.getServer().getWorldData().worldGenSettings().generateStructures();
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
		if (hasWork()) {
			if (direction == Direction.NORTH) {
				chunkZ--;
			} else if (direction == Direction.EAST) {
				chunkX++;
			} else if (direction == Direction.SOUTH) {
				chunkZ++;
			} else if (direction == Direction.WEST) {
				chunkX--;
			}

			x = chunkX << 4;
			z = chunkZ << 4;

			for (Structure structure : structures) {
				StructureCheckResult checkResult = level.structureManager().checkStructurePresence(new ChunkPos(chunkX, chunkZ), structure, false);
				if (checkResult != StructureCheckResult.START_NOT_PRESENT) {
					if (checkResult == StructureCheckResult.START_PRESENT) {
						succeed(structure);
						return true;
					}
					ChunkAccess chunkAccess = level.getChunk(chunkX, chunkZ, ChunkStatus.STRUCTURE_STARTS);
					StructureStart structureStart = level.structureManager().getStartForStructure(SectionPos.bottomOf(chunkAccess), structure, chunkAccess);
					if (structureStart != null && structureStart.isValid()) {
						x = getLocatePos(structureStart.getChunkPos()).getX();
						z = getLocatePos(structureStart.getChunkPos()).getZ();
						succeed(structure);
						return true;
					}
				}
			}

			samples++;
			length++;
			if (length >= nextLength) {
				if (direction != Direction.UP) {
					nextLength++;
					direction = direction.getClockWise();
				} else {
					direction = Direction.NORTH;
				}
				length = 0;
			}

			int radius = getRadius();
			if (radius > 250 && radius / 250 > lastRadiusThreshold) {
				if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
					((ExplorersCompassItem) stack.getItem()).setSearchRadius(stack, roundRadius(radius, 250), player);
				}
				lastRadiusThreshold = radius / 250;
			}
		}
		if (hasWork()) {
			return true;
		}
		if (!finished) {
			fail();
		}
		return false;
	}

	private void succeed(Structure structure) {
		ExplorersCompass.LOGGER.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
			((ExplorersCompassItem) stack.getItem()).setFound(stack, StructureUtils.getKeyForStructure(level, structure), x, z, samples, player);
			((ExplorersCompassItem) stack.getItem()).setDisplayCoordinates(stack, ConfigHandler.GENERAL.displayCoordinates.get());
		} else {
			ExplorersCompass.LOGGER.error("Invalid compass after successful search");
		}
		finished = true;
	}
	
	private void fail() {
		ExplorersCompass.LOGGER.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
			((ExplorersCompassItem) stack.getItem()).setNotFound(stack, player, roundRadius(getRadius(), 250), samples);
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
		return StructureUtils.getHorizontalDistanceToLocation(startPos, x, z);
	}

	private int roundRadius(int radius, int roundTo) {
		return ((int) radius / roundTo) * roundTo;
	}

	private BlockPos getLocatePos(ChunkPos chunkPos) {
		return new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
	}

}
