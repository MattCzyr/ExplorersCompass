package com.chaosthedude.explorerscompass.util;

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
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.WorldWorkerManager;
import net.minecraftforge.registries.ForgeRegistries;

public class StructureSearchWorker implements WorldWorkerManager.IWorker {

	public ServerLevel level;
	public StructureFeature<?> structure;
	public ResourceLocation structureKey;
	public StructureFeatureConfiguration structureConfig;
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
	public WorldgenRandom rand;
	public int x;
	public int z;
	public int lastRadiusThreshold;

	public StructureSearchWorker(ServerLevel world, Player player, ItemStack stack, StructureFeature<?> structure, BlockPos startPos) {
		this.level = world;
		this.player = player;
		this.stack = stack;
		this.structure = structure;
		this.startPos = startPos;
		chunkX = startPos.getX() >> 4;
		chunkZ = startPos.getZ() >> 4;
		x = startPos.getX();
		z = startPos.getZ();
		nextLength = 1;
		length = 0;
		samples = 0;
		direction = Direction.UP;
		structureKey = ForgeRegistries.STRUCTURE_FEATURES.getKey(structure);
		rand = new WorldgenRandom();
		lastRadiusThreshold = 0;
		structureConfig = world.getChunkSource().getGenerator().getSettings().getConfig(structure);
		finished = !world.getServer().getWorldData().worldGenSettings().generateFeatures()
				|| !world.getChunkSource().getGenerator().getBiomeSource().canGenerateStructure(structure)
				|| structureConfig == null;
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
			if (ConfigHandler.GENERAL.maxRadius.get() > 0) {
				ExplorersCompass.LOGGER.info("Starting search: " + ConfigHandler.GENERAL.maxRadius.get() + " max radius, " + ConfigHandler.GENERAL.maxSamples.get() + " max samples");
				WorldWorkerManager.addWorker(this);
			} else {
				finish(false);
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

			ChunkPos chunkPos = structure.getPotentialFeatureChunk(structureConfig, level.getSeed(), rand, chunkX, chunkZ);
			ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
			StructureStart<?> structureStart = level.structureFeatureManager().getStartForFeature(SectionPos.bottomOf(chunk), structure, chunk);
			if (structureStart != null && structureStart.isValid()) {
				x = structureStart.getLocatePos().getX();
				z = structureStart.getLocatePos().getZ();
				finish(true);
				return true;
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
		finish(false);
		return false;
	}

	private void finish(boolean found) {
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
			if (found) {
				ExplorersCompass.LOGGER.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
				((ExplorersCompassItem) stack.getItem()).setFound(stack, x, z, samples, player);
				((ExplorersCompassItem) stack.getItem()).setDisplayCoordinates(stack, ConfigHandler.GENERAL.displayCoordinates.get());
			} else {
				ExplorersCompass.LOGGER.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
				((ExplorersCompassItem) stack.getItem()).setNotFound(stack, player, roundRadius(getRadius(), 250), samples);
			}
		} else {
			ExplorersCompass.LOGGER.error("Invalid compass after search");
		}
		finished = true;
	}

	private int getRadius() {
		return StructureUtils.getDistanceToStructure(startPos, x, z);
	}
	
	private int roundRadius(int radius, int roundTo) {
 		return ((int) radius / roundTo) * roundTo;
 	}

}
