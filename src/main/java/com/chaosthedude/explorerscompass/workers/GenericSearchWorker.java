package com.chaosthedude.explorerscompass.workers;

import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;

public class GenericSearchWorker extends StructureSearchWorker<StructurePlacement> {

	public int chunkX;
	public int chunkZ;
	public int length;
	public double nextLength;
	public Direction direction;

	public GenericSearchWorker(ServerWorld level, PlayerEntity player, ItemStack stack, BlockPos startPos, StructurePlacement placement, List<Structure> structureSet, String managerId) {
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
			currentPos = new BlockPos(ChunkSectionPos.getOffsetPos(chunkPos.x, 8), 0, ChunkSectionPos.getOffsetPos(chunkPos.z, 8));

			Pair<BlockPos, Structure> pair = getStructureGeneratingAt(chunkPos);
			if (pair != null) {
				succeed(pair.getFirst(), pair.getSecond());
			}

			samples++;
			length++;
			if (length >= (int)nextLength) {
				if (direction != Direction.UP) {
					nextLength += 0.5;
					direction = direction.rotateYClockwise();
				} else {
					direction = Direction.NORTH;
				}
				length = 0;
			}

			int radius = getRadius();
			if (radius > 250 && radius / 250 > lastRadiusThreshold) {
				if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.EXPLORERS_COMPASS_ITEM) {
					((ExplorersCompassItem) stack.getItem()).setSearchRadius(stack, roundRadius(radius, 250));
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