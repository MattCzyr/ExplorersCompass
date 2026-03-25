package com.chaosthedude.explorerscompass.worker;

import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.item.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public abstract class StructureSearchWorker<T extends StructurePlacement> implements WorldWorkerManager.IWorker {
	
	protected String managerId;
	protected ServerLevel level;
	protected Player player;
	protected ItemStack stack;
	protected BlockPos startPos;
	protected BlockPos currentPos;
	protected T placement;
	protected List<Structure> structureSet;
	protected Identifier structureOrGroupId;
	protected boolean isGroup;
	protected int samples;
	protected boolean finished;
	protected int lastRadiusThreshold;
	protected List<BlockPos> prevPos;

	public StructureSearchWorker(ServerLevel level, Player player, ItemStack stack, BlockPos startPos, List<BlockPos> prevPos, T placement, List<Structure> structureSet, Identifier structureOrGroupId, boolean isGroup, String managerId) {
		this.level = level;
		this.player = player;
		this.stack = stack;
		this.startPos = startPos;
		this.prevPos = prevPos;
		this.structureSet = structureSet;
		this.structureOrGroupId = structureOrGroupId;
		this.isGroup = isGroup;
		this.placement = placement;
		this.managerId = managerId;
		
		currentPos = startPos;
		samples = 0;
		
		finished = /*!level.getServer().getWorldData().worldGenOptions().generateStructures()*/ false;
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
			if (ConfigHandler.GENERAL.maxRadius.get() > 0) {
				ExplorersCompass.LOGGER.info("SearchWorkerManager " + managerId + ": " + getName() + " starting with " + (shouldLogRadius() ? ConfigHandler.GENERAL.maxRadius.get() + " max radius, " : "") + ConfigHandler.GENERAL.maxSamples.get() + " max samples, " + prevPos.size() + " previous locations");
				WorldWorkerManager.addWorker(this);
			} else {
				fail();
			}
		}
	}

	@Override
	public boolean hasWork() {
		return !finished  && prevPos.size() <= ConfigHandler.GENERAL.maxNextSearches.get() && getRadius() < ConfigHandler.GENERAL.maxRadius.get() && samples < ConfigHandler.GENERAL.maxSamples.get();
	}

	@Override
	public boolean doWork() {
		int radius = getRadius();
		if (radius > 250 && radius / 250 > lastRadiusThreshold) {
			if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
				stack.set(ExplorersCompass.SEARCH_RADIUS, roundRadius(radius, 250));
			}
			lastRadiusThreshold = radius / 250;
		}
		return false;
	}

	protected Pair<BlockPos, Structure> getStructureGeneratingAt(ChunkPos chunkPos) {
		for (Structure structure : structureSet) {
			StructureCheckResult result = level.structureManager().checkStructurePresence(chunkPos, structure, placement, false);
			if (result != StructureCheckResult.START_NOT_PRESENT) {
				if (result == StructureCheckResult.START_PRESENT) {
					return Pair.of(placement.getLocatePos(chunkPos), structure);
				}

				ChunkAccess chunkAccess = level.getChunk(chunkPos.x(), chunkPos.z(), ChunkStatus.STRUCTURE_STARTS);
				StructureStart structureStart = level.structureManager().getStartForStructure(SectionPos.bottomOf(chunkAccess), structure, chunkAccess);
				if (structureStart != null && structureStart.isValid()) {
					return Pair.of(placement.getLocatePos(structureStart.getChunkPos()), structure);
				}
			}
		}

		return null;
	}

	protected void succeed(BlockPos pos, Structure structure) {
		ExplorersCompass.LOGGER.info("SearchWorkerManager " + managerId + ": " + getName() + " succeeded with " + (shouldLogRadius() ? getRadius() + " radius, " : "") + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
			((ExplorersCompassItem) stack.getItem()).succeed(stack, StructureUtils.getIdForStructure(level, structure), isGroup, pos.getX(), pos.getZ(), prevPos, samples, ConfigHandler.GENERAL.displayCoordinates.get());
		} else {
			ExplorersCompass.LOGGER.error("SearchWorkerManager " + managerId + ": " + getName() + " found invalid compass after successful search");
		}
		finished = true;
	}

	protected void fail() {
		ExplorersCompass.LOGGER.info("SearchWorkerManager " + managerId + ": " + getName() + " failed with " + (shouldLogRadius() ? getRadius() + " radius, " : "") + samples + " samples");
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
			((ExplorersCompassItem) stack.getItem()).fail(stack, structureOrGroupId, roundRadius(getRadius(), 250), samples);
		} else {
			ExplorersCompass.LOGGER.error("SearchWorkerManager " + managerId + ": " + getName() + " found invalid compass after failed search");
		}
		finished = true;
	}

	public void stop() {
		ExplorersCompass.LOGGER.info("SearchWorkerManager " + managerId + ": " + getName() + " stopped with " + (shouldLogRadius() ? getRadius() + " radius, " : "") + samples + " samples");
		finished = true;
	}
	
	public boolean shouldIgnore(BlockPos pos) {
		return prevPos.contains(pos);
	}

	protected int getRadius() {
		return StructureUtils.getHorizontalDistanceToLocation(startPos, currentPos.getX(), currentPos.getZ());
	}

	protected int roundRadius(int radius, int roundTo) {
		return ((int) radius / roundTo) * roundTo;
	}
	
	protected abstract String getName();
	
	protected abstract boolean shouldLogRadius();

}
