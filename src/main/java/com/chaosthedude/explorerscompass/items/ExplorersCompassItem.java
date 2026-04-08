package com.chaosthedude.explorerscompass.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.gui.GuiWrapper;
import com.chaosthedude.explorerscompass.network.SyncPacket;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.ItemUtils;
import com.chaosthedude.explorerscompass.util.PlayerUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.chaosthedude.explorerscompass.worker.SearchWorkerManager;
import com.google.common.collect.ListMultimap;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.network.PacketDistributor;

public class ExplorersCompassItem extends Item {

	public static final String NAME = "explorerscompass";

	private SearchWorkerManager workerManager;

	public ExplorersCompassItem() {
		super(new Properties().stacksTo(1));
		workerManager = new SearchWorkerManager();
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (!player.isCrouching()) {
			if (isBroken(player.getItemInHand(hand))) {
				return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, player.getItemInHand(hand));
			}

			if (level.isClientSide()) {
				final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.explorersCompass);
				GuiWrapper.openGUI(level, player, stack);
			} else {
				final ServerLevel serverLevel = (ServerLevel) level;
				final ServerPlayer serverPlayer = (ServerPlayer) player;
				final boolean canTeleport = ConfigHandler.GENERAL.allowTeleport.get() && PlayerUtils.canTeleport(player.getServer(), player);
				final int maxNextSearches = ConfigHandler.GENERAL.maxNextSearches.get();
				final boolean hasInfiniteXp = player.hasInfiniteMaterials();
				final List<ResourceLocation> allowedStructureKeys = StructureUtils.getAllowedStructureKeys(serverLevel);
				final Map<ResourceLocation, Integer> xpLevels = StructureUtils.getXpLevelsForAllowedStructures(serverLevel, allowedStructureKeys);
				final ListMultimap<ResourceLocation, ResourceLocation> generatingDimensions = StructureUtils.getGeneratingDimensionsForAllowedStructures(serverLevel, allowedStructureKeys);
				PacketDistributor.sendToPlayer(serverPlayer, new SyncPacket(canTeleport, maxNextSearches, hasInfiniteXp, allowedStructureKeys, xpLevels, generatingDimensions, StructureUtils.getStructureKeysToTypeKeys(serverLevel), StructureUtils.getTypeKeysToStructureKeys(serverLevel)));
			}
		} else {
			workerManager.stop();
			workerManager.clear();
			setInactive(player.getItemInHand(hand), player);
		}
		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, player.getItemInHand(hand));
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		if (max > 0) {
			int damage = stack.getOrDefault(ExplorersCompass.DAMAGE_COMPONENT, 0);
			return damage > 0;
		}
		return false;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		if (max > 0) {
			int damage = stack.getOrDefault(ExplorersCompass.DAMAGE_COMPONENT, 0);
			return Math.clamp(Math.round(13.0f * (1.0f - (float) damage / max)), 0, 13);
		}
		return 13;
	}

	@Override
	public int getBarColor(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		int damage = stack.getOrDefault(ExplorersCompass.DAMAGE_COMPONENT, 0);
		float f = max > 0 ? (float) damage / max : 0.0f;
		return Mth.hsvToRgb(Math.max(0.0F, (1.0F - f) / 3.0F), 1.0F, 1.0F);
	}

	@Override
 	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
 		if (getState(oldStack) == getState(newStack)) {
 			return false;
 		}
 		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
 	}

	public void searchForStructure(Level level, Player player, ResourceLocation groupOrStructureKey, List<ResourceLocation> structureKeys, BlockPos pos, ItemStack stack, boolean isGroup) {
		if (!isBroken(stack)) {
			setSearching(stack, groupOrStructureKey, isGroup, player);
			setSearchRadius(stack, 0, player);
			if (level instanceof ServerLevel) {
				ServerLevel serverLevel = (ServerLevel) level;
				List<Structure> structures = new ArrayList<Structure>();
				for (ResourceLocation key : structureKeys) {
					structures.add(StructureUtils.getStructureForKey(serverLevel, key));
				}
				List<BlockPos> prevPos = new ArrayList<BlockPos>();
				workerManager.stop();
				workerManager.createWorkers(serverLevel, player, stack, structures, groupOrStructureKey, isGroup, pos, prevPos);
				boolean started = workerManager.start();
				if (!started) {
					setNotFound(stack, 0, 0);
				}

				int xpLevelsToConsume = StructureUtils.getXpLevelsForStructure(serverLevel, groupOrStructureKey);
				if (!player.hasInfiniteMaterials() && xpLevelsToConsume > 0) {
					player.giveExperienceLevels(-xpLevelsToConsume);
				}
			}
		}
	}

	public void searchForNextStructure(ServerLevel level, Player player, BlockPos pos, ItemStack stack) {
		if (!isBroken(stack)) {
			ResourceLocation structureKey = getStructureKey(stack);
			List<BlockPos> storedPrevPos = getPrevPos(stack);
			List<BlockPos> prevPos = storedPrevPos != null ? new ArrayList<>(storedPrevPos) : null;
			boolean isGroup = getIsGroup(stack);
			if (stack.has(ExplorersCompass.STRUCTURE_ID_COMPONENT) && prevPos != null) {
				setSearchRadius(stack, 0, player);

				List<ResourceLocation> structureKeys;
				if (isGroup) {
					// The compass stores the ID of the specific structure found, so re-determine the group key
					Structure structure = StructureUtils.getStructureForKey(level, structureKey);
					ResourceLocation groupKey = StructureUtils.getTypeForStructure(level, structure);
					setSearching(stack, groupKey, isGroup, player);
					structureKeys = new ArrayList<>(StructureUtils.getTypeKeysToStructureKeys(level).get(groupKey));
				} else {
					setSearching(stack, structureKey, isGroup, player);
					structureKeys = List.of(structureKey);
				}

				List<Structure> structures = new ArrayList<Structure>();
				for (ResourceLocation key : structureKeys) {
					structures.add(StructureUtils.getStructureForKey(level, key));
				}
				workerManager.stop();
				workerManager.createWorkers(level, player, stack, structures, structureKey, isGroup, pos, prevPos);
				boolean started = workerManager.start();
				if (!started) {
					setNotFound(stack, 0, 0);
				}

                int xpLevelsToConsume = StructureUtils.getXpLevelsForStructure(level, structureKey);
                if (!player.hasInfiniteMaterials() && xpLevelsToConsume > 0) {
                    player.giveExperienceLevels(-xpLevelsToConsume);
                }
			}
		}
	}

	public void succeed(ItemStack stack, ResourceLocation structureKey, boolean isGroup, int x, int z, List<BlockPos> prevPos, int samples, boolean displayCoordinates) {
		setFound(stack, structureKey, isGroup, x, z, samples);
		setDisplayCoordinates(stack, displayCoordinates);
		if (prevPos != null) {
			stack.set(ExplorersCompass.PREV_POS_COMPONENT, prevPos);
		}
		damageCompass(stack);
		workerManager.clear();
	}

	public void fail(ItemStack stack, ResourceLocation structureKey, int radius, int samples) {
		workerManager.pop();
		boolean started = workerManager.start();
		if (!started) {
			setNotFound(stack, radius, samples);
			if (ItemUtils.isCompass(stack)) {
				stack.set(ExplorersCompass.STRUCTURE_ID_COMPONENT, structureKey.toString());
			}
		}
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.isCompass(stack)) {
			return getState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	public boolean isBroken(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		if (max > 0) {
			return getDamage(stack) >= max;
		}
		return false;
	}

	public int getDamage(ItemStack stack) {
		return stack.getOrDefault(ExplorersCompass.DAMAGE_COMPONENT, 0);
	}

	private void damageCompass(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		if (max > 0) {
			int damage = getDamage(stack) + 1;
			stack.set(ExplorersCompass.DAMAGE_COMPONENT, damage);
		}
	}

	public void setSearching(ItemStack stack, ResourceLocation structureKey, boolean isGroup, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.STRUCTURE_ID_COMPONENT, structureKey.toString());
			stack.set(ExplorersCompass.IS_GROUP_COMPONENT, isGroup);
			stack.set(ExplorersCompass.COMPASS_STATE_COMPONENT, CompassState.SEARCHING.getID());
		}
	}

	public void setFound(ItemStack stack, ResourceLocation structureKey, boolean isGroup, int x, int z, int samples) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.COMPASS_STATE_COMPONENT, CompassState.FOUND.getID());
			stack.set(ExplorersCompass.STRUCTURE_ID_COMPONENT, structureKey.toString());
			stack.set(ExplorersCompass.IS_GROUP_COMPONENT, isGroup);
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

	public void setInactive(ItemStack stack, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.COMPASS_STATE_COMPONENT, CompassState.INACTIVE.getID());
			stack.remove(ExplorersCompass.STRUCTURE_ID_COMPONENT);
			stack.remove(ExplorersCompass.PREV_POS_COMPONENT);
		}
	}

	public void setState(ItemStack stack, BlockPos pos, CompassState state, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.COMPASS_STATE_COMPONENT, state.getID());
		}
	}

	public void setFoundStructureX(ItemStack stack, int x, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.FOUND_X_COMPONENT, x);
		}
	}

	public void setFoundStructureZ(ItemStack stack, int z, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.FOUND_Z_COMPONENT, z);
		}
	}

	public void setStructureKey(ItemStack stack, ResourceLocation structureKey, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.STRUCTURE_ID_COMPONENT, structureKey.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.SEARCH_RADIUS_COMPONENT, searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples, Player player) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.SAMPLES_COMPONENT, samples);
		}
	}

	public void setDisplayCoordinates(ItemStack stack, boolean displayPosition) {
		if (ItemUtils.isCompass(stack)) {
			stack.set(ExplorersCompass.DISPLAY_COORDS_COMPONENT, displayPosition);
		}
	}

	public CompassState getState(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(ExplorersCompass.COMPASS_STATE_COMPONENT)) {
			return CompassState.fromID(stack.get(ExplorersCompass.COMPASS_STATE_COMPONENT));
		}

		return null;
	}

	public boolean getIsGroup(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(ExplorersCompass.IS_GROUP_COMPONENT)) {
			return stack.get(ExplorersCompass.IS_GROUP_COMPONENT);
		}
		return false;
	}

	public List<BlockPos> getPrevPos(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(ExplorersCompass.PREV_POS_COMPONENT)) {
			return stack.get(ExplorersCompass.PREV_POS_COMPONENT);
		}
		return null;
	}

	public int getFoundStructureX(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(ExplorersCompass.FOUND_X_COMPONENT)) {
			return stack.get(ExplorersCompass.FOUND_X_COMPONENT);
		}

		return 0;
	}

	public int getFoundStructureZ(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(ExplorersCompass.FOUND_Z_COMPONENT)) {
			return stack.get(ExplorersCompass.FOUND_Z_COMPONENT);
		}

		return 0;
	}

	public ResourceLocation getStructureKey(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(ExplorersCompass.STRUCTURE_ID_COMPONENT)) {
			return ResourceLocation.parse(stack.get(ExplorersCompass.STRUCTURE_ID_COMPONENT));
		}

		return ResourceLocation.fromNamespaceAndPath("", "");
	}

	public int getSearchRadius(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(ExplorersCompass.SEARCH_RADIUS_COMPONENT)) {
			return stack.get(ExplorersCompass.SEARCH_RADIUS_COMPONENT);
		}

		return -1;
	}

	public int getSamples(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(ExplorersCompass.SAMPLES_COMPONENT)) {
			return stack.get(ExplorersCompass.SAMPLES_COMPONENT);
		}

		return -1;
	}

	public int getDistanceToBiome(Player player, ItemStack stack) {
		return StructureUtils.getHorizontalDistanceToLocation(player, getFoundStructureX(stack), getFoundStructureZ(stack));
	}

	public boolean shouldDisplayCoordinates(ItemStack stack) {
		if (ItemUtils.isCompass(stack) && stack.has(ExplorersCompass.DISPLAY_COORDS_COMPONENT)) {
			return stack.get(ExplorersCompass.DISPLAY_COORDS_COMPONENT);
		}

		return true;
	}

}
