package com.chaosthedude.explorerscompass.worker;

import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class GenericSearchWorker extends StructureSearchWorker<StructurePlacement> {

	public int chunkX;
	public int chunkZ;
	public int length;
	public double nextLength;
	public Direction direction;

	public GenericSearchWorker(ServerLevel level, Player player, ItemStack stack, BlockPos startPos, StructurePlacement placement, List<Structure> structureSet, String managerId) {
		super(level, player, stack, startPos, placement, structureSet, managerId);
		chunkX = startPos.getX() >> 4;
		chunkZ = startPos.getZ() >> 4;
		nextLength = 1;
		length = 0;
		direction = Direction.UP;
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
			
			ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
			currentPos = new BlockPos(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 0, SectionPos.sectionToBlockCoord(chunkPos.z, 8));

			Pair<BlockPos, Structure> pair = getStructureGeneratingAt(chunkPos);
			if (pair != null) {
				succeed(pair.getFirst(), pair.getSecond());
			}

			samples++;
			length++;
			if (length >= (int)nextLength) {
				if (direction != Direction.UP) {
					nextLength += 0.5;
					direction = direction.getClockWise();
				} else {
					direction = Direction.NORTH;
				}
				length = 0;
			}

			int radius = getRadius();
			if (radius > 250 && radius / 250 > lastRadiusThreshold) {
				if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
					((ExplorersCompassItem) stack.getItem()).setSearchRadius(stack, roundRadius(radius, 250), player);
				}
				lastRadiusThreshold = radius / 250;
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
		return "GenericSearchWorker";
	}
	
	@Override
	public boolean shouldLogRadius() {
		return true;
	}

}