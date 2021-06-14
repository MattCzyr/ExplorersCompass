package com.chaosthedude.explorerscompass.util;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.WorldWorkerManager;
import net.minecraftforge.registries.ForgeRegistries;

public class StructureSearchWorker implements WorldWorkerManager.IWorker {

	public ServerWorld world;
	public Structure<?> structure;
	public ResourceLocation structureKey;
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
	public SharedSeedRandom rand;
	public int x;
	public int z;
	public int lastRadiusThreshold;

	public StructureSearchWorker(ServerWorld world, PlayerEntity player, ItemStack stack, Structure<?> structure, BlockPos startPos) {
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
		finished = !world.getServer().getServerConfiguration().getDimensionGeneratorSettings().doesGenerateFeatures()
				|| !world.getChunkProvider().getChunkGenerator().getBiomeProvider().hasStructure(structure);
		structureKey = ForgeRegistries.STRUCTURE_FEATURES.getKey(structure);
		rand = new SharedSeedRandom();
		lastRadiusThreshold = 0;
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
		return !finished && getRadius() <= ConfigHandler.GENERAL.maxRadius.get() && samples <= ConfigHandler.GENERAL.maxSamples.get();
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

			StructureSeparationSettings separationSettings = world.getChunkProvider().getChunkGenerator().func_235957_b_().func_236197_a_(structure);
			ChunkPos chunkPos = structure.getChunkPosForStructure(separationSettings, world.getSeed(), rand, chunkX, chunkZ);
			IChunk chunk = world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
			StructureStart<?> structureStart = world.func_241112_a_().getStructureStart(SectionPos.from(chunk.getPos(), 0), structure, chunk);
			if (structureStart != null && structureStart.isValid()) {
				x = structureStart.getPos().getX();
				z = structureStart.getPos().getZ();
				finish(true);
				return true;
			}

			samples++;
			length++;
			if (length >= nextLength) {
				if (direction != Direction.UP) {
					nextLength++;
					direction = direction.rotateY();
				} else {
					direction = Direction.NORTH;
				}
				length = 0;
			}
			
			int radius = getRadius();
 			if (radius > 250 && radius / 250 > lastRadiusThreshold) {
 				((ExplorersCompassItem) stack.getItem()).setSearchRadius(stack, roundRadius(radius, 250), player);
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
