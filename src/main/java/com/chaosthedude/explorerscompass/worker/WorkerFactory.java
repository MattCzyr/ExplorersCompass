package com.chaosthedude.explorerscompass.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class WorkerFactory {
	
	private static final Map<Structure, List<StructurePlacement>> placementsForStructure = new Object2ObjectOpenHashMap<>();

	public static StructureSearchWorker createWorker(ServerLevel level, Player player, ItemStack stack, HolderSet<Structure> structureSet, BlockPos startPos) {
		Map<StructurePlacement, Set<Holder<Structure>>> map = new Object2ObjectArrayMap<>();
		
		for (Holder<Structure> holder : structureSet) {
			for (StructurePlacement structureplacement : getPlacementsForStructure(level.getChunkSource().getGenerator(), holder, level.getChunkSource().randomState())) {
				map.computeIfAbsent(structureplacement, (p_223127_) -> {
					return new ObjectArraySet();
				}).add(holder);
			}
		}
		
        List<Map.Entry<StructurePlacement, Set<Holder<Structure>>>> randomSpreadList = new ArrayList<>(map.size());
        List<Map.Entry<StructurePlacement, Set<Holder<Structure>>>> concentricRingsList = new ArrayList<>(map.size());

        for(Map.Entry<StructurePlacement, Set<Holder<Structure>>> entry : map.entrySet()) {
           StructurePlacement placement = entry.getKey();
           if (placement instanceof ConcentricRingsStructurePlacement) {
              concentricRingsList.add(entry);
           } else if (placement instanceof RandomSpreadStructurePlacement) {
        	   randomSpreadList.add(entry);
           }
        }
        
        if (!randomSpreadList.isEmpty() && !concentricRingsList.isEmpty()) {
        	System.out.println("CONTAINS BOTH RANDOM SPREAD AND CONCENTRIC RINGS");
        }
        
        if (!concentricRingsList.isEmpty()) {
        	return new ConcentricRingsSearchWorker(level, player, stack, concentricRingsList, startPos);
        }
        
        if (!randomSpreadList.isEmpty()) {
        	return new RandomSpreadStructurePlacement(level, player, stack, randomSpreadList, startPos);
        }

		return null;
	}
	
	private static List<StructurePlacement> getPlacementsForStructure(ChunkGenerator chunkGen, Holder<Structure> holder, RandomState random) {
		chunkGen.ensureStructuresGenerated(random);
		return placementsForStructure.getOrDefault(holder.value(), List.of());
	}

}
