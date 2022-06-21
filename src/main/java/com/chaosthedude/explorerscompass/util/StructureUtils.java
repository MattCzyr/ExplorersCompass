package com.chaosthedude.explorerscompass.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

public class StructureUtils {
	
	public static ListMultimap<ResourceLocation, ResourceLocation> getTypeKeysToStructureKeys(ServerLevel level) {
		ListMultimap<ResourceLocation, ResourceLocation> typeKeysToStructureKeys = ArrayListMultimap.create();
		for (Structure structure : getStructureRegistry(level)) {
			typeKeysToStructureKeys.put(getTypeForStructure(level, structure), getKeyForStructure(level, structure));
		}
		return typeKeysToStructureKeys;
	}
	
	public static Map<ResourceLocation, ResourceLocation> getStructureKeysToTypeKeys(ServerLevel level) {
		Map<ResourceLocation, ResourceLocation> structureKeysToStructureKeys = new HashMap<ResourceLocation, ResourceLocation>();
		for (Structure structure : getStructureRegistry(level)) {
			structureKeysToStructureKeys.put(getKeyForStructure(level, structure), getTypeForStructure(level, structure));
		}
		return structureKeysToStructureKeys;
	}
	
	public static ResourceLocation getTypeForStructure(ServerLevel level, Structure structure) {
		for (List<String> entry : ConfigHandler.GENERAL.structureGroupOverrides.get()) {
			// Need to do some parsing since we are storing a map as a list of lists
			if (entry.size() < 2) {
				continue;
			}
			String overrideStructureKey = entry.get(0);
			String overrideGroupKey = entry.get(1);
			if (getKeyForStructure(level, structure).toString().matches(convertToRegex(overrideStructureKey)) && ResourceLocation.isValidResourceLocation(overrideGroupKey)) {
				return new ResourceLocation(overrideGroupKey);
			}
		}
		return Registry.STRUCTURE_TYPES.getKey(structure.type());
	}

	public static ResourceLocation getKeyForStructure(ServerLevel level, Structure structure) {
		return getStructureRegistry(level).getKey(structure);
	}

	public static Structure getStructureForKey(ServerLevel level, ResourceLocation key) {
		return getStructureRegistry(level).get(key);
	}

	public static List<ResourceLocation> getAllowedStructureKeys(ServerLevel level) {
		final List<ResourceLocation> structures = new ArrayList<ResourceLocation>();
		for (Structure structure : getStructureRegistry(level)) {
			if (structure != null && getKeyForStructure(level, structure) != null && !structureIsBlacklisted(level, structure)) {
				structures.add(getKeyForStructure(level, structure));
			}
		}
		return structures;
	}
	
	public static boolean structureIsBlacklisted(ServerLevel level, Structure structure) {
		final List<String> structureBlacklist = ConfigHandler.GENERAL.structureBlacklist.get();
		for (String structureKey : structureBlacklist) {
			if (getKeyForStructure(level, structure).toString().matches(convertToRegex(structureKey))) {
				return true;
			}
		}
		return false;
	}

	public static void searchForStructure(ServerLevel serverLevel, Player player, ItemStack stack, List<Structure> structures, BlockPos startPos) {
		StructureSearchWorker worker = new StructureSearchWorker(serverLevel, player, stack, structures, startPos);
		worker.start();
	}

	public static List<ResourceLocation> getGeneratingDimensionKeys(ServerLevel serverLevel, Structure structure) {
		final List<ResourceLocation> dimensions = new ArrayList<ResourceLocation>();
		for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
			ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
            Set<Holder<Biome>> biomeSet = chunkGenerator.getBiomeSource().possibleBiomes();
            if (!structure.biomes().stream().noneMatch(biomeSet::contains)) {
            	dimensions.add(level.dimension().location());
            }
		}
		// Fix empty dimensions for stronghold
		if (structure == Structures.STRONGHOLD && dimensions.isEmpty()) {
			dimensions.add(new ResourceLocation("minecraft:overworld"));
		}
		return dimensions;
	}

	public static ListMultimap<ResourceLocation, ResourceLocation> getGeneratingDimensionsForAllowedStructures(ServerLevel serverLevel) {
		ListMultimap<ResourceLocation, ResourceLocation> dimensionsForAllowedStructures = ArrayListMultimap.create();
		for (ResourceLocation structureKey : getAllowedStructureKeys(serverLevel)) {
			Structure structure = getStructureForKey(serverLevel, structureKey);
			dimensionsForAllowedStructures.putAll(structureKey, getGeneratingDimensionKeys(serverLevel, structure));
		}
		return dimensionsForAllowedStructures;
	}
	
	public static int getHorizontalDistanceToLocation(Player player, int x, int z) {
		return getHorizontalDistanceToLocation(player.blockPosition(), x, z);
	}

	public static int getHorizontalDistanceToLocation(BlockPos startPos, int x, int z) {
		return (int) Mth.sqrt((float) startPos.distSqr(new BlockPos(x, startPos.getY(), z)));
	}
	
	@OnlyIn(Dist.CLIENT)
	public static String getPrettyStructureName(ResourceLocation key) {
		String name = key.toString();
		if (ConfigHandler.CLIENT.translateStructureNames.get()) {
			name = I18n.get(Util.makeDescriptionId("structure", key));
		}
		if (name.equals(Util.makeDescriptionId("structure", key)) || !ConfigHandler.CLIENT.translateStructureNames.get()) {
			name = key.toString();
			if (name.contains(":")) {
				name = name.substring(name.indexOf(":") + 1);
			}
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}

	@OnlyIn(Dist.CLIENT)
	public static String getPrettyStructureSource(ResourceLocation key) {
		if (key == null) {
			return "";
		}
		String registryEntry = key.toString();
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

	@OnlyIn(Dist.CLIENT)
	public static String dimensionKeysToString(List<ResourceLocation> dimensions) {
		Set<String> dimensionNames = new HashSet<String>();
		dimensions.forEach((key) -> dimensionNames.add(getDimensionName(key)));
		return String.join(", ", dimensionNames);
	}

	@OnlyIn(Dist.CLIENT)
	private static String getDimensionName(ResourceLocation dimensionKey) {
		String name = I18n.get(Util.makeDescriptionId("dimension", dimensionKey));
		if (name.equals(Util.makeDescriptionId("dimension", dimensionKey))) {
			name = dimensionKey.toString();
			if (name.contains(":")) {
				name = name.substring(name.indexOf(":") + 1);
			}
			name = WordUtils.capitalize(name.replace('_', ' '));
		}
		return name;
	}
	
	private static Registry<Structure> getStructureRegistry(ServerLevel level) {
		return level.registryAccess().ownedRegistryOrThrow(Registry.STRUCTURE_REGISTRY);
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