package com.chaosthedude.explorerscompass.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import com.chaosthedude.explorerscompass.gui.GuiWrapper;
import com.chaosthedude.explorerscompass.network.SyncPacket;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.ItemUtils;
import com.chaosthedude.explorerscompass.util.PlayerUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.chaosthedude.explorerscompass.workers.SearchWorkerManager;
import com.google.common.collect.ListMultimap;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;

public class ExplorersCompassItem extends Item {

	public static final String NAME = "explorerscompass";

	private SearchWorkerManager workerManager;

	public ExplorersCompassItem() {
		super(new Settings().maxCount(1));
		workerManager = new SearchWorkerManager();
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (!player.isSneaking()) {
			if (isBroken(player.getStackInHand(hand))) {
				return TypedActionResult.pass(player.getStackInHand(hand));
			}

			if (world.isClient) {
				final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.EXPLORERS_COMPASS_ITEM);
				GuiWrapper.openGUI(world, player, stack);
			} else {
				final ServerWorld serverWorld = (ServerWorld) world;
				final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				final boolean canTeleport = ExplorersCompassConfig.allowTeleport && PlayerUtils.canTeleport(player);
				final int maxNextSearches = ExplorersCompassConfig.maxNextSearches;
				final boolean hasInfiniteXp = player.getAbilities().creativeMode;
				final List<Identifier> allowedStructures = StructureUtils.getAllowedStructureIDs(serverWorld);
				final Map<Identifier, Integer> xpLevels = StructureUtils.getXpLevelsForAllowedStructures(serverWorld);
				final ListMultimap<Identifier, Identifier> dimensionsForAllowedStructures = StructureUtils.getGeneratingDimensionIDsForAllowedStructures(serverWorld);
				final Map<Identifier, Identifier> structureIDsToGroupIDs = StructureUtils.getStructureIDsToGroupIDs(serverWorld);
				ServerPlayNetworking.send(serverPlayer, new SyncPacket(canTeleport, maxNextSearches, hasInfiniteXp, allowedStructures, xpLevels, dimensionsForAllowedStructures, structureIDsToGroupIDs));
			}
		} else {
			workerManager.stop();
			workerManager.clear();
			setInactive(player.getStackInHand(hand));
		}

