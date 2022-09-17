package com.chaosthedude.explorerscompass.worker;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class ConcentricRingsSearchWorker extends StructureSearchWorker {

	public Iterator<Entry<StructurePlacement, Set<Holder<Structure>>>> iterator;

	public ConcentricRingsSearchWorker(ServerLevel level, Player player, ItemStack stack, List<Entry<StructurePlacement, Set<Holder<Structure>>>> entries, BlockPos startPos) {
		super(level, player, stack, entries, startPos);
		iterator = entries.iterator();
	}

	@Override
	public boolean hasWork() {
		return super.hasWork() && iterator.hasNext();
	}

	@Override
	public boolean doWork() {
		if (hasWork()) {
			Entry<StructurePlacement, Set<Holder<Structure>>> entry = iterator.next();
			ConcentricRingsStructurePlacement placement = (ConcentricRingsStructurePlacement) entry.getKey();
			Pair<BlockPos, Holder<Structure>> pair = getClosestConcentric(entry.getValue(), placement);
			if (startPos.distSqr(pair.getFirst()) < startPos.distSqr(closest.getFirst())) {
				closest = pair;
			}
		} else {
			if (closest != null) {
				succeed(closest.getSecond().get());
			} else {
				fail();
			}
		}

		if (hasWork()) {
			return true;
		}

		return false;
	}

	private Pair<BlockPos, Holder<Structure>> getClosestConcentric(Set<Holder<Structure>> structureSet, ConcentricRingsStructurePlacement placement) {
		List<ChunkPos> list = level.getChunkSource().getGenerator().getRingPositionsFor(placement, level.getChunkSource().randomState());
		if (list == null) {
			return null;
		} else {
			Pair<BlockPos, Holder<Structure>> pair = null;
			double d0 = Double.MAX_VALUE;
			BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

			for (ChunkPos chunkpos : list) {
				blockpos$mutableblockpos.set(SectionPos.sectionToBlockCoord(chunkpos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkpos.z, 8));
				double d1 = blockpos$mutableblockpos.distSqr(startPos);
				boolean flag = pair == null || d1 < d0;
				if (flag) {
					Pair<BlockPos, Holder<Structure>> pair1 = getStructureGeneratingAt(structureSet, placement, chunkpos);
					if (pair1 != null) {
						pair = pair1;
						d0 = d1;
					}
				}
			}

			return pair;
		}
	}

	private Pair<BlockPos, Holder<Structure>> getStructureGeneratingAt(Set<Holder<Structure>> holderSet, StructurePlacement placement, ChunkPos chunkPos) {
		for (Holder<Structure> holder : holderSet) {
			StructureCheckResult structurecheckresult = level.structureManager().checkStructurePresence(chunkPos, holder.value(), false);
			if (structurecheckresult != StructureCheckResult.START_NOT_PRESENT) {
				if (structurecheckresult == StructureCheckResult.START_PRESENT) {
					return Pair.of(placement.getLocatePos(chunkPos), holder);
				}

				ChunkAccess chunkaccess = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
				StructureStart structurestart = level.structureManager().getStartForStructure(SectionPos.bottomOf(chunkaccess), holder.value(), chunkaccess);
				if (structurestart != null && structurestart.isValid()) {
					return Pair.of(placement.getLocatePos(structurestart.getChunkPos()), holder);
				}
			}
		}

		return null;
	}

}
