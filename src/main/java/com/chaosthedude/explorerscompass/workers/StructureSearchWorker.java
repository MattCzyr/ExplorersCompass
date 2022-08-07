package com.chaosthedude.explorerscompass.workers;

import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructurePresence;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.structure.Structure;

public class StructureSearchWorker implements WorldWorkerManager.IWorker {

	public ServerWorld world;
	public List<Structure> structures;
	public BlockPos startPos;
	public int samples;
	public double nextLength;
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

	public StructureSearchWorker(ServerWorld world, PlayerEntity player, ItemStack stack, List<Structure> structures, BlockPos startPos) {
		this.world = world;
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
		finished = !world.getServer().getSaveProperties().getGeneratorOptions().shouldGenerateStructures();
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.EXPLORERS_COMPASS_ITEM) {
			if (ExplorersCompassConfig.maxRadius > 0) {
				ExplorersCompass.LOGGER.info("Starting search: " + ExplorersCompassConfig.maxRadius + " max radius, " + ExplorersCompassConfig.maxSamples + " max samples");
				WorldWorkerManager.addWorker(this);
			} else {
				succeed(null);
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

			for (Structure structure : structures) {
				StructurePresence checkResult = world.getStructureAccessor().getStructurePresence(new ChunkPos(chunkX, chunkZ), structure, false);
				if (checkResult != StructurePresence.START_NOT_PRESENT) {
					if (checkResult == StructurePresence.START_PRESENT) {
						succeed(structure);
						return true;
					}
					Chunk chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.STRUCTURE_STARTS);
					StructureStart structureStart = world.getStructureAccessor().getStructureStart(ChunkSectionPos.from(chunk), structure, chunk);
					if (structureStart != null && structureStart.hasChildren()) {
						x = getLocatePos(structureStart.getPos()).getX();
						z = getLocatePos(structureStart.getPos()).getZ();
						succeed(structure);
						return true;
					}
				}
			}

			samples++;
			length++;
			if (length >= (int) nextLength) {
				if (direction != Direction.UP) {
					nextLength += 0.5;
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
		if (!finished) {
			fail();
		}
		return false;
	}

	private void succeed(Structure structure) {
		ExplorersCompass.LOGGER.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.EXPLORERS_COMPASS_ITEM) {
			((ExplorersCompassItem) stack.getItem()).succeed(stack, player, StructureUtils.getIDForStructure(world, structure), x, z, samples, ExplorersCompassConfig.displayCoordinates);
		} else {
			ExplorersCompass.LOGGER.error("Invalid compass after search");
		}
		finished = true;
	}
	
	private void fail() {
		ExplorersCompass.LOGGER.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.EXPLORERS_COMPASS_ITEM) {
			((ExplorersCompassItem) stack.getItem()).fail(stack, player, roundRadius(getRadius(), 250), samples);
		} else {
			ExplorersCompass.LOGGER.error("Invalid compass after search");
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
		return new BlockPos(chunkPos.getStartX(), 0, chunkPos.getStartZ());
	}

}
