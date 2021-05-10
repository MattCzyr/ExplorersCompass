package com.chaosthedude.explorerscompass.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.explorerscompass.config.ConfigHandler;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class StructureUtils {

	public static ResourceLocation getKeyForStructure(Structure<?> structure) {
		return ForgeRegistries.STRUCTURE_FEATURES.getKey(structure);
	}

	public static Structure<?> getStructureForKey(ResourceLocation key) {
		return ForgeRegistries.STRUCTURE_FEATURES.getValue(key);
	}

	public static List<Structure<?>> getAllowedStructures() {
		final List<Structure<?>> structures = new ArrayList<Structure<?>>();
		for (Structure<?> structure : ForgeRegistries.STRUCTURE_FEATURES) {
			if (structure != null && getStructureForKey(structure.getRegistryName()) != null && !structureIsBlacklisted(structure)) {
				structures.add(structure);
			}
		}

		return structures;
	}

	public static void searchForStructure(ServerWorld world, PlayerEntity player, ItemStack stack, Structure<?> structure, BlockPos startPos) {
		StructureSearchWorker worker = new StructureSearchWorker(world, player, stack, structure, startPos);
		worker.start();
	}

	public static int getDistanceToStructure(PlayerEntity player, int biomeX, int biomeZ) {
		return getDistanceToStructure(player.getPosition(), biomeX, biomeZ);
	}

	public static int getDistanceToStructure(BlockPos startPos, int structureX, int structureZ) {
		return (int) MathHelper.sqrt(startPos.distanceSq(new BlockPos(structureX, startPos.getY(), structureZ)));
	}

	@OnlyIn(Dist.CLIENT)
	public static String getStructureName(Structure<?> structure) {
		String name = structure.getStructureName();
		if (ConfigHandler.CLIENT.translateStructureNames.get()) {
			name = I18n.format(Util.makeTranslationKey("structure", getKeyForStructure(structure)));
		}
		if (name.equals(Util.makeTranslationKey("structure", getKeyForStructure(structure))) || !ConfigHandler.CLIENT.translateStructureNames.get()) {
			name = structure.getStructureName();
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}

	@OnlyIn(Dist.CLIENT)
	public static String getStructureName(ResourceLocation key) {
		return getStructureName(getStructureForKey(key));
	}

	@OnlyIn(Dist.CLIENT)
	public static String getStructureSource(Structure<?> structure) {
		if (getKeyForStructure(structure) == null) {
			return "";
		}
		String registryEntry = getKeyForStructure(structure).toString();
		String modid = registryEntry.substring(0, registryEntry.indexOf(":"));
		if (modid.equals("minecraft")) {
			return "Minecraft";
		}
		Optional<? extends ModContainer> sourceContainer = ModList.get().getModContainerById(modid);
		if (sourceContainer.isPresent()) {
			return sourceContainer.get().getModInfo().getDisplayName();
		}
		return modid;
	}

	public static boolean structureIsBlacklisted(Structure<?> structure) {
		final List<String> structureBlacklist = ConfigHandler.GENERAL.structureBlacklist.get();
		final ResourceLocation structureResourceLocation = getKeyForStructure(structure);
		return structureBlacklist.contains(String.valueOf(StructureUtils.getKeyForStructure(structure))) || (structureResourceLocation != null && structureBlacklist.contains(structureResourceLocation.toString()));
	}

}