package com.chaosthedude.explorerscompass.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chaosthedude.explorerscompass.util.StructureUtils;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class SearchWorkerFactory {
	
	public static List<StructureSearchWorker<?>> createWorkers(ServerLevel level, Player player, ItemStack stack, List<Structure> structures, BlockPos startPos) {
		List<StructureSearchWorker<?>> workers = new ArrayList<StructureSearchWorker<?>>();
		
		Map<StructurePlacement, List<Structure>> placementToStructuresMap = new Object2ObjectArrayMap<>();
		
		for (Structure structure : structures) {
			for (StructurePlacement structureplacement : level.getChunkSource().getGenerator().getPlacementsForStructure(StructureUtils.getHolderForStructure(level, structure), level.getChunkSource().randomState())) {
				placementToStructuresMap.computeIfAbsent(structureplacement, (holderSet) -> {
					return new ObjectArrayList<Structure>();
				}).add(structure);
			}
		}

		for (Map.Entry<StructurePlacement, List<Structure>> entry : placementToStructuresMap.entrySet()) {
			StructurePlacement placement = entry.getKey();
			if (placement instanceof ConcentricRingsStructurePlacement) {
				workers.add(new ConcentricRingsSearchWorker(level, player, stack, startPos, (ConcentricRingsStructurePlacement) placement, entry.getValue()));
			} else if (placement instanceof RandomSpreadStructurePlacement) {
				workers.add(new RandomSpreadSearchWorker(level, player, stack, startPos, (RandomSpreadStructurePlacement) placement, entry.getValue()));
			} else {
				workers.add(new GenericSearchWorker(level, player, stack, startPos, placement, entry.getValue()));
			}
		}

		return workers;
	}

}
