package com.chaosthedude.explorerscompass.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.explorerscompass.config.ConfigHandler;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class StructureUtils {
	
	public static MutableRegistry<Structure<?>> getStructureRegistry(World world) {
		return world.func_241828_r().getRegistry(ForgeRegistries.Keys.STRUCTURE_FEATURES);
	}

	public static ResourceLocation getKeyForStructure(World world, Structure<?> structure) {
		return getStructureRegistry(world).getKey(structure);
	}

	public static Optional<Structure<?>> getStructureForKey(World world, ResourceLocation key) {
		return getStructureRegistry(world).getOptional(key);
	}

	public static List<Structure<?>> getAllowedStructures(World world) {
		final List<Structure<?>> structures = new ArrayList<Structure<?>>();
		for (Map.Entry<RegistryKey<Structure<?>>, Structure<?>> entry : getStructureRegistry(world).getEntries()) {
 			Structure<?> structure = entry.getValue();
 			if (structure != null && getKeyForStructure(world, structure) != null && !structureIsBlacklisted(world, structure)) {
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
	public static String getStructureName(World world, Structure<?> structure) {
		String name = structure.getStructureName();
		if (ConfigHandler.CLIENT.translateStructureNames.get()) {
			name = I18n.format(Util.makeTranslationKey("structure", getKeyForStructure(world, structure)));
		}
		if (name.equals(Util.makeTranslationKey("structure", getKeyForStructure(world, structure))) || !ConfigHandler.CLIENT.translateStructureNames.get()) {
			name = structure.getStructureName();
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}

	@OnlyIn(Dist.CLIENT)
	public static String getStructureName(World world, ResourceLocation key) {
		Optional<Structure<?>> optionalStructure = getStructureForKey(world, key);
		if (optionalStructure.isPresent()) {
			return getStructureName(world, optionalStructure.get());
		}
		return "";
	}

	@OnlyIn(Dist.CLIENT)
	public static String getStructureSource(World world, Structure<?> structure) {
		if (getKeyForStructure(world, structure) == null) {
			return "";
		}
		String registryEntry = getKeyForStructure(world, structure).toString();
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

	public static boolean structureIsBlacklisted(World world, Structure<?> structure) {
		final List<String> structureBlacklist = ConfigHandler.GENERAL.structureBlacklist.get();
		for (String structureKey : structureBlacklist) {
			if (getKeyForStructure(world, structure).toString().matches(convertToRegex(structureKey))) {
				return true;
			}
		}
		return false;
	}
	
	private static String convertToRegex(String glob) {
 		String regex = "^";
 		for (char i = 0; i < glob.length(); i++) {
 			char c = glob.charAt(i);
 			if (c == '*') {
 				regex += ".*";
 			} else if (c == '?') {
 				regex += ".";
 			} else if (c == '.') {
 				regex += "\\.";
 			} else {
 				regex += c;
 			}
 		}
 		regex += "$";
 		return regex;
 	}

}