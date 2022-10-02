package com.chaosthedude.explorerscompass.worker;

import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraftforge.common.WorldWorkerManager;

public class RandomSpreadSearchWorker extends StructureSearchWorker<RandomSpreadStructurePlacement> {

	private int spacing;
	private int length;
	private int startSectionPosX;
	private int startSectionPosZ;
	private int x;
	private int z;

	public RandomSpreadSearchWorker(ServerLevel level, Player player, ItemStack stack, BlockPos startPos, RandomSpreadStructurePlacement placement, List<ConfiguredStructureFeature<?, ?>> configuredStructureSet, String managerId) {
		super(level, player, stack, startPos, placement, configuredStructureSet, managerId);

		spacing = placement.spacing();
		startSectionPosX = SectionPos.blockToSectionCoord(startPos.getX());
		startSectionPosZ = SectionPos.blockToSectionCoord(startPos.getZ());
		x = 0;
		z = 0;
		length = 0;

		finished = !level.getServer().getWorldData().worldGenSettings().generateFeatures();
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
				
				ChunkPos chunkPos = placement.getPotentialFeatureChunk(level.getSeed(), sampleX, sampleZ);
				currentPos = new BlockPos(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 0, SectionPos.sectionToBlockCoord(chunkPos.z, 8));
				
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
					ChunkPos chunkPos = placement.getPotentialFeatureChunk(level.getSeed(), sampleX, sampleZ);
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
