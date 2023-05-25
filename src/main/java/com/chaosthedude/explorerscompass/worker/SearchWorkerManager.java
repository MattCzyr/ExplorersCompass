package com.chaosthedude.explorerscompass.worker;

import com.chaosthedude.explorerscompass.util.StructureUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchWorkerManager {

    private final String id = RandomStringUtils.random(8, "0123456789abcdef");

    private final List<StructureSearchWorker<?>> workers;

    public SearchWorkerManager() {
        workers = new ArrayList<>();
    }

    public void createWorkers(ServerLevel level, Player player, ItemStack stack, List<Structure> structures, BlockPos startPos) {
        workers.clear();

        Map<StructurePlacement, List<Structure>> placementToStructuresMap = new Object2ObjectArrayMap<>();

        for (Structure structure : structures) {
            Holder<Structure> holder = StructureUtils.getHolderForStructure(level, structure);
            RandomState randomState = level.getChunkSource().randomState();

            List<StructurePlacement> result = level.getChunkSource().getGenerator().getPlacementsForStructure(holder, randomState);

            for (StructurePlacement structureplacement : result) {
                placementToStructuresMap.computeIfAbsent(structureplacement, (holderSet) -> new ObjectArrayList<>()).add(structure);
            }
        }

        for (Map.Entry<StructurePlacement, List<Structure>> entry : placementToStructuresMap.entrySet()) {
            StructurePlacement placement = entry.getKey();
            if (placement instanceof ConcentricRingsStructurePlacement) {
                workers.add(new ConcentricRingsSearchWorker(level, player, stack, startPos, (ConcentricRingsStructurePlacement) placement, entry.getValue(), id));
            } else if (placement instanceof RandomSpreadStructurePlacement) {
                workers.add(new RandomSpreadSearchWorker(level, player, stack, startPos, (RandomSpreadStructurePlacement) placement, entry.getValue(), id));
            } else {
                workers.add(new GenericSearchWorker(level, player, stack, startPos, placement, entry.getValue(), id));
            }
        }
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
