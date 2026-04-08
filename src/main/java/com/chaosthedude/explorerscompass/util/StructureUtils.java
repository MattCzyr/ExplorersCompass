package com.chaosthedude.explorerscompass.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSet.StructureSelectionEntry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

public class StructureUtils {

	public static Map<ResourceLocation, ResourceLocation> getStructureKeysToTypeKeys(ServerLevel level) {
		Map<ResourceLocation, ResourceLocation> structureKeysToStructureKeys = new HashMap<ResourceLocation, ResourceLocation>();
		for (Structure structure : getStructureRegistry(level)) {
			structureKeysToStructureKeys.put(getKeyForStructure(level, structure), getTypeForStructure(level, structure));
		}
		return structureKeysToStructureKeys;
	}

	public static ResourceLocation getTypeForStructure(ServerLevel level, Structure structure) {
		Registry<StructureSet> registry = getStructureSetRegistry(level);
		for (StructureSet set : registry) {
			for (StructureSelectionEntry entry : set.structures()) {
				if (entry.structure().get().equals(structure)) {
					return registry.getKey(set);
				}
			}
		}
		return new ResourceLocation(ExplorersCompass.MODID, "none");
	}

	public static List<ResourceLocation> getStructuresForGroup(ServerLevel level, ResourceLocation groupKey) {
		List<ResourceLocation> structureKeys = new ArrayList<ResourceLocation>();
		Registry<StructureSet> registry = getStructureSetRegistry(level);
		StructureSet set = registry.get(groupKey);
		if (set != null) {
			for (StructureSelectionEntry entry : set.structures()) {
				ResourceLocation structureKey = getKeyForStructure(level, entry.structure().get());
				if (structureKey != null) {
					structureKeys.add(structureKey);
				}
			}
		}
		return structureKeys;
	}

	public static ResourceLocation getKeyForStructure(ServerLevel level, Structure structure) {
		return getStructureRegistry(level).getKey(structure);
	}

	public static Structure getStructureForKey(ServerLevel level, ResourceLocation key) {
		return getStructureRegistry(level).get(key);
	}
	
	public static Holder<Structure> getHolderForStructure(ServerLevel level, Structure structure) {
		Optional<ResourceKey<Structure>> optional = getStructureRegistry(level).getResourceKey(structure);
		if (optional.isPresent()) {
			return getStructureRegistry(level).getHolderOrThrow(optional.get());
		}
		return null;
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

	public static List<ResourceLocation> getGeneratingDimensionKeys(ServerLevel serverLevel, Structure structure) {
		final List<ResourceLocation> dimensions = new ArrayList<ResourceLocation>();
		for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
			ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
			Set<Holder<Biome>> biomeSet = chunkGenerator.getBiomeSource().possibleBiomes();
			if (!structure.biomes().stream().noneMatch(biomeSet::contains)) {
				dimensions.add(level.dimension().location());
			}
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

	public static int getXpLevelsForStructure(ServerLevel serverLevel, ResourceLocation structureKey) {
		int xpLevels = ConfigHandler.GENERAL.defaultXpLevels.get();
		final Map<String, Integer> xpLevelOverrides = parseXpLevelOverridesConfig();
		for (String structureRegex : xpLevelOverrides.keySet()) {
			if (structureKey.toString().matches(convertToRegex(structureRegex))) {
				xpLevels = xpLevelOverrides.get(structureRegex);
				if (xpLevels > 3) {
					xpLevels = 3;
				}
				break;
			}
		}
		return xpLevels;
	}

	public static Map<String, Integer> parseXpLevelOverridesConfig() {
		final List<String> xpLevelOverrides = ConfigHandler.GENERAL.perStructureXpLevels.get();
		Map<String, Integer> parsedOverrides = new HashMap<String, Integer>();
		for (String override : xpLevelOverrides) {
			String[] split = override.split(",");
			if (split.length != 2) {
				continue;
			}
			String structureRegex = split[0];
			String xpLevelsStr = split[1];
			try {
				int xpLevels = Integer.valueOf(xpLevelsStr);
				parsedOverrides.put(structureRegex, xpLevels);
			} catch (NumberFormatException e) {
				continue;
			}
		}
		return parsedOverrides;
	}

	public static Map<ResourceLocation, Integer> getXpLevelsForAllowedStructures(ServerLevel serverLevel) {
		final Map<ResourceLocation, Integer> xpLevels = new HashMap<ResourceLocation, Integer>();
		for (ResourceLocation structureKey : getAllowedStructureKeys(serverLevel)) {
			xpLevels.put(structureKey, getXpLevelsForStructure(serverLevel, structureKey));
		}
		return xpLevels;
	}

	public static int getHorizontalDistanceToLocation(Player player, int x, int z) {
		return getHorizontalDistanceToLocation(player.blockPosition(), x, z);
	}

	public static int getHorizontalDistanceToLocation(BlockPos startPos, int x, int z) {
		return (int) Mth.sqrt((float) startPos.distSqr(new BlockPos(x, startPos.getY(), z)));
	}

	@OnlyIn(Dist.CLIENT)
	public static String getPrettyStructureName(ResourceLocation key) {
		if (key == null) {
			return "";
		}
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
		return level.registryAccess().registryOrThrow(Registries.STRUCTURE);
	}

	private static Registry<StructureSet> getStructureSetRegistry(ServerLevel level) {
		return level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
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