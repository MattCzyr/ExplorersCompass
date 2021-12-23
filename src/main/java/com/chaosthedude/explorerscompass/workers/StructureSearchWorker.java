package com.chaosthedude.explorerscompass.workers;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class StructureSearchWorker implements WorldWorkerManager.IWorker {

	public ServerWorld world;
	public StructureFeature<?> structure;
	public Identifier structureKey;
	public StructureConfig structureConfig;
	public BlockPos startPos;
	public int samples;
	public int nextLength;
	public Direction direction;
	public ItemStack stack;
	public PlayerEntity player;
	public int chunkX;
	public int chunkZ;
	public int length;
	public boolean finished;
	public int x;
	public int z;
	public int lastRadiusThreshold;

	public StructureSearchWorker(ServerWorld world, PlayerEntity player, ItemStack stack, StructureFeature<?> structure, BlockPos startPos) {
		this.world = world;
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
		structureKey = StructureUtils.getIDForStructure(structure);
		lastRadiusThreshold = 0;
		structureConfig = world.getChunkManager().getChunkGenerator().getStructuresConfig().getForType(structure);
		finished = !world.getServer().getSaveProperties().getGeneratorOptions().shouldGenerateStructures()
				|| world.getChunkManager().getChunkGenerator().getStructuresConfig().getConfiguredStructureFeature(structure).isEmpty()
				|| structureConfig == null;
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.EXPLORERS_COMPASS_ITEM) {
			if (ExplorersCompassConfig.maxRadius > 0) {
				ExplorersCompass.LOGGER.info("Starting search: " + ExplorersCompassConfig.maxRadius + " max radius, " + ExplorersCompassConfig.maxSamples + " max samples");
				WorldWorkerManager.addWorker(this);
			} else {
				finish(false);
			}
		}
	}

	@Override
	public boolean hasWork() {
		return !finished && getRadius() < ExplorersCompassConfig.maxRadius && samples < ExplorersCompassConfig.maxSamples;
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

			ChunkPos chunkPos = structure.getStartChunk(structureConfig, world.getSeed(), chunkX, chunkZ);
			Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
			StructureStart<?> structureStart = world.getStructureAccessor().getStructureStart(ChunkSectionPos.from(chunk.getPos(), 0), structure, chunk);
			if (structureStart != null && structureStart.hasChildren()) {
				x = getLocatePos(structureStart.getPos()).getX();
				z = getLocatePos(structureStart.getPos()).getZ();
				finish(true);
				return true;
			}

			samples++;
			length++;
			if (length >= nextLength) {
				if (direction != Direction.UP) {
					nextLength++;
					direction = direction.rotateYClockwise();
				} else {
					direction = Direction.NORTH;
				}
				length = 0;
			}
			
			int radius = getRadius();
 			if (radius > 250 && radius / 250 > lastRadiusThreshold) {
 				if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.EXPLORERS_COMPASS_ITEM) {
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
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.EXPLORERS_COMPASS_ITEM) {
			if (found) {
				ExplorersCompass.LOGGER.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
				((ExplorersCompassItem) stack.getItem()).setFound(stack, x, z, samples, player);
				((ExplorersCompassItem) stack.getItem()).setDisplayCoordinates(stack, ExplorersCompassConfig.displayCoordinates);
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
	
	private BlockPos getLocatePos(ChunkPos chunkPos) {
		return new BlockPos(chunkPos.getStartX(), 0, chunkPos.getStartZ());
	}

}
