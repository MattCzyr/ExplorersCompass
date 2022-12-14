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

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
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
		super(new FabricItemSettings().maxCount(1));
		workerManager = new SearchWorkerManager();
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (!player.isSneaking()) {
			if (world.isClient) {
				final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.EXPLORERS_COMPASS_ITEM);
				GuiWrapper.openGUI(world, player, stack);
			} else {
				final ServerWorld serverWorld = (ServerWorld) world;
				final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				final boolean canTeleport = ExplorersCompassConfig.allowTeleport && PlayerUtils.canTeleport(player);
				final List<Identifier> allowedStructures = StructureUtils.getAllowedStructureIDs(serverWorld);
				final ListMultimap<Identifier, Identifier> dimensionsForAllowedStructures = StructureUtils.getGeneratingDimensionIDsForAllowedStructures(serverWorld);
				final Map<Identifier, Identifier> structureIDsToGroupIDs = StructureUtils.getStructureIDsToGroupIDs(serverWorld);
				final ListMultimap<Identifier, Identifier> groupIDsToStructureIDs = StructureUtils.getGroupIDsToStructureIDs(serverWorld);
				ServerPlayNetworking.send(serverPlayer, SyncPacket.ID, new SyncPacket(canTeleport, allowedStructures, dimensionsForAllowedStructures, structureIDsToGroupIDs, groupIDsToStructureIDs));
			}
		} else {
			workerManager.stop();
			workerManager.clear();
			setState(player.getStackInHand(hand), null, CompassState.INACTIVE);
		}

		return TypedActionResult.pass(player.getStackInHand(hand));
	}

	public void searchForStructure(World world, PlayerEntity player, Identifier structureID, List<Identifier> structureIDs, BlockPos pos, ItemStack stack) {
		setSearching(stack, structureID);
		setSearchRadius(stack, 0);
		if (world instanceof ServerWorld) {
			ServerWorld serverWorld = (ServerWorld) world;
			List<Structure> structures = new ArrayList<Structure>();
			for (Identifier id : structureIDs) {
				structures.add(StructureUtils.getStructureForID(serverWorld, id));
			}
			workerManager.stop();
			workerManager.createWorkers(serverWorld, player, stack, structures, pos);
			boolean started = workerManager.start();
			if (!started) {
				setNotFound(stack, 0, 0);
			}
		}
	}
	
	public void succeed(ItemStack stack, Identifier structureID, int x, int z, int samples, boolean displayCoordinates) {
		setFound(stack, structureID, x, z, samples);
		setDisplayCoordinates(stack, displayCoordinates);
		workerManager.clear();
	}
	
	public void fail(ItemStack stack, int radius, int samples) {
		workerManager.pop();
		boolean started = workerManager.start();
		if (!started) {
			setNotFound(stack, radius, samples);
		}
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return getState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	public void setSearching(ItemStack stack, Identifier structureID) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putString("StructureID", structureID.toString());
			stack.getNbt().putInt("State", CompassState.SEARCHING.getID());
		}
	}

	public void setFound(ItemStack stack, Identifier structureID, int x, int z, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putString("StructureID", structureID.toString());
			stack.getNbt().putInt("State", CompassState.FOUND.getID());
			stack.getNbt().putInt("FoundX", x);
			stack.getNbt().putInt("FoundZ", z);
			stack.getNbt().putInt("Samples", samples);
		}
	}

	public void setNotFound(ItemStack stack, int searchRadius, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("State", CompassState.NOT_FOUND.getID());
			stack.getNbt().putInt("SearchRadius", searchRadius);
			stack.getNbt().putInt("Samples", samples);
		}
	}

	public void setInactive(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("State", CompassState.INACTIVE.getID());
		}
	}

	public void setState(ItemStack stack, BlockPos pos, CompassState state) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("State", state.getID());
		}
	}

	public void setFoundStructureX(ItemStack stack, int x) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("FoundX", x);
		}
	}

	public void setFoundStructureZ(ItemStack stack, int z) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("FoundZ", z);
		}
	}

	public void setStructureKey(ItemStack stack, Identifier structureID) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putString("StructureID", structureID.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("SearchRadius", searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putInt("Samples", samples);
		}
	}
	
	public void setDisplayCoordinates(ItemStack stack, boolean displayPosition) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getNbt().putBoolean("DisplayCoordinates", displayPosition);
		}
	}

	public CompassState getState(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return CompassState.fromID(stack.getNbt().getInt("State"));
		}

		return null;
	}

	public int getFoundStructureX(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getNbt().getInt("FoundX");
		}

		return 0;
	}

	public int getFoundStructureZ(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getNbt().getInt("FoundZ");
		}

		return 0;
	}

	public Identifier getStructureID(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return new Identifier(stack.getNbt().getString("StructureID"));
		}

		return new Identifier("");
	}

	public int getSearchRadius(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getNbt().getInt("SearchRadius");
		}

		return -1;
	}

	public int getSamples(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getNbt().getInt("Samples");
		}

		return -1;
	}

	public int getDistanceToBiome(PlayerEntity player, ItemStack stack) {
		return StructureUtils.getHorizontalDistanceToLocation(player, getFoundStructureX(stack), getFoundStructureZ(stack));
	}
	
	public boolean shouldDisplayCoordinates(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getNbt().contains("DisplayCoordinates")) {
			return stack.getNbt().getBoolean("DisplayCoordinates");
		}

		return true;
	}

}
