package com.chaosthedude.explorerscompass.item;

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
import com.chaosthedude.explorerscompass.worker.SearchWorkerManager;
import com.google.common.collect.ListMultimap;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ExplorersCompassItem extends Item {

	public static final String NAME = "explorerscompass";

	public static final ResourceKey<Item> KEY = ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(ExplorersCompass.MODID, NAME));

	private SearchWorkerManager workerManager;

	public ExplorersCompassItem() {
		super(new Properties().setId(KEY).stacksTo(1));
		workerManager = new SearchWorkerManager();
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!player.isCrouching()) {
            if (isBroken(player.getItemInHand(hand))) {
                return InteractionResult.PASS;
            }
            
			if (level.isClientSide()) {
				final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.EXPLORERS_COMPASS_ITEM);
				GuiWrapper.openGUI(level, player, stack);
			} else {
				final ServerLevel serverLevel = (ServerLevel) level;
				final ServerPlayer serverPlayer = (ServerPlayer) player;
				final boolean canTeleport = ExplorersCompassConfig.allowTeleport && PlayerUtils.canTeleport(serverLevel.getServer(), player);
				final int maxNextSearches = ExplorersCompassConfig.maxNextSearches;
				final boolean hasInfiniteXp = player.hasInfiniteMaterials();
				final List<Identifier> allowedStructureIds = StructureUtils.getAllowedStructureIds(serverLevel);
				final Map<Identifier, Integer> xpLevels = StructureUtils.getXpLevelsForAllowedStructures(serverLevel, allowedStructureIds);
				final ListMultimap<Identifier, Identifier> generatingDimensions = StructureUtils.getGeneratingDimensionIdsForAllowedStructures(serverLevel, allowedStructureIds);
				ServerPlayNetworking.send(serverPlayer, new SyncPacket(canTeleport, maxNextSearches, hasInfiniteXp, allowedStructureIds, xpLevels, generatingDimensions, StructureUtils.structureIdsToGroupIds(serverLevel)));
			}
		} else {
			workerManager.stop();
			workerManager.clear();
			ItemStack stack = player.getItemInHand(hand);
			clearSearchData(stack);
		}

		return InteractionResult.CONSUME;
	}

    @Override
    public boolean isBarVisible(ItemStack stack) {
        int max = ExplorersCompassConfig.compassDurability;
        if (max > 0) {
            int damage = stack.getOrDefault(ExplorersCompass.DAMAGE, 0);
            return damage > 0;
        }
        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int max = ExplorersCompassConfig.compassDurability;
        if (max > 0) {
            int damage = stack.getOrDefault(ExplorersCompass.DAMAGE, 0);
            return Math.clamp(Math.round(13.0f * (1.0f - (float) damage / max)), 0, 13);
        }
        return 13;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int max = ExplorersCompassConfig.compassDurability;
        int damage = stack.getOrDefault(ExplorersCompass.DAMAGE, 0);
        float f = max > 0 ? (float) damage / max : 0.0f;
        return Mth.hsvToRgb(Math.max(0.0F, (1.0F - f) / 3.0F), 1.0F, 1.0F);
    }

	public void searchForStructure(ServerLevel level, Player player, BlockPos pos, Identifier structureOrGroupId, boolean isGroup, ItemStack stack) {
        if (!isBroken(stack)) {
            search(stack, structureOrGroupId, isGroup);

            List<Identifier> structureIds = List.of(structureOrGroupId);
            if (isGroup) {
                structureIds = StructureUtils.getStructuresForGroup(level, structureOrGroupId);
            }

            List<Structure> structures = new ArrayList<Structure>();
            for (Identifier key : structureIds) {
                structures.add(StructureUtils.getStructureForId(level, key));
            }
            List<BlockPos> prevPos = new ArrayList<BlockPos>();
            workerManager.stop();
            workerManager.createWorkers(level, player, stack, structures, structureOrGroupId, isGroup, pos, prevPos);
            boolean started = workerManager.start();
            if (!started) {
                fail(stack, structureOrGroupId, 0, 0);
            }

            int xpLevels = StructureUtils.getXpLevelsForStructure(level, structureOrGroupId);
            if (!player.hasInfiniteMaterials() && xpLevels > 0) {
                player.giveExperienceLevels(-xpLevels);
            }
        }
	}

	public void searchForNextStructure(ServerLevel level, Player player, BlockPos pos, ItemStack stack) {
        if (!isBroken(stack)) {
            String structureIdStr = stack.getOrDefault(ExplorersCompass.STRUCTURE_ID, null);
            List<BlockPos> prevPos = stack.getOrDefault(ExplorersCompass.PREV_POS, null);
            boolean isGroup = stack.getOrDefault(ExplorersCompass.IS_GROUP, false);
            if (structureIdStr != null && prevPos != null) {
                Identifier structureId = Identifier.parse(structureIdStr);

                List<Identifier> structureIds;
                if (isGroup) {
                    // The compass will always store the ID of the specific structure that was found, even if the
                    // search itself was for a group, so we need to re-determine the group ID
                    Identifier groupId = StructureUtils.structureIdsToGroupIds(level).get(structureId);
                    search(stack, groupId, isGroup);
                    structureIds = StructureUtils.getStructuresForGroup(level, groupId);
                } else {
                    search(stack, structureId, isGroup);
                    structureIds = List.of(structureId);
                }

                List<Structure> structures = new ArrayList<Structure>();
                for (Identifier key : structureIds) {
                    structures.add(StructureUtils.getStructureForId(level, key));
                }
                workerManager.stop();
                workerManager.createWorkers(level, player, stack, structures, structureId, isGroup, pos, prevPos);
                boolean started = workerManager.start();
                if (!started) {
                    fail(stack, structureId, 0, 0);
                }

                int xpLevels = StructureUtils.getXpLevelsForStructure(level, structureId);
                if (!player.hasInfiniteMaterials() && xpLevels > 0) {
                    player.giveExperienceLevels(-xpLevels);
                }
            }
        }
	}

	public void succeed(Player player, ItemStack stack, Identifier structureID, boolean isGroup, int x, int z, List<BlockPos> prevPos, int samples, boolean displayCoordinates) {
		clearSearchData(stack);
		setCompassState(stack, CompassState.FOUND);
		stack.set(ExplorersCompass.STRUCTURE_ID, structureID.toString());
		stack.set(ExplorersCompass.IS_GROUP, isGroup);
		stack.set(ExplorersCompass.FOUND_X, x);
		stack.set(ExplorersCompass.FOUND_Z, z);
		stack.set(ExplorersCompass.PREV_POS, prevPos);
		stack.set(ExplorersCompass.SAMPLES, samples);
		stack.set(ExplorersCompass.DISPLAY_COORDS, displayCoordinates);
        damageCompass(player, stack);
		workerManager.clear();
	}

	public void fail(ItemStack stack, Identifier structureId, int radius, int samples) {
		workerManager.pop();
		boolean started = workerManager.start();
		if (!started) {
			clearSearchData(stack);
			setCompassState(stack, CompassState.NOT_FOUND);
			stack.set(ExplorersCompass.STRUCTURE_ID, structureId.toString());
			stack.set(ExplorersCompass.SEARCH_RADIUS, radius);
			stack.set(ExplorersCompass.SAMPLES, samples);
		}
	}

	public void search(ItemStack stack, Identifier structureId, boolean isGroup) {
		clearSearchData(stack);
		stack.set(ExplorersCompass.COMPASS_STATE, CompassState.SEARCHING.getID());
		stack.set(ExplorersCompass.STRUCTURE_ID, structureId.toString());
		stack.set(ExplorersCompass.IS_GROUP, isGroup);
		stack.set(ExplorersCompass.SEARCH_RADIUS, 0);
		stack.set(ExplorersCompass.SAMPLES, 0);
	}

	public void setCompassState(ItemStack stack, CompassState state) {
		stack.set(ExplorersCompass.COMPASS_STATE, state.getID());
	}

	public CompassState getCompassState(ItemStack stack) {
        if (stack.has(ExplorersCompass.COMPASS_STATE)) {
            return CompassState.fromID(stack.get(ExplorersCompass.COMPASS_STATE));
        }
        return null;
	}

	public int getDamage(ItemStack stack) {
		return stack.getOrDefault(ExplorersCompass.DAMAGE, 0);
	}

    private void damageCompass(Player player, ItemStack stack) {
        int max = ExplorersCompassConfig.compassDurability;
        if (!player.hasInfiniteMaterials() && max > 0) {
            int damage = getDamage(stack) + 1;
            stack.set(ExplorersCompass.DAMAGE, damage);
        }
    }

	public boolean isBroken(ItemStack stack) {
		int max = ExplorersCompassConfig.compassDurability;
		if (max > 0) {
			return getDamage(stack) >= max;
		}
		return false;
	}

	private void clearSearchData(ItemStack stack) {
		stack.remove(ExplorersCompass.COMPASS_STATE);
		stack.remove(ExplorersCompass.STRUCTURE_ID);
		stack.remove(ExplorersCompass.FOUND_X);
		stack.remove(ExplorersCompass.FOUND_Z);
		stack.remove(ExplorersCompass.PREV_POS);
		stack.remove(ExplorersCompass.IS_GROUP);
		stack.remove(ExplorersCompass.DISPLAY_COORDS);
		stack.remove(ExplorersCompass.SEARCH_RADIUS);
		stack.remove(ExplorersCompass.SAMPLES);
	}
}
