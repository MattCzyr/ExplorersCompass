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

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.minecraftforge.network.NetworkDirection;

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
				return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
			}
			if (level.isClientSide()) {
				final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.explorersCompass);
				GuiWrapper.openGUI(level, player, stack);
			} else {
				final ServerLevel serverLevel = (ServerLevel) level;
				final ServerPlayer serverPlayer = (ServerPlayer) player;
				final boolean canTeleport = ConfigHandler.GENERAL.allowTeleport.get() && PlayerUtils.canTeleport(player.getServer(), player);
				final int maxNextSearches = ConfigHandler.GENERAL.maxNextSearches.get();
				final boolean infiniteXp = player.getAbilities().instabuild;
				final List<ResourceLocation> allowedStructureKeys = StructureUtils.getAllowedStructureKeys(serverLevel);
				final Map<ResourceLocation, Integer> xpLevels = StructureUtils.getXpLevelsForAllowedStructures(serverLevel);
				ExplorersCompass.network.sendTo(new SyncPacket(canTeleport, maxNextSearches, infiniteXp, allowedStructureKeys, xpLevels, StructureUtils.getGeneratingDimensionsForAllowedStructures(serverLevel), StructureUtils.getStructureKeysToTypeKeys(serverLevel)), serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			}
		} else {
			workerManager.stop();
			workerManager.clear();
			ItemStack stack = player.getItemInHand(hand);
			setState(stack, null, CompassState.INACTIVE, player);
			stack.getOrCreateTag().remove("PrevPos");
		}
		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, player.getItemInHand(hand));
	}

	@Override
 	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
 		if (getState(oldStack) == getState(newStack)) {
 			return false;
 		}
 		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
 	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		if (max > 0) {
			int damage = getDamage(stack);
			return damage > 0;
		}
		return false;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		if (max > 0) {
			int damage = getDamage(stack);
			return Math.max(0, Math.min(13, Math.round(13.0f * (1.0f - (float) damage / max))));
		}
		return 13;
	}

	@Override
	public int getBarColor(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		int damage = getDamage(stack);
		float f = max > 0 ? (float) damage / max : 0.0f;
		return Mth.hsvToRgb(Math.max(0.0F, (1.0F - f) / 3.0F), 1.0F, 1.0F);
	}

	public void searchForStructure(ServerLevel level, Player player, BlockPos pos, ResourceLocation structureOrGroupKey, boolean isGroup, ItemStack stack) {
		if (!isBroken(stack)) {
			setSearching(stack, structureOrGroupKey, player);
			setSearchRadius(stack, 0, player);
			setIsGroup(stack, isGroup);

			List<ResourceLocation> structureKeys = List.of(structureOrGroupKey);
			if (isGroup) {
				structureKeys = StructureUtils.getStructuresForGroup(level, structureOrGroupKey);
			}

			List<Structure> structures = new ArrayList<Structure>();
			for (ResourceLocation key : structureKeys) {
				structures.add(StructureUtils.getStructureForKey(level, key));
			}
			List<BlockPos> prevPos = new ArrayList<BlockPos>();
			workerManager.stop();
			workerManager.createWorkers(level, player, stack, structures, structureOrGroupKey, isGroup, pos, prevPos);
			boolean started = workerManager.start();
			if (!started) {
				setNotFound(stack, 0, 0);
			}

			int xpLevelCost = StructureUtils.getXpLevelsForStructure(level, structureOrGroupKey);
			if (!player.getAbilities().instabuild && xpLevelCost > 0) {
				player.giveExperienceLevels(-xpLevelCost);
			}
		}
	}

	public void searchForNextStructure(ServerLevel level, Player player, BlockPos pos, ItemStack stack) {
		if (!isBroken(stack)) {
			ResourceLocation structureKey = getStructureKey(stack);
			List<BlockPos> prevPos = getPrevPos(stack);
			boolean isGroup = getIsGroup(stack);
			if (structureKey != null && prevPos != null) {
				setSearchRadius(stack, 0, player);

				List<ResourceLocation> structureKeys;
				if (isGroup) {
					// The compass stores the ID of the specific structure found, so re-determine the group key
					ResourceLocation groupKey = StructureUtils.getStructureKeysToTypeKeys(level).get(structureKey);
					setSearching(stack, groupKey, player);
					structureKeys = StructureUtils.getStructuresForGroup(level, groupKey);
				} else {
					setSearching(stack, structureKey, player);
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

                int xpLevelCost = StructureUtils.getXpLevelsForStructure(level, structureKey);
                if (!player.getAbilities().instabuild && xpLevelCost > 0) {
                    player.giveExperienceLevels(-xpLevelCost);
                }
			}
		}
	}

	public void succeed(ItemStack stack, ResourceLocation structureKey, boolean isGroup, int x, int z, List<BlockPos> prevPos, int samples, boolean displayCoordinates) {
		setFound(stack, structureKey, x, z, samples);
		setDisplayCoordinates(stack, displayCoordinates);
		setIsGroup(stack, isGroup);
		setPrevPos(stack, prevPos);
		damageCompass(stack);
		workerManager.clear();
	}

	public void fail(ItemStack stack, ResourceLocation structureKey, int radius, int samples) {
		workerManager.pop();
		boolean started = workerManager.start();
		if (!started) {
			setNotFound(stack, radius, samples);
			if (ItemUtils.verifyNBT(stack)) {
				stack.getTag().putString("StructureKey", structureKey.toString());
			}
		}
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return getState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	public void setSearching(ItemStack stack, ResourceLocation structureKey, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putString("StructureKey", structureKey.toString());
			stack.getTag().putInt("State", CompassState.SEARCHING.getID());
		}
	}

	public void setFound(ItemStack stack, ResourceLocation structureKey, int x, int z, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.FOUND.getID());
			stack.getTag().putString("StructureKey", structureKey.toString());
			stack.getTag().putInt("FoundX", x);
			stack.getTag().putInt("FoundZ", z);
			stack.getTag().putInt("Samples", samples);
		}
	}

	public void setIsGroup(ItemStack stack, boolean isGroup) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putBoolean("IsGroup", isGroup);
		}
	}

	public void setNotFound(ItemStack stack, int searchRadius, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.NOT_FOUND.getID());
			stack.getTag().putInt("SearchRadius", searchRadius);
			stack.getTag().putInt("Samples", samples);
		}
	}

	public void setInactive(ItemStack stack, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.INACTIVE.getID());
			stack.getTag().remove("PrevPos");
		}
	}

	public void setState(ItemStack stack, BlockPos pos, CompassState state, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", state.getID());
		}
	}

	public void setFoundStructureX(ItemStack stack, int x, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("FoundX", x);
		}
	}

	public void setFoundStructureZ(ItemStack stack, int z, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("FoundZ", z);
		}
	}

	public void setStructureKey(ItemStack stack, ResourceLocation structureKey, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putString("StructureKey", structureKey.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("SearchRadius", searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("Samples", samples);
		}
	}

	public void setDisplayCoordinates(ItemStack stack, boolean displayPosition) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putBoolean("DisplayCoordinates", displayPosition);
		}
	}

	public CompassState getState(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return CompassState.fromID(stack.getTag().getInt("State"));
		}

		return null;
	}

	public boolean getIsGroup(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("IsGroup")) {
			return stack.getTag().getBoolean("IsGroup");
		}
		return false;
	}

    private void setPrevPos(ItemStack stack, List<BlockPos> prevPos) {
        if (ItemUtils.verifyNBT(stack)) {
            ListTag listTag = new ListTag();
            for (BlockPos pos : prevPos) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("X", pos.getX());
                posTag.putInt("Y", pos.getY());
                posTag.putInt("Z", pos.getZ());
                listTag.add(posTag);
            }
            stack.getTag().put("PrevPos", listTag);
        }
    }

    private List<BlockPos> getPrevPos(ItemStack stack) {
        List<BlockPos> prevPos = new ArrayList<BlockPos>();
        if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("PrevPos", Tag.TAG_LIST)) {
            ListTag listTag = stack.getTag().getList("PrevPos", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag posTag = listTag.getCompound(i);
                prevPos.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
            }
        }
        return prevPos;
    }

	public int getFoundStructureX(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("FoundX");
		}

		return 0;
	}

	public int getFoundStructureZ(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("FoundZ");
		}

		return 0;
	}

	public ResourceLocation getStructureKey(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("StructureKey")) {
			return new ResourceLocation(stack.getTag().getString("StructureKey"));
		}

		return null;
	}

	public int getSearchRadius(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("SearchRadius");
		}

		return -1;
	}

	public int getSamples(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("Samples");
		}

		return -1;
	}

	public int getDistanceToBiome(Player player, ItemStack stack) {
		return StructureUtils.getHorizontalDistanceToLocation(player, getFoundStructureX(stack), getFoundStructureZ(stack));
	}

	public boolean shouldDisplayCoordinates(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("DisplayCoordinates")) {
			return stack.getTag().getBoolean("DisplayCoordinates");
		}

		return true;
	}

	public int getDamage(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("CompassDamage")) {
			return stack.getTag().getInt("CompassDamage");
		}
		return 0;
	}

	private void damageCompass(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		if (max > 0) {
			int damage = getDamage(stack) + 1;
			stack.getOrCreateTag().putInt("CompassDamage", damage);
		}
	}

	public boolean isBroken(ItemStack stack) {
		int max = ConfigHandler.GENERAL.compassDurability.get();
		if (max > 0) {
			return getDamage(stack) >= max;
		}
		return false;
	}

}
