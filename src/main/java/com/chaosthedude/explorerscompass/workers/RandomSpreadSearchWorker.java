package com.chaosthedude.explorerscompass.workers;

import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;

public class RandomSpreadSearchWorker extends StructureSearchWorker<RandomSpreadStructurePlacement> {

	private int spacing;
	private int length;
	private int startSectionPosX;
	private int startSectionPosZ;
	private int x;
	private int z;

	public RandomSpreadSearchWorker(ServerWorld level, PlayerEntity player, ItemStack stack, BlockPos startPos, RandomSpreadStructurePlacement placement, List<ConfiguredStructureFeature<?, ?>> configuredStructureSet, String managerId) {
		super(level, player, stack, startPos, placement, configuredStructureSet, managerId);

		spacing = placement.spacing();
		startSectionPosX = ChunkSectionPos.getSectionCoord(startPos.getX());
		startSectionPosZ = ChunkSectionPos.getSectionCoord(startPos.getZ());
		x = 0;
		z = 0;
		length = 0;

		finished = !level.getServer().getSaveProperties().getGeneratorOptions().shouldGenerateStructures();
	}

	@Override
	public boolean hasWork() {
		return super.hasWork();
	}

	@Override
	public boolean doWork() {
		super.doWork();
		if (hasWork()) {
			boolean shouldSampleX = x == -length || x == length;
			boolean shouldSampleZ = z == -length || z == length;

			if (shouldSampleX || shouldSampleZ) {
				int sampleX = startSectionPosX + (spacing * x);
				int sampleZ = startSectionPosZ + (spacing * z);
				
				ChunkPos chunkPos = placement.getStartChunk(level.getSeed(), sampleX, sampleZ);
				currentPos = new BlockPos(ChunkSectionPos.getOffsetPos(chunkPos.x, 8), 0, ChunkSectionPos.getOffsetPos(chunkPos.z, 8));
				
				Pair<BlockPos, ConfiguredStructureFeature<?, ?>> pair = getStructureGeneratingAt(chunkPos);
				samples++;
				if (pair != null) {
					succeed(pair.getFirst(), pair.getSecond());
				}
			}

			z++;
			if (z > length) {
				z = -length;
				x++;
				if (x > length) {
					x = -length;
					length++;
				}
			}
		} else {
			if (!finished) {
				fail();
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
	
	@Override
	protected String getName() {
		return "RandomSpreadSearchWorker";
	}
	
	@Override
	public boolean shouldLogRadius() {
		return true;
	}

	// Non-optimized method to get the closest structure, for testing purposes
	private Pair<BlockPos, ConfiguredStructureFeature<?, ?>> getClosest() {
		for (int x = -length; x <= length; ++x) {
			boolean shouldSampleX = x == -length || x == length;
			for (int z = -length; z <= length; ++z) {
				boolean shouldSampleZ = z == -length || z == length;
				if (shouldSampleX || shouldSampleZ) {
					int sampleX = startSectionPosX + (spacing * x);
					int sampleZ = startSectionPosZ + (spacing * z);
					ChunkPos chunkPos = placement.getStartChunk(level.getSeed(), sampleX, sampleZ);
					Pair<BlockPos, ConfiguredStructureFeature<?, ?>> pair = getStructureGeneratingAt(chunkPos);
					if (pair != null) {
						return pair;
					}
				}
			}
		}

		return null;
	}

}