		return TypedActionResult.pass(player.getStackInHand(hand));
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		int max = ExplorersCompassConfig.compassDurability;
		if (max > 0) {
			int damage = getDamage(stack);
			return damage > 0;
		}
		return false;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		int max = ExplorersCompassConfig.compassDurability;
		if (max > 0) {
			int damage = getDamage(stack);
			return Math.clamp(Math.round(13.0f * (1.0f - (float) damage / max)), 0, 13);
		}
		return 13;
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		int max = ExplorersCompassConfig.compassDurability;
		int damage = getDamage(stack);
		float f = max > 0 ? (float) damage / max : 0.0f;
		return net.minecraft.util.math.MathHelper.hsvToRgb(Math.max(0.0F, (1.0F - f) / 3.0F), 1.0F, 1.0F);
	}

	public void searchForStructure(ServerWorld world, PlayerEntity player, BlockPos pos, Identifier structureOrGroupID, boolean isGroup, ItemStack stack) {
		if (!isBroken(stack)) {
			setSearching(stack, structureOrGroupID, isGroup);
			setSearchRadius(stack, 0);

			List<Identifier> structureIds = List.of(structureOrGroupID);
			if (isGroup) {
				structureIds = StructureUtils.getStructuresForGroup(world, structureOrGroupID);
			}

			List<Structure> structures = new ArrayList<Structure>();
			for (Identifier id : structureIds) {
				structures.add(StructureUtils.getStructureForID(world, id));
			}
			List<BlockPos> prevPos = new ArrayList<BlockPos>();
			workerManager.stop();
			workerManager.createWorkers(world, player, stack, structures, structureOrGroupID, isGroup, pos, prevPos);
			boolean started = workerManager.start();
			if (!started) {
				setNotFound(stack, 0, 0);
			}

			int xpLevels = StructureUtils.getXpLevelsForStructure(world, structureOrGroupID);
			if (!player.getAbilities().creativeMode && xpLevels > 0) {
				player.addExperienceLevels(-xpLevels);
			}
		}
	}

	public void searchForNextStructure(ServerWorld world, PlayerEntity player, BlockPos pos, ItemStack stack) {
		if (!isBroken(stack)) {
			Identifier structureId = getStructureID(stack);
			List<BlockPos> prevPos = getPrevPos(stack);
			boolean isGroup = getIsGroup(stack);
			if (structureId != null && prevPos != null) {
				setSearchRadius(stack, 0);

				List<Identifier> structureIds;
				if (isGroup) {
					// The compass will always store the ID of the specific structure that was found, even if the
					// search itself was for a group, so we need to re-determine the group ID
					Identifier groupId = StructureUtils.getStructureIDsToGroupIDs(world).get(structureId);
					setSearching(stack, groupId, isGroup);
					structureIds = StructureUtils.getStructuresForGroup(world, groupId);
				} else {
					setSearching(stack, structureId, isGroup);
					structureIds = List.of(structureId);
				}

				List<Structure> structures = new ArrayList<Structure>();
				for (Identifier id : structureIds) {
					structures.add(StructureUtils.getStructureForID(world, id));
				}
				workerManager.stop();
				workerManager.createWorkers(world, player, stack, structures, structureId, isGroup, pos, prevPos);
				boolean started = workerManager.start();
				if (!started) {
					setNotFound(stack, 0, 0);
				}

				int xpLevels = StructureUtils.getXpLevelsForStructure(world, structureId);
				if (!player.getAbilities().creativeMode && xpLevels > 0) {
					player.addExperienceLevels(-xpLevels);
				}
			}
		}
	}

	public void succeed(ItemStack stack, Identifier structureID, boolean isGroup, int x, int z, List<BlockPos> prevPos, int samples, boolean displayCoordinates) {
		setFound(stack, structureID, isGroup, x, z, samples);
		setDisplayCoordinates(stack, displayCoordinates);
		setPrevPos(stack, prevPos);
		damageCompass(stack);
		workerManager.clear();
	}

	public void fail(ItemStack stack, Identifier structureId, int radius, int samples) {
		workerManager.pop();
		boolean started = workerManager.start();
		if (!started) {
			setNotFound(stack, radius, samples);
			if (ItemUtils.isCompass(stack)) {
				stack.set(ExplorersCompass.STRUCTURE_ID_COMPONENT, structureId.toString());
			}
		}
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.isCompass(stack)) {
			return getCompassState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	public void setSearching(ItemStack stack, Identifier structureID, boolean isGroup) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.STRUCTURE_ID_COMPONENT, structureID.toString());
			stack.set(ExplorersCompass.IS_GROUP_COMPONENT, isGroup);
			stack.set(ExplorersCompass.COMPASS_STATE_COMPONENT, CompassState.SEARCHING.getID());
		}
	}

	public void setFound(ItemStack stack, Identifier structureID, boolean isGroup, int x, int z, int samples) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.STRUCTURE_ID_COMPONENT, structureID.toString());
			stack.set(ExplorersCompass.IS_GROUP_COMPONENT, isGroup);
			stack.set(ExplorersCompass.COMPASS_STATE_COMPONENT, CompassState.FOUND.getID());
			stack.set(ExplorersCompass.FOUND_X_COMPONENT, x);
			stack.set(ExplorersCompass.FOUND_Z_COMPONENT, z);
			stack.set(ExplorersCompass.SAMPLES_COMPONENT, samples);
		}
	}

	public void setNotFound(ItemStack stack, int searchRadius, int samples) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.COMPASS_STATE_COMPONENT, CompassState.NOT_FOUND.getID());
			stack.set(ExplorersCompass.SEARCH_RADIUS_COMPONENT, searchRadius);
			stack.set(ExplorersCompass.SAMPLES_COMPONENT, samples);
		}
	}

	public void setInactive(ItemStack stack) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.COMPASS_STATE_COMPONENT, CompassState.INACTIVE.getID());
			stack.remove(ExplorersCompass.STRUCTURE_ID_COMPONENT);
			stack.remove(ExplorersCompass.IS_GROUP_COMPONENT);
			stack.remove(ExplorersCompass.PREV_POS_COMPONENT);
		}
	}

	public void setPrevPos(ItemStack stack, List<BlockPos> prevPos) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.PREV_POS_COMPONENT, prevPos);
		}
	}

	public void setState(ItemStack stack, BlockPos pos, CompassState state) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.COMPASS_STATE_COMPONENT, state.getID());
		}
	}

	public void setFoundStructureX(ItemStack stack, int x) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.FOUND_X_COMPONENT, x);
		}
	}

	public void setFoundStructureZ(ItemStack stack, int z) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.FOUND_Z_COMPONENT, z);
		}
	}

	public void setStructureKey(ItemStack stack, Identifier structureID) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.STRUCTURE_ID_COMPONENT, structureID.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.SEARCH_RADIUS_COMPONENT, searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.SAMPLES_COMPONENT, samples);
		}
	}

	public void setDisplayCoordinates(ItemStack stack, boolean displayPosition) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.DISPLAY_COORDS_COMPONENT, displayPosition);
		}
	}

	public CompassState getCompassState(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(ExplorersCompass.COMPASS_STATE_COMPONENT)) {
			return CompassState.fromID(stack.get(ExplorersCompass.COMPASS_STATE_COMPONENT));
		}

		return null;
	}

	public int getDamage(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(ExplorersCompass.DAMAGE_COMPONENT)) {
			return stack.get(ExplorersCompass.DAMAGE_COMPONENT);
		}
		return 0;
	}

	private void damageCompass(ItemStack stack) {
		int max = ExplorersCompassConfig.compassDurability;
		if (max > 0) {
			int damage = getDamage(stack) + 1;
			stack.set(ExplorersCompass.DAMAGE_COMPONENT, damage);
		}
	}

	public boolean isBroken(ItemStack stack) {
		int max = ExplorersCompassConfig.compassDurability;
		if (max > 0) {
			return getDamage(stack) >= max;
		}
		return false;
	}

	public int getFoundStructureX(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(ExplorersCompass.FOUND_X_COMPONENT)) {
			return stack.get(ExplorersCompass.FOUND_X_COMPONENT);
		}

		return 0;
	}

	public int getFoundStructureZ(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(ExplorersCompass.FOUND_Z_COMPONENT)) {
			return stack.get(ExplorersCompass.FOUND_Z_COMPONENT);
		}

		return 0;
	}

	public Identifier getStructureID(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(ExplorersCompass.STRUCTURE_ID_COMPONENT)) {
			return Identifier.of(stack.get(ExplorersCompass.STRUCTURE_ID_COMPONENT));
		}

		return null;
	}

	public List<BlockPos> getPrevPos(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(ExplorersCompass.PREV_POS_COMPONENT)) {
			return stack.get(ExplorersCompass.PREV_POS_COMPONENT);
		}

		return null;
	}

	public boolean getIsGroup(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(ExplorersCompass.IS_GROUP_COMPONENT)) {
			return stack.get(ExplorersCompass.IS_GROUP_COMPONENT);
		}

		return false;
	}

	public int getSearchRadius(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(ExplorersCompass.SEARCH_RADIUS_COMPONENT)) {
			return stack.get(ExplorersCompass.SEARCH_RADIUS_COMPONENT);
		}

		return -1;
	}

	public int getSamples(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(ExplorersCompass.SAMPLES_COMPONENT)) {
			return stack.get(ExplorersCompass.SAMPLES_COMPONENT);
		}

		return -1;
	}

	public int getDistanceToBiome(PlayerEntity player, ItemStack stack) {
		return StructureUtils.getHorizontalDistanceToLocation(player, getFoundStructureX(stack), getFoundStructureZ(stack));
	}

	public boolean shouldDisplayCoordinates(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.contains(ExplorersCompass.DISPLAY_COORDS_COMPONENT)) {
			return stack.get(ExplorersCompass.DISPLAY_COORDS_COMPONENT);
		}

		return true;
	}

}
