package com.chaosthedude.explorerscompass.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;

import com.chaosthedude.explorerscompass.util.StructureUtils;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;

public class SearchWorkerManager {
	
	private final String id = RandomStringUtils.random(8, "0123456789abcdef");
	
	private List<StructureSearchWorker<?>> workers;
	
	public SearchWorkerManager() {
		workers = new ArrayList<StructureSearchWorker<?>>();
	}
	
	public void createWorkers(ServerWorld world, PlayerEntity player, ItemStack stack, List<Structure> structures, Identifier structureOrGroupId, boolean isGroup, BlockPos startPos, List<BlockPos> prevPos) {
		workers.clear();

		Map<StructurePlacement, List<Structure>> placementToStructuresMap = new Object2ObjectArrayMap<>();

		for (Structure structure : structures) {
			for (StructurePlacement structureplacement : world.getChunkManager().getStructurePlacementCalculator().getPlacements(StructureUtils.getEntryForStructure(world, structure))) {
				placementToStructuresMap.computeIfAbsent(structureplacement, (holderSet) -> {
					return new ObjectArrayList<Structure>();
				}).add(structure);
			}
		}

		for (Map.Entry<StructurePlacement, List<Structure>> entry : placementToStructuresMap.entrySet()) {
			StructurePlacement placement = entry.getKey();
			List<Structure> placementStructures = entry.getValue();
			if (placement instanceof ConcentricRingsStructurePlacement) {
				workers.add(new ConcentricRingsSearchWorker(world, player, stack, startPos, prevPos, (ConcentricRingsStructurePlacement) placement, placementStructures, structureOrGroupId, isGroup, id));
			} else if (placement instanceof RandomSpreadStructurePlacement) {
				workers.add(new RandomSpreadSearchWorker(world, player, stack, startPos, prevPos, (RandomSpreadStructurePlacement) placement, placementStructures, structureOrGroupId, isGroup, id));
			} else {
				workers.add(new GenericSearchWorker(world, player, stack, startPos, prevPos, placement, placementStructures, structureOrGroupId, isGroup, id));
			}
		}
	}

	public void createWorkers(ServerWorld world, PlayerEntity player, ItemStack stack, List<Structure> structures, Identifier structureOrGroupId, boolean isGroup, BlockPos startPos) {
		createWorkers(world, player, stack, structures, structureOrGroupId, isGroup, startPos, new java.util.ArrayList<>());
	}
	
	// Returns true if a worker starts, false otherwise
	public boolean start() {
		if (!workers.isEmpty()) {
			workers.get(0).start();
			return true;
		}
		return false;
	}
	
	public void pop() {
		if (!workers.isEmpty()) {
			workers.remove(0);
		}
	}
	
	public void stop() {
		for (StructureSearchWorker<?> worker : workers) {
			worker.stop();
		}
	}
	
	public void clear() {
		workers.clear();
	}

}
