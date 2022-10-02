package com.chaosthedude.explorerscompass.workers;

import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.StructurePresence;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;

public class StructureSearchWorker<T extends StructurePlacement> implements WorldWorkerManager.IWorker {

	protected ServerWorld level;
	protected PlayerEntity player;
	protected ItemStack stack;
	protected BlockPos startPos;
	protected BlockPos currentPos;
	protected T placement;
	protected List<ConfiguredStructureFeature<?, ?>> configuredStructureSet;
	protected int samples;
	protected boolean finished;
	protected int lastRadiusThreshold;

	public StructureSearchWorker(ServerWorld level, PlayerEntity player, ItemStack stack, BlockPos startPos, T placement, List<ConfiguredStructureFeature<?, ?>> configuredStructureSet) {
		this.level = level;
		this.player = player;
		this.stack = stack;
		this.startPos = startPos;
		this.configuredStructureSet = configuredStructureSet;
		this.placement = placement;
		
		currentPos = startPos;
		samples = 0;
		
		finished = !level.getServer().getSaveProperties().getGeneratorOptions().shouldGenerateStructures();
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.EXPLORERS_COMPASS_ITEM) {
			if (ExplorersCompassConfig.maxRadius > 0) {
				ExplorersCompass.LOGGER.info("Starting search: " + ExplorersCompassConfig.maxRadius + " max radius, " + ExplorersCompassConfig.maxSamples + " max samples");
				WorldWorkerManager.addWorker(this);
			} else {
				fail();
			}
		}
	}

	@Override
	public boolean hasWork() {
		return !finished && getRadius() < ExplorersCompassConfig.maxRadius && samples < ExplorersCompassConfig.maxSamples;
	}

	@Override
	public boolean doWork() {
		int radius = getRadius();
		if (radius > 250 && radius / 250 > lastRadiusThreshold) {
			if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.EXPLORERS_COMPASS_ITEM) {
				((ExplorersCompassItem) stack.getItem()).setSearchRadius(stack, roundRadius(radius, 250));
			}
			lastRadiusThreshold = radius / 250;
		}
		return false;
	}

	protected Pair<BlockPos, ConfiguredStructureFeature<?, ?>> getStructureGeneratingAt(ChunkPos chunkPos) {
		for (ConfiguredStructureFeature<?, ?> configuredStructure : configuredStructureSet) {
			StructurePresence result = level.getStructureAccessor().getStructurePresence(chunkPos, configuredStructure, false);
			if (result != StructurePresence.START_NOT_PRESENT) {
				if (result == StructurePresence.START_PRESENT) {
					return Pair.of(getLocatePos(chunkPos), configuredStructure);
				}

				Chunk chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
				StructureStart structureStart = level.getStructureAccessor().getStructureStart(ChunkSectionPos.from(chunk), configuredStructure, chunk);
				if (structureStart != null && structureStart.hasChildren()) {
					return Pair.of(getLocatePos(structureStart.getPos()), configuredStructure);
				}
			}
		}

		return null;
	}

	protected void succeed(BlockPos pos, ConfiguredStructureFeature<?, ?> structure) {
		ExplorersCompass.LOGGER.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.EXPLORERS_COMPASS_ITEM) {
			((ExplorersCompassItem) stack.getItem()).succeed(stack, StructureUtils.getIDForConfiguredStructure(level, structure), pos.getX(), pos.getZ(), samples, ExplorersCompassConfig.displayCoordinates);
		} else {
			ExplorersCompass.LOGGER.error("Invalid compass after successful search");
		}
		finished = true;
	}

	protected void fail() {
		ExplorersCompass.LOGGER.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.EXPLORERS_COMPASS_ITEM) {
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

	protected int getRadius() {
		return StructureUtils.getHorizontalDistanceToLocation(startPos, currentPos.getX(), currentPos.getZ());
	}

	protected int roundRadius(int radius, int roundTo) {
		return ((int) radius / roundTo) * roundTo;
	}
	
	private BlockPos getLocatePos(ChunkPos chunkPos) {
		return new BlockPos(chunkPos.getStartX(), 0, chunkPos.getStartZ());
	}

